# Terra Nexo — Plataforma de imóveis urbanos e rurais

Protótipo evolutivo do MVP descrito em `docs/`. Esta entrega cobre duas frentes da
especificação:

- **Landing page** (seção 6) — banner, busca simplificada, destaques gerais,
  destaques urbanos e rurais separados, imóveis recentes, apresentação da
  imobiliária, regiões atendidas, serviços, chamada para proprietários,
  corretores, depoimentos e contato com captação de lead.
- **Módulo de resultado de busca** (seção 13) — pesquisa simples e avançada,
  filtros rurais adicionais, ordenação, facetas com contagem, paginação
  rastreável e cartão de resultado completo.
- **Administração** (seções 8, 9, 10 e 18) — painel com indicadores,
  pendências acionáveis e composição da carteira; listagem com filtros
  administrativos; e cadastro de imóveis com os blocos específicos de urbano
  e rural.

O backend e o frontend rodam em **containers separados** (seção 29).

## Stack

| Camada | Tecnologia |
| --- | --- |
| Portal público | Angular 20 com SSR (Node/Express), standalone components e signals |
| API | Java 21 + Spring Boot 3.3, REST, OpenAPI, Actuator |
| Persistência | Em memória nesta fase — a troca por PostgreSQL/PostGIS afeta apenas `infrastructure` |
| Execução | Docker Compose com dois serviços e rede interna |

## Como executar

```bash
docker compose up -d --build
```

| Endereço | O que é |
| --- | --- |
| http://localhost:4000 | Portal (landing page) |
| http://localhost:4000/imoveis | Módulo de resultado de busca |
| http://localhost:4000/sitemap.xml | Sitemap gerado a partir do catálogo publicado |
| http://localhost:4000/admin | Painel administrativo |
| http://localhost:4000/admin/imoveis/novo | Cadastro de imóvel urbano ou rural |
| http://localhost:18080/docs | OpenAPI (Swagger UI) da API |
| http://localhost:18080/actuator/health | Health check do backend |

A porta do backend (18080) serve apenas para inspeção: o portal fala com ele pela
rede interna do Compose, em `http://backend:8080`.

Para derrubar: `docker compose down`.

## Estrutura

```
.
├── backend/                  API Spring Boot (monólito modular, seção 30)
│   └── src/main/java/br/com/terranexo/
│       ├── property/         domain · application · api · infrastructure
│       ├── lead/             captação de contatos do portal
│       ├── broker/           corretores públicos
│       ├── region/           regiões atendidas
│       └── shared/           configuração e tratamento de erros
├── frontend/                 Portal Angular com SSR (seção 31)
│   └── src/app/
│       ├── core/             api, layout, seo, theme
│       ├── shared/           componentes e modelos
│       ├── public/           home · property-search · property-detail
│       └── admin/            dashboard · property-list · property-form
├── docs/                     Especificação, resumo e sumário executivo (.docx)
│   └── historico-da-sessao.md  Diário técnico: decisões, problemas e validações
├── legacy-prototipo/         Primeiro protótipo estático (referência de design)
└── docker-compose.yml
```

## API pública

Prefixo `/api/v1/public` (seção 23).

| Método | Endpoint | Finalidade |
| --- | --- | --- |
| GET | `/properties` | Pesquisar com filtros, ordenação, paginação e facetas |
| GET | `/properties/featured` | Destaques, opcionalmente por categoria |
| GET | `/properties/recent` | Publicados recentemente |
| GET | `/properties/{codeOrSlug}` | Consultar um imóvel |
| GET | `/properties/similar/{id}` | Imóveis semelhantes |
| GET | `/regions` | Regiões atendidas com contagem |
| GET | `/brokers` | Corretores públicos |
| POST | `/leads` | Captar contato do portal |

### Administração

Prefixo `/api/v1/admin`. No protótipo estas rotas estão abertas; a autenticação
e o controle por perfil entram na etapa 1 do plano de implementação.

| Método | Endpoint | Finalidade |
| --- | --- | --- |
| GET | `/dashboard` | Indicadores, pendências e últimos cadastros |
| GET | `/properties` | Listar com filtros administrativos |
| POST | `/properties` | Cadastrar imóvel urbano ou rural |
| GET | `/situacoes` | Situações administrativas disponíveis |

### Filtros da pesquisa

**Gerais:** `q`, `categoria`, `finalidade`, `tipo`, `uf`, `municipio`, `bairro`,
`regiao`, `precoMin`, `precoMax`, `areaMin`, `areaMax`, `dormitorios`, `suites`,
`banheiros`, `vagas`, `caracteristica`, `corretor`, `codigo`, `permuta`,
`financiamento`.

