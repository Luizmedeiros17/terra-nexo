# Terra Nexo — protótipo estático servido por nginx
FROM nginx:1.27-alpine

LABEL org.opencontainers.image.title="Terra Nexo — protótipo" \
      org.opencontainers.image.description="Site protótipo de imobiliária urbana e rural (HTML, CSS e JS)" \
      org.opencontainers.image.licenses="MIT"

# Configuração do servidor
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/terranexo.conf

# Conteúdo do site
COPY index.html imoveis.html imovel.html sobre.html contato.html \
     robots.txt sitemap.xml site.webmanifest /usr/share/nginx/html/
COPY assets/ /usr/share/nginx/html/assets/

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s \
  CMD wget -qO- http://localhost/ >/dev/null 2>&1 || exit 1
