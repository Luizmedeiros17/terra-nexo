package br.com.terranexo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Plataforma Terra Nexo — API pública do MVP.
 *
 * <p>Monólito modular conforme a seção 5 da especificação: os pacotes de negócio
 * ({@code property}, {@code lead}, {@code broker}, {@code region}) mantêm fronteiras
 * explícitas e cada um se organiza em {@code domain}, {@code application},
 * {@code api} e {@code infrastructure}.</p>
 *
 * <p>Nesta fase de protótipo a persistência é em memória; a troca para
 * PostgreSQL/PostGIS acontece apenas na camada {@code infrastructure}.</p>
 */
@SpringBootApplication
public class TerraNexoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TerraNexoApiApplication.class, args);
    }
}
