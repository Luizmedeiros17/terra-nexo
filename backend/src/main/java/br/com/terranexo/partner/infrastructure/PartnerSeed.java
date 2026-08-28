package br.com.terranexo.partner.infrastructure;

import br.com.terranexo.partner.domain.Partner;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Carga inicial da base de prospecção a partir de {@code partners-seed.json}
 * (classpath), gerado por docs/prospeccao/db/export_backend_seed.py a partir do
 * banco normalizado docs/prospeccao/db/prospeccao.db.
 *
 * <p>Substituível por Flyway + tabelas {@code prospeccao.*} sem alterar as
 * demais camadas — ver V1__create_prospeccao_schema.sql.</p>
 */
final class PartnerSeed {

    private static final String RECURSO = "/partners-seed.json";

    private PartnerSeed() {
    }

    static List<Partner> carregar() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try (InputStream in = PartnerSeed.class.getResourceAsStream(RECURSO)) {
            if (in == null) {
                throw new IllegalStateException("Recurso " + RECURSO + " não encontrado no classpath");
            }
            Partner[] parceiros = mapper.readValue(in, Partner[].class);
            return List.of(parceiros);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao carregar " + RECURSO, e);
        }
    }
}
