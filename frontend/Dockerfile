# ---------- build ----------
FROM node:22-alpine AS build
WORKDIR /app

# Camada de dependências: só reinstala quando o manifesto muda
COPY package.json package-lock.json* ./
RUN npm install --no-audit --no-fund

COPY . .
RUN npm run build

# ---------- runtime ----------
FROM node:22-alpine AS runtime
WORKDIR /app

ENV NODE_ENV=production \
    PORT=4000

# Só o necessário para o SSR: bundle do servidor, estáticos e o express
COPY --from=build /app/dist ./dist
COPY --from=build /app/package.json ./package.json
RUN npm install --omit=dev --no-audit --no-fund express \
 && addgroup -S app && adduser -S app -G app \
 && chown -R app:app /app

USER app
EXPOSE 4000

HEALTHCHECK --interval=30s --timeout=3s --start-period=15s \
  CMD wget -qO- http://localhost:4000/healthz >/dev/null 2>&1 || exit 1

# Em hospedagens que injetam o host público (Render: RENDER_EXTERNAL_HOSTNAME),
# libera esse host na validação do @angular/ssr quando NG_ALLOWED_HOSTS não vem definido.
CMD ["sh", "-c", "if [ -z \"$NG_ALLOWED_HOSTS\" ] && [ -n \"$RENDER_EXTERNAL_HOSTNAME\" ]; then export NG_ALLOWED_HOSTS=\"$RENDER_EXTERNAL_HOSTNAME\"; fi; exec node dist/terranexo-web/server/server.mjs"]
