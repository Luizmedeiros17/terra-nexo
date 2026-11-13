# Deploy gratuito no Render — passo a passo

Este guia publica o Terra Nexo na internet usando o plano gratuito do
[Render](https://render.com). Leva cerca de 20 minutos, a maior parte esperando
o primeiro build.

O Render lê o arquivo `render.yaml`, na raiz do repositório, e cria sozinho os
dois serviços do projeto:

| Serviço | O que é | Endereço esperado |
| --- | --- | --- |
| `terranexo-web` | Portal (Angular SSR) | https://terranexo-web.onrender.com |
| `terranexo-api` | API (Spring Boot) | https://terranexo-api.onrender.com |

## Antes de começar

- O `render.yaml` e o ajuste do `frontend/Dockerfile` precisam estar na branch
  `master` do repositório `Luizmedeiros17/terra-nexo`. Se chegaram por pull
  request, faça o merge primeiro.
- Você precisa estar logado no GitHub com a conta dona do repositório.

## 1. Criar a conta no Render

1. Acesse https://dashboard.render.com/register.
2. Clique em **GitHub** para se cadastrar com a conta do GitHub (é o caminho
   mais curto, porque já vincula as duas contas).
3. Autorize o Render quando o GitHub pedir.
4. Confirme o e-mail, se o Render enviar uma mensagem de verificação.
5. Se aparecer um questionário de boas-vindas, responda ou pule. Na escolha de
   plano do workspace, fique com o **Hobby** (gratuito).

## 2. Criar o Blueprint

1. No painel, clique em **New** (canto superior direito) e escolha **Blueprint**.
2. Em **Connect a repository**, clique em **GitHub** / **Configure account**.
3. Na tela do GitHub, escolha **Only select repositories**, marque
   `terra-nexo` e clique em **Install** (ou **Save**).
4. De volta ao Render, clique em **Connect** ao lado de `terra-nexo`.
5. Preencha:
   - **Blueprint Name:** `terra-nexo`
   - **Branch:** `master`
6. O Render mostra os dois serviços que vai criar (`terranexo-api` e
   `terranexo-web`), ambos no plano **Free**. Confira e clique em
   **Deploy Blueprint** (em algumas versões da tela, **Apply**).

## 3. Acompanhar o primeiro build

1. O Render abre a página do Blueprint com os dois serviços em andamento.
2. Clique em cada serviço para ver os logs. O primeiro build demora de 5 a 10
   minutos por serviço (Maven e npm baixam todas as dependências).
3. O deploy terminou quando os dois serviços mostram o selo **Live**.

## 4. Conferir os endereços

Abra a página de cada serviço e veja a URL no topo.

- Se as URLs forem exatamente `terranexo-web.onrender.com` e
  `terranexo-api.onrender.com`, pule para o passo 5.
- Se o Render acrescentou um sufixo (por exemplo `terranexo-api-x1y2`), o nome
  já estava em uso e é preciso corrigir duas variáveis:
  1. Em `terranexo-web` > **Environment**, altere `API_URL` para a URL real da
     API e `SITE_URL` para a URL real do portal.
  2. Em `terranexo-api` > **Environment**, altere
     `TERRANEXO_CORS_ALLOWED_ORIGINS` para a URL real do portal.
  3. Clique em **Save Changes** em cada um; o Render refaz o deploy sozinho.

## 5. Testar

| Endereço | Deve mostrar |
| --- | --- |
| `https://terranexo-api.onrender.com/actuator/health` | `{"status":"UP"...}` |
| `https://terranexo-web.onrender.com` | Landing page com os imóveis em destaque |
| `https://terranexo-web.onrender.com/imoveis` | Resultado de busca |
| `https://terranexo-web.onrender.com/admin` | Painel administrativo |

Abra primeiro o endereço da API e espere responder; só depois abra o portal.

## O que esperar do plano gratuito

- **Hibernação:** cada serviço dorme após cerca de 15 minutos sem acesso. O
  acesso seguinte leva em torno de um minuto. Se o portal abrir sem imóveis ou
  com a mensagem "Serviço de imóveis indisponível", a API ainda está acordando:
  aguarde um minuto e recarregue.
- **Dados voláteis:** os dados ficam em memória. Imóveis cadastrados, leads e
  fotos enviadas pelo painel somem a cada hibernação ou novo deploy, e o
  sistema volta aos dados de exemplo.
- **Painel aberto:** as rotas `/admin` não têm login nesta fase. Qualquer pessoa
  com o link consegue acessar e cadastrar; não coloque dados reais.
- **Cota mensal:** o plano dá 750 horas de instância por mês para o workspace,
  somando os dois serviços. Como eles hibernam quando ociosos, isso costuma
  bastar para demonstrações.

## Atualizações futuras

Todo push na branch `master` dispara um novo deploy automaticamente. Para
publicar manualmente, abra o serviço e use **Manual Deploy > Deploy latest
commit**.

## Problemas comuns

| Sintoma | Causa provável | O que fazer |
| --- | --- | --- |
| Portal responde `400 Bad Request` | Acesso por um domínio que o SSR não reconhece (domínio próprio, por exemplo) | Em `terranexo-web` > **Environment**, crie `NG_ALLOWED_HOSTS` com os domínios separados por vírgula |
| Portal abre, mas sem imóveis | API hibernando ou `API_URL` errada | Abra `/actuator/health` da API; se responder, confira `API_URL` (passo 4) |
| Build da API falha por falta de memória | Limite do build gratuito | Use **Manual Deploy > Clear build cache & deploy** e tente de novo |
| Serviço fica em **Deploy failed** | Erro no build ou no health check | Abra a aba **Logs** do serviço e leia as últimas linhas |
