import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { join } from 'node:path';

const browserDistFolder = join(import.meta.dirname, '../browser');

/** URL interna do backend na rede do Docker. */
const apiUrl = process.env['API_URL'] ?? 'http://localhost:8080';

const app = express();
const angularApp = new AngularNodeAppEngine();

app.disable('x-powered-by');

/**
 * Proxy da API pública.
 *
 * O navegador chama sempre `/api/...` no mesmo host: não há CORS, a URL interna
 * do backend não vaza para o cliente e o SSR usa a mesma origem lógica.
 */
// Limite alto o bastante para os vídeos aceitos pelo gerenciador de mídia (seção 12) —
// o próprio backend aplica um limite mais específico por tipo de arquivo.
app.use('/api', express.raw({ type: '*/*', limit: '90mb' }), async (req, res) => {
  const destino = `${apiUrl}/api${req.url}`;
  const temCorpo = !['GET', 'HEAD'].includes(req.method);
  try {
    const resposta = await fetch(destino, {
      method: req.method,
      headers: {
        accept: req.headers.accept ?? 'application/json',
        ...(req.headers['content-type'] ? { 'content-type': req.headers['content-type'] } : {}),
        // Repassa o Range para permitir busca/streaming parcial de vídeos servidos via /api/v1/midias.
        ...(req.headers.range ? { range: req.headers.range as string } : {}),
      },
      // Uint8Array em vez de Buffer: é o tipo que a assinatura de fetch aceita.
      body: temCorpo && Buffer.isBuffer(req.body) ? new Uint8Array(req.body) : undefined,
    });

    res.status(resposta.status);
    resposta.headers.forEach((valor, chave) => {
      if (!['content-encoding', 'transfer-encoding', 'connection'].includes(chave)) {
        res.setHeader(chave, valor);
      }
    });
    const corpo = Buffer.from(await resposta.arrayBuffer());
    res.end(corpo);
  } catch (erro) {
    console.error(`[proxy] falha ao chamar ${destino}`, erro);
    res.status(502).json({ erro: 'Serviço de imóveis indisponível no momento.' });
  }
});

/** Sonda usada pelo healthcheck do container. */
app.get('/healthz', (_req, res) => {
  res.json({ status: 'UP', api: apiUrl });
});

/**
 * Sitemap gerado a partir do catálogo publicado (seção 20): páginas
 * institucionais, as listagens indexáveis e cada imóvel no ar.
 */
app.get('/sitemap.xml', async (_req, res) => {
  const site = process.env['SITE_URL'] ?? 'https://www.terranexo.com.br';
  const hoje = new Date().toISOString().slice(0, 10);

  const urls: { loc: string; prioridade: string; frequencia: string; lastmod?: string }[] = [
    { loc: '/', prioridade: '1.0', frequencia: 'daily' },
    { loc: '/imoveis', prioridade: '0.9', frequencia: 'daily' },
    { loc: '/imoveis?categoria=URBANO', prioridade: '0.8', frequencia: 'daily' },
    { loc: '/imoveis?categoria=RURAL', prioridade: '0.8', frequencia: 'daily' },
    { loc: '/imoveis?finalidade=VENDA', prioridade: '0.7', frequencia: 'daily' },
    { loc: '/imoveis?finalidade=LOCACAO', prioridade: '0.7', frequencia: 'daily' },
  ];

  try {
    const resposta = await fetch(`${apiUrl}/api/v1/public/properties?tamanho=48`);
    if (resposta.ok) {
      const dados = (await resposta.json()) as {
        conteudo: { url: string; atualizadoEm: string }[];
      };
      for (const imovel of dados.conteudo) {
        urls.push({
          loc: imovel.url,
          prioridade: '0.6',
          frequencia: 'weekly',
          lastmod: imovel.atualizadoEm,
        });
      }
    }
  } catch (erro) {
    console.error('[sitemap] não foi possível listar os imóveis', erro);
  }

  const corpo = urls
    .map(
      (u) => `  <url>
    <loc>${site}${u.loc.replace(/&/g, '&amp;')}</loc>
    <lastmod>${u.lastmod ?? hoje}</lastmod>
    <changefreq>${u.frequencia}</changefreq>
    <priority>${u.prioridade}</priority>
  </url>`,
    )
    .join('\n');

  res.type('application/xml').send(
    `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${corpo}\n</urlset>\n`,
  );
});

/** Arquivos estáticos com hash no nome — cache longo. */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

/**
 * Rotas públicas conhecidas. O que não casar é renderizado como a página 404 do
 * portal, porém com status HTTP 404 — o rastreador precisa do código correto,
 * não de um "soft 404" (seção 20).
 */
const ROTAS_CONHECIDAS = [
  /^\/$/,
  /^\/imoveis\/?$/,
  /^\/imoveis\/[^/]+\/?$/,
  /^\/admin(\/.*)?$/,
];

/** Demais rotas são renderizadas pelo Angular no servidor. */
app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then(async (response) => {
      if (!response) {
        return next();
      }
      const caminho = new URL(req.url, 'http://interno').pathname;
      const conhecida = ROTAS_CONHECIDAS.some((padrao) => padrao.test(caminho));
      if (conhecida || response.status !== 200) {
        return writeResponseToNodeResponse(response, res);
      }
      const html = await response.text();
      return writeResponseToNodeResponse(
        new Response(html, { status: 404, headers: response.headers }),
        res,
      );
    })
    .catch(next);
});

if (isMainModule(import.meta.url) || process.env['pm_id']) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, () => {
    console.log(`Terra Nexo SSR ouvindo em http://localhost:${port} (API: ${apiUrl})`);
  });
}

export const reqHandler = createNodeRequestHandler(app);
