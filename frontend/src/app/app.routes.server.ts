import { RenderMode, ServerRoute } from '@angular/ssr';

/**
 * Estratégia de renderização (seção 20).
 *
 * A home e o resultado de busca dependem de dados da API e de query params,
 * portanto são renderizados no servidor a cada requisição — o HTML entregue ao
 * rastreador já contém os anúncios. A prerenderização das páginas institucionais
 * estáveis entra quando elas forem criadas, com a API disponível no build.
 */
export const serverRoutes: ServerRoute[] = [
  { path: '', renderMode: RenderMode.Server },
  { path: 'imoveis', renderMode: RenderMode.Server },
  { path: 'imoveis/:slug', renderMode: RenderMode.Server },
  // Telas autenticadas rodam no cliente: não são indexáveis e dependem de sessão.
  { path: 'admin', renderMode: RenderMode.Client },
  { path: 'admin/**', renderMode: RenderMode.Client },
  { path: '**', renderMode: RenderMode.Server },
];
