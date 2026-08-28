# Histórico de desenvolvimento

Registro do que foi construído, das decisões tomadas e dos problemas encontrados
em cada sessão. Não é a transcrição literal da conversa: é o diário técnico do
trabalho, escrito para quem for continuar o projeto.

---

# Sessão de 28/07/2026

## 1. Ponto de partida

O diretório estava vazio. O pedido inicial foi um protótipo de site de imobiliária
em HTML, CSS e JavaScript, bonito e com boas práticas de SEO.

Foi entregue um site estático completo — home, listagem com filtros, página de
imóvel, sobre e contato — com design system próprio (tokens, tema claro/escuro),
dados estruturados JSON-LD, sitemap, robots.txt e manifesto.

Esse material continua no repositório em `legacy-prototipo/`, como referência de
design. Não é mais o frontend do projeto.

---

## 2. Evolução dos pedidos

A sessão foi incremental — cada pedido chegou depois de o anterior estar no ar.

| # | Pedido | Resultado |
|---|--------|-----------|
| 1 | Protótipo de site de imobiliária com SEO | Site estático em `legacy-prototipo/` |
| 2 | Rodar em container Docker | nginx servindo o estático |
| 3 | Nome passa a ser "Terra Nexo"; ajustar o logotipo | Rebranding completo + marca nova |
| 4 | Logotipo com referências rurais e urbanas | Emblema com colina, silo, torres e sol |
| 5 | "Logotipo mais trabalhado, mais profissional" | Emblema redesenhado com anel duplo, janelas vazadas e sulcos de lavoura |
| 6 | Seguir a especificação dos `.docx`: evoluir a landing e criar o módulo de busca, com backend e frontend em containers separados | Reescrita em Angular SSR + Spring Boot |
| 7 | Tela principal da administração e formulário de cadastro | Painel, listagem e cadastro urbano/rural |
| 8 | Publicar no GitLab | Repositório sincronizado |
| 9 | Campo CEP com integração ViaCEP | Busca automática de endereço |

---

## 3. Decisões estruturais

### 3.1 A virada para a stack da especificação

Os três documentos em `docs/` definem Angular com SSR, Java/Spring Boot,
PostgreSQL com PostGIS e containers separados. Antes de reescrever, foram
apresentadas duas escolhas ao cliente:

- **Frontend**: manter HTML/CSS/JS ou migrar para Angular 20 com SSR.
  → Escolhido **Angular**, para que o protótipo já nasça como base do MVP.
- **Backend**: dados em memória ou PostgreSQL/PostGIS desde já.
  → Escolhido **em memória**, isolado na camada de infraestrutura, para o
  protótipo subir em segundos. A troca por banco real não afeta as demais camadas.

### 3.2 A URL é a fonte de verdade da busca

Todo filtro navega e reescreve a query string, em vez de guardar estado interno.
Isso preserva os parâmetros ao compartilhar o link, faz o botão voltar funcionar e
permite que a paginação use links `<a>` reais — rastreáveis, como a seção 13 exige.

### 3.3 SEO nasce no servidor

As rotas públicas são renderizadas no SSR: o HTML já sai com anúncios, `title`,
`description`, canonical, Open Graph e JSON-LD. O backend informa em
`seo.indexavel` se a combinação de filtros merece indexação; combinações finas
recebem `noindex` e canonical apontando para `/imoveis`, evitando a explosão de
páginas de faceta descrita na seção 20.

### 3.4 O navegador não fala direto com a API

O servidor SSR expõe `/api` e encaminha para o container do backend pela rede
interna do Docker. Não há CORS no caminho comum e a URL interna não vaza.
A exceção é o ViaCEP, serviço externo que libera CORS e é chamado direto.

### 3.5 Dados rurais são declaratórios

A interface sempre indica a fonte e a data de referência. A documentação (CAR,
CCIR, SIGEF, matrícula) aparece apenas como "informada" — números e coordenadas
exatas permanecem privados, conforme as regras 24.5 e 24.17.

---

## 4. Problemas encontrados e como foram resolvidos

Esta seção existe porque os erros ensinam mais que os acertos.

**Erro de inferência genérica no comparador de preço.** O primeiro build do
backend falhou em `Comparator.nullsLast` — o tipo precisou ser declarado
explicitamente como `Comparator<BigDecimal>`.

**SSR entregando casca vazia.** Todas as rotas devolviam 16 KB idênticos. Os logs
do container mostraram `Header "host" with value "localhost:4000" is not allowed`:
o Angular 20 valida o host contra `security.allowedHosts`, que o scaffold cria
vazio. Depois de preencher a lista — incluindo as variantes com porta — o HTML
passou a sair renderizado, com os anúncios dentro.