**Rurais:** `areaHaMin`, `areaHaMax`, `terreno`, `atividade`, `cultura`,
`pecuaria`, `agua`, `irrigacao`, `energiaTrifasica`, `distanciaAsfaltoMax`,
`benfeitoria`, `documentacao`.

**Controle:** `ordenar` (`relevancia`, `recentes`, `preco-asc`, `preco-desc`,
`area-asc`, `area-desc`, `destaque`), `pagina`, `tamanho`.

Exemplos:

```bash
curl "http://localhost:8080/api/v1/public/properties?categoria=RURAL&pecuaria=true"
curl "http://localhost:8080/api/v1/public/properties?areaHaMin=100&agua=Rio&documentacao=SIGEF"
curl "http://localhost:8080/api/v1/public/properties?categoria=URBANO&dormitorios=3&ordenar=preco-asc"
```

## Decisões relevantes

**A URL é a fonte de verdade da busca.** Todo filtro navega e reescreve a query
string: os parâmetros ficam preservados ao compartilhar o link, o botão voltar
funciona e a paginação usa links `<a>` reais, rastreáveis.

**SEO nasce no servidor.** As rotas públicas são renderizadas no SSR, então o
HTML já chega com anúncios, `title`, `description`, canonical, Open Graph e
JSON-LD (`RealEstateAgent`, `WebSite`, `ItemList`, `Residence`, `BreadcrumbList`).
O backend informa em `seo.indexavel` se a combinação de filtros merece indexação —
combinações finas recebem `noindex` e canonical apontando para `/imoveis`,
evitando a explosão de páginas de faceta descrita na seção 20.

**O navegador nunca fala direto com a API.** O servidor SSR expõe `/api` e
encaminha para o container do backend pela rede interna: sem CORS no caminho
comum e sem expor a URL interna.

**O cadastro é permissivo até a publicação.** Rascunhos aceitam campos em branco;
publicar exige título, descrição, tipo, finalidade, localização, corretor, preço
(ou indicação de consulta) e ao menos uma foto — e, no caso rural, área total e
município. Quando falta algo, a API responde dizendo exatamente o quê.

**O CEP preenche o endereço.** O cadastro consulta o
[ViaCEP](https://viacep.com.br) direto do navegador — o serviço libera CORS, então
não há intermediário. As respostas ficam em cache na sessão, um CEP inexistente
volta com HTTP 200 e corpo `{"erro": "true"}` (tratado como não encontrado) e
qualquer falha deixa os campos editáveis manualmente, sem travar o cadastro.
Dois detalhes do contrato: o município vem em `localidade`, e `complemento` traz a
faixa de numeração do CEP ("de 612 a 1510 - lado par") — não o complemento do
imóvel, por isso vira dica ao lado do número em vez de preencher o campo.

**Dados rurais são declaratórios.** A interface sempre indica a fonte e a data de
referência, e a documentação (CAR, CCIR, SIGEF, matrícula) aparece apenas como
"informada" — números e coordenadas exatas permanecem privados, conforme as
regras 24.5 e 24.17.

## Limitações conhecidas

- Os dados vivem em memória: reiniciar o backend zera os leads captados e
  restaura o catálogo original.
- Um imóvel inexistente em `/imoveis/{slug}` exibe a mensagem correta, mas ainda
  responde HTTP 200 — o status 404 só é aplicado a rotas fora do mapa. Resolver
  junto com a consulta ao catálogo no servidor.
- Os vocabulários dos filtros rurais (terreno, cultura, benfeitorias) estão
  fixos no frontend; no MVP completo virão do cadastro configurável da seção 10.
- As imagens são carregadas do Unsplash. Em produção entram o armazenamento
  compatível com S3 e o processamento de miniaturas descritos na seção 12.

## Próximos passos

1. Autenticação e perfis (ADMIN, GESTOR, CORRETOR, CONTEÚDO) protegendo `/admin`.
2. Substituir o repositório em memória por PostgreSQL/PostGIS com Flyway.
2. Prerenderizar as páginas institucionais estáveis e gerar `sitemap.xml`
   a partir do catálogo.
3. Autenticação, painel administrativo e cadastro de imóveis (etapas 1 a 3).
4. Mapa com localização aproximada e perímetro público simplificado.
5. Upload e processamento de mídia com armazenamento compatível com S3.
