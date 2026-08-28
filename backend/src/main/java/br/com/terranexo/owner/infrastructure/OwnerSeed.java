package br.com.terranexo.owner.infrastructure;

import br.com.terranexo.owner.domain.Owner;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/** Carga inicial de proprietários de exemplo, dentro das 7 cidades atendidas pelo Terra Nexo. */
final class OwnerSeed {

    private OwnerSeed() {
    }

    static List<Owner> carregar() {
        OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.UTC);
        return List.of(
                new Owner("prop-001", Owner.TipoPessoa.FISICA, "Antônio Ferreira Campos", "123.456.789-00",
                        "antonio.campos@exemplo.com", "(38) 99811-2233", null,
                        new Owner.Address("Unaí", "MG", "Rua das Palmeiras", "120", null, "Centro", "38610-000"),
                        List.of(), "Proprietário de fazenda de pecuária, herdada da família.",
                        agora, agora),
                new Owner("prop-002", Owner.TipoPessoa.JURIDICA, "Agropecuária Cristalina Ltda", "12.345.678/0001-90",
                        "contato@agrocristalina.com.br", "(61) 99722-4455", "(61) 3612-8800",
                        new Owner.Address("Cristalina", "GO", "Rodovia GO-330, km 12", "s/n", null, "Zona Rural", "73850-000"),
                        List.of(), null,
                        agora, agora),
                new Owner("prop-003", Owner.TipoPessoa.FISICA, "Marta Souza Lima", "987.654.321-00",
                        "marta.lima@exemplo.com", "(61) 99633-7788", null,
                        new Owner.Address("Luziânia", "GO", "Avenida Goiás", "450", "Apto 302", "Jardim Ingá", "72800-000"),
                        List.of(), "Proprietária de imóvel urbano, mora em Brasília.",
                        agora, agora));
    }
}
