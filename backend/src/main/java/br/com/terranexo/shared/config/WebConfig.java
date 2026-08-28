package br.com.terranexo.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS restrito às origens do portal (seção 21) e metadados da OpenAPI. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${terranexo.cors.allowed-origins}")
    private String[] origensPermitidas;

    @Value("${terranexo.midias.diretorio:/tmp/terranexo-midias}")
    private String diretorioMidias;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origensPermitidas)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept")
                .maxAge(3600);
    }

    /** Serve os arquivos enviados pelo gerenciador de mídia (fotos e vídeos dos imóveis). */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String local = diretorioMidias.endsWith("/") ? diretorioMidias : diretorioMidias + "/";
        registry.addResourceHandler("/api/v1/midias/**")
                .addResourceLocations("file:" + local)
                .setCachePeriod(3600);
    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Terra Nexo — API pública")
                .version("0.1.0")
                .description("Endpoints públicos do portal: pesquisa de imóveis urbanos e rurais, "
                        + "regiões, corretores e captação de leads. MVP conforme especificação de 28/07/2026.")
                .contact(new Contact().name("Terra Nexo").email("contato@terranexo.com.br")));
    }
}