**Regra de indexação contraditória.** Uma busca por `tipo=Fazenda` vinha marcada
como `noindex`, quando deveria ser indexável. A condição exigia lista de tipos
vazia e, ao mesmo tempo, permitia um tipo único — a segunda regra nunca era
alcançada. Corrigido removendo `tipos` da checagem de filtros finos.

**Selo invisível no tema escuro.** O badge de finalidade tinha fundo branco fixo e
cor de texto seguindo o tema: no escuro, texto claro sobre fundo claro. A cor do
texto passou a ser fixa, já que o fundo também é.

**Galeria com buracos.** O mosaico de cinco fotos deixava células vazias em
imóveis com menos imagens. Resolvido com `:has()`, adaptando o grid à quantidade.

**Soft 404.** Rotas inexistentes respondiam HTTP 200 com a página de erro. O
servidor SSR passou a comparar o caminho com as rotas conhecidas e devolver 404 de
verdade — o rastreador precisa do código correto.

**Plurais quebrados nos títulos.** "Sala comercials", "Área de reflorestamentos".
Tipos compostos não seguem regra simples; entrou uma tabela explícita de plurais.

**O `complemento` do ViaCEP não é o complemento do imóvel.** Descoberto no teste
visual: a API devolve a faixa de numeração do logradouro ("de 612 a 1510 - lado
par"). Preencher o campo com isso faria o corretor publicar um complemento errado.
Virou dica ao lado do número, sem preencher o campo.

**Contrato do ViaCEP verificado antes de codificar.** CEP inexistente responde
HTTP 200 com `{"erro": "true"}` — string, não booleano. O código aceita as duas
formas, porque a API já usou ambas.

---

## 5. Validações executadas

Nada foi dado como pronto sem verificação:

- **Filtros da busca**: 20 combinações testadas contra a API, conferindo as
  contagens uma a uma — categoria, finalidade, tipo, município, preço, área,
  ambientes, e os rurais (pecuária, água, cultura, irrigação, documentação,
  distância do asfalto, benfeitorias, terreno).
- **Facetas**: cada dimensão conta sobre o conjunto filtrado pelas demais, de modo
  que selecionar um valor não zera as outras opções do mesmo grupo.
- **Ordenação**: preço decrescente confirmado (R$ 198 mi → R$ 24 mi), com
  "Consulte-nos" sempre ao final, já que não há como posicioná-lo com honestidade.
- **SEO por rota**: title, description, canonical e robots conferidos no HTML
  entregue pelo servidor, incluindo o `noindex` das combinações finas.
- **Status HTTP**: 200 nas rotas conhecidas, 404 em rota inexistente.
- **Imagens**: as 35 URLs do catálogo verificadas — todas respondem 200.
- **Paleta do painel**: validada pelo script de daltonismo e contraste nos temas
  claro e escuro antes de entrar no CSS.
- **Regras de publicação**: publicar sem descrição, foto, corretor e preço é
  recusado com a lista do que falta; o mesmo cadastro como rascunho é aceito, não
  aparece na busca pública e passa a contar no painel.
- **Cadastro rural completo**: 300 ha convertidos para 3.000.000 m², código
  `RUR-000212` gerado, resumo montado automaticamente.
- **CEP**: `01310-100` preencheu Avenida Paulista, Bela Vista, São Paulo e SP,
  com o foco indo para o número; CEP malformado recusado pelo backend.

---

## 6. Estado final

**Portal público** (`http://localhost:4000`)
Landing com as doze seções da especificação, módulo de resultado de busca com
filtros urbanos e rurais, página de imóvel, 404 e sitemap gerado do catálogo.

**Administração** (`http://localhost:4000/admin`)
Painel com indicadores, pendências acionáveis e composição da carteira; listagem
com filtros administrativos; cadastro com blocos condicionais por categoria e
busca de endereço por CEP.

**API** (`http://localhost:18080/docs`)
Endpoints públicos de imóveis, regiões, corretores e leads; endpoints
administrativos de painel, listagem e cadastro.

**Execução**: `docker compose up -d --build` sobe os dois containers.

**Commits no GitLab** (`gitlab.com/juniorcouto/terranexo`):

| Commit | Conteúdo |
|--------|----------|
| `31f1d25` | Portal público e API — 115 arquivos |
| `4ab89c7` | Ambiente administrativo — 19 arquivos |
| `1fe770e` | Busca de endereço por CEP — 10 arquivos |

---

## 7. Pendências conhecidas

Registradas com honestidade, para não haver surpresa:

1. **`/admin` está sem autenticação**, tela e API. Adequado para demonstrar o
   protótipo, inaceitável em ambiente exposto. É a etapa 1 do plano.
2. **Dados em memória**: reiniciar o backend zera os leads e restaura o catálogo.
3. **Imóvel inexistente** em `/imoveis/{slug}` mostra a mensagem correta, mas
   ainda responde HTTP 200.
4. **Vocabulários rurais fixos no frontend** (terreno, cultura, benfeitorias);
   deveriam vir do cadastro configurável da seção 10.
5. **Imagens do Unsplash**: falta o armazenamento compatível com S3 e o
   processamento de miniaturas da seção 12.
6. **Sem testes automatizados.** As validações desta sessão foram manuais, contra
   a API e o HTML renderizado. A seção 28 pede suíte própria.

---

## 8. Próximos passos sugeridos

1. Autenticação e perfis protegendo `/admin`.
2. PostgreSQL com PostGIS e Flyway, substituindo o repositório em memória.
3. Upload e processamento de mídia.
4. Módulo de leads completo: distribuição, funil, interações e exportação.
5. Mapa com localização aproximada e perímetro público simplificado.
6. Testes automatizados e pipeline de CI/CD.

---

# Sessão de 27/08/2026

## 1. Evolução dos pedidos

| # | Pedido | Resultado |
|---|--------|-----------|
| 1 | Base de prospecção (CRM) de empresas agro/imobiliárias/irrigação/silos/leilão/laticínios nas 7 cidades do projeto | Banco SQLite normalizado em `docs/prospeccao/db/` |
| 2 | Integrar a prospecção como módulo real do Terra Nexo, em memória, com API + tela admin + sugestão cruzada "parceiros para este imóvel" | Módulo `partner` completo (domain/application/api/infrastructure) |
| 3 | Pesquisar integração com iList (RE/MAX/Attria) — **sem implementar** | Pesquisa feita, registrada em memória, decisão de adiar para versão futura |
| 4 | Trocar a marca genérica pela logo real e as fotos genéricas pelas 7 cidades do projeto | `brand-mark.ts` + assets em `frontend/public/brand/`; catálogo de imóveis reescrito |
| 5 | Remover imóveis fora das 7 cidades; identidade do admin = "Luiz Medeiros - Corretor Remax Prime" | `PropertySeed.java` reescrito; `admin-shell.ts` atualizado |
| 6 | CRUD completo (criar/ler/atualizar/excluir) para Imóveis e Parceiros — API + telas admin | Implementado nos dois módulos, backend e frontend |
| 7 | Bug: cadastro de imóvel rejeitava com "Revise os campos destacados..." sem indicar o quê | Investigado e corrigido (ver §3) |
| 8 | Mesmo bug relatado no cadastro de Parceiros; pedido de verificação sem inventar testes | Investigado e corrigido; auditoria de **todos** os formulários do admin |
| 9 | Testar tudo direto no navegador via Claude in Chrome | Pareamento da extensão não foi concluído nesta sessão (ver §6) |
| 10 | Bug: campo de status do Parceiro não atualizava, mesmo erro genérico | Investigado e corrigido (ver §3) |
| 11 | Inspeção de qualidade/redundância/performance de todo o código da sessão | Feita; um achado corrigido, um documentado como decisão em aberto (ver §5) |

---

## 2. Decisões estruturais

### 2.1 Prospecção como módulo de domínio próprio

O módulo `partner` foi criado espelhando a estrutura de `property`
(domain/application/api/infrastructure), e não como uma extensão de `property`
— parceiro comercial e imóvel são conceitos diferentes, com ciclos de vida
próprios (funil de prospecção vs. situação de publicação). A sugestão cruzada
("parceiros para este imóvel") vive em `PartnerSuggestionService`, que depende
das duas camadas de domínio sem que nenhuma dependa da outra.

### 2.2 iList/RE/MAX: pesquisado e adiado

A integração com o CRM iList (RE/MAX, via Attria) foi pesquisada a fundo —
inclusive o caminho de exportação seletiva de imóveis/contatos via Zapier — mas
o cliente decidiu adiar para uma versão futura. A pesquisa está registrada em
`docs/integracao-ilist/` e na memória persistente do projeto, para não ser
refeita à toa quando o assunto voltar.

### 2.3 Identidade do admin é fixa, não autenticada

Sem módulo de login, o nome "Luiz Medeiros" aparece hardcoded em dois lugares
com propósitos diferentes: o cabeçalho do painel (`admin-shell.ts`, identidade
de quem está usando o sistema) e, agora, o valor padrão do campo "Responsável"
no formulário de atualização de status de parceiro (`partner-detail.ts`) — ver
§3. Isso é deliberado enquanto não existe autenticação; a lista pública de
corretores (`PublicBrokerController`, 4 nomes fictícios) é um conceito
diferente — o "time" que aparece no site público — e não foi alterada (ver §5).

---

## 3. Problemas encontrados e como foram resolvidos

**Cadastro de imóvel rejeitado silenciosamente.** `areaValor` no Angular usava
`Validators.min(1)`, enquanto o backend aceita qualquer área positiva
(`@Positive`, > 0). Um sítio de 0,5 ha era bloqueado no navegador antes mesmo
de chegar à API, sem indicar por quê. Corrigido para `Validators.min(0.01)`,
espelhando o backend.

**O erro genérico não apontava o campo culpado.** Vários campos obrigatórios —
inclusive dentro de listas dinâmicas (culturas, benfeitorias, titulares,
contatos) — não tinham nenhuma indicação visual de erro. Qualquer bloqueio de
validação, seja qual fosse a causa, resultava na mesma mensagem genérica sem
pista de qual campo revisar. Corrigido nos três formulários do admin
(`property-form`, `partner-form`, `partner-detail`) com o padrão já usado no
projeto (`is-invalid` + `field__error`) e um novo método
`focarPrimeiroInvalido()`, que rola a tela e foca o primeiro campo inválido —
inclusive dentro de `FormArray`s.

**Duas divergências adicionais de rigor cliente/servidor**, achadas ao
comparar sistematicamente os `Validators` do Angular com as anotações Jakarta
do backend: `uf` sem `minLength(2)` (backend exige exatamente 2 caracteres) em
`property-form` e `partner-form`; `cep` sem validação de formato em
`property-form`; `observacoes` sem `maxLength(2000)` em `partner-form`. Todas
corrigidas.

**Status do parceiro "não atualizava".** Sintoma reportado: selecionar um novo
status e salvar devolvia "Revise os campos destacados...". Causa: o campo
"Responsável" (também obrigatório) começava vazio, sem nenhuma pista visual de
que era obrigatório até a tentativa de salvar — o usuário preenchia só o
status, e o formulário inteiro ficava inválido por causa do outro campo.
Corrigido pré-preenchendo "Responsável" com a identidade fixa do admin
("Luiz Medeiros"), mantendo o campo editável.

**Duplicação em `PartnerService` corrigida por inspeção proativa** (não foi
bug reportado, achado na auditoria de qualidade do fim da sessão — §5): os
métodos `criar()` e `atualizar()` montavam o record `Partner` (16 argumentos
posicionais) quase de forma idêntica, divergindo só em status/criadoEm/
histórico. Extraído um método privado `construir(...)`, no mesmo padrão que
`PropertyAdminService` já usava (`popular()`) — mas que não tinha sido
replicado em `PartnerService` quando o módulo foi criado.

---

## 4. Validações executadas

- **CRUD completo via API real**, nos dois módulos, reproduzindo os cenários
  exatos dos bugs relatados: criar com área 0,5 ha (imóvel), atualizar status
  sem preencher responsável (parceiro), editar e excluir os dois tipos de
  registro, com verificação de erro idempotente em exclusão repetida.
- **Paridade de validação cliente/servidor**: testes negativos via curl
  confirmando que o backend rejeita exatamente o que o Angular agora também
  rejeita (UF de 1 caractere, CEP malformado, responsável em branco) — sem
  bloqueio silencioso e sem aceitar no cliente algo que o servidor recusa.
- **Bundle do container em produção inspecionado diretamente** (não só o
  código-fonte) para confirmar que as correções realmente estavam servidas —
  `grep` por `focarPrimeiroInvalido`, pelas mensagens de erro e pelo valor
  padrão "Luiz Medeiros" dentro dos arquivos `.js` do container rodando.
- **Rebuild completo** (`docker compose build` + `up -d --force-recreate`)
  confirmado saudável (`healthy`) antes e depois da refatoração de
  `PartnerService`, com o mesmo roteiro de CRUD reexecutado após a mudança.
- **Limpeza de dados de teste**: todos os registros criados durante os testes
  foram excluídos via API ao final; um resíduo de uma rodada de testes
  anterior (`RUR-000219`, fora das 7 cidades do projeto) foi encontrado e
  removido durante a limpeza pedida nesta sessão.

---

## 5. Auditoria de qualidade (fim de sessão)

Feita uma inspeção do código construído/alterado nesta sessão, focada em
redundância, código morto e riscos de performance em produção.

**Corrigido:** duplicação em `PartnerService.criar()`/`atualizar()` (§3).

**Decisão em aberto, não uma correção unilateral:** a lista `CORRETORES`
hardcoded em `property-form.ts` (4 nomes fictícios: Marina Alencar, Rafael
Ortiz, Juliana Prado, Eduardo Salles) não inclui "Luiz Medeiros" — ou seja, ao
publicar um imóvel pelo painel, o admin real não consegue se selecionar como
corretor responsável, só um dos quatro fictícios. Isso pode ser intencional
(a lista representa o "time" fictício que aparece publicamente em
`PublicBrokerController`, um conceito de produto separado da identidade de
quem está logado no admin) ou pode ser um descuido. **Não foi alterado** —
fica registrado para decisão do cliente.

**Verificado e considerado aceitável, sem necessidade de ação agora:**
- Repositórios em memória (`InMemoryPropertyRepository`,
  `InMemoryPartnerRepository`): buscas e `save()`/`deleteById()` são O(n) sobre
  `CopyOnWriteArrayList`. Irrelevante na escala atual (dezenas de registros);
  a própria classe já documenta que a troca por PostgreSQL não afeta as
  demais camadas — é o caminho natural para produção, não um bug a corrigir
  agora.
- Duplicação pontual entre `property-form.ts` e `partner-form.ts` no frontend
  (`invalidoItem`, `focarPrimeiroInvalido`, ~15 linhas cada) — aceitável com
  apenas 2 ocorrências; vale extrair um utilitário compartilhado se um
  terceiro formulário repetir o padrão, não antes.
- `property-form.ts` com 711 linhas — grande, mas por ser genuinamente um
  formulário complexo (urbano + rural, múltiplos `FormArray`s), não por
  acúmulo de código morto. Candidato a dividir em subcomponentes numa sessão
  futura, sem urgência.
- Nenhum `TODO`/`FIXME` pendente encontrado no código desta sessão.
- Controllers REST (`AdminPropertyController`, `AdminPartnerController`)
  seguem os mesmos padrões de status HTTP (201 no create, 204 no delete, erro
  estruturado nos demais casos) — sem inconsistência entre os dois módulos.

---

## 6. Pendência aberta: Claude in Chrome

Foi pedido teste via navegador real (Claude in Chrome). A extensão está
instalada, mas `/chrome` mostra **Status: Disabled** de forma persistente,
mesmo após reinstalar, reiniciar o Chrome e o Claude Code várias vezes.
Tentativas feitas: reinício completo do Chrome (não só da sessão), reconexão
via `/chrome`, verificação de que a extensão aparece "habilitada" em
`chrome://extensions`. Não foi possível concluir o pareamento nesta sessão —
o roteiro de verificação usado como alternativa (build Docker real + inspeção
do bundle do container + chamadas de API reproduzindo os cenários exatos dos
formulários) cobre a lógica de validação e o contrato cliente/servidor, mas
não substitui um clique real no navegador.

---

## 7. Estado final desta sessão

**Módulo `partner`** completo: domínio, CRUD via API
(`/api/v1/admin/partners`), telas admin (listagem, detalhe com histórico de
funil, cadastro/edição), sugestão cruzada de parceiros por imóvel.

**CRUD de imóveis e parceiros** — API e telas admin, com paridade de
validação cliente/servidor conferida nos três formulários que existem hoje.

**Catálogo público** limitado às 7 cidades do projeto (Unaí, Paracatu,
Formosa, Luziânia, Cristalina, Catalão, João Pinheiro); marca própria
(`brand-mark.ts` + assets gerados da logo real) substituindo o placeholder.

**Execução**: `docker compose up -d --build` sobe os dois containers, ambos
`healthy` ao final desta sessão.

---

## 8. Próximos passos sugeridos

1. Decidir o que fazer com a lista `CORRETORES` do formulário de imóvel (§5) —
   incluir Luiz Medeiros, ou deixar só os corretores fictícios do time público.
2. Retomar o pareamento do Claude in Chrome (§6) para permitir teste de
   clique real, se ainda for prioridade.
3. As pendências gerais de produção continuam as da sessão anterior — §7 de
   28/07: autenticação em `/admin`, PostgreSQL/PostGIS substituindo o
   repositório em memória, upload de mídia, testes automatizados.
4. Se o cliente reabrir o assunto, a pesquisa de integração iList/RE/MAX já
   está pronta em `docs/integracao-ilist/` — não precisa refazer, só retomar.
