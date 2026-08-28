package br.com.terranexo.partner.api;

import br.com.terranexo.partner.api.dto.PartnerCardResponse;
import br.com.terranexo.partner.api.dto.PartnerDetailResponse;
import br.com.terranexo.partner.domain.Partner;
import org.springframework.stereotype.Component;

@Component
public class PartnerMapper {

    public PartnerCardResponse toCard(Partner p) {
        Partner.Titular titular = p.titularPrincipal();
        return new PartnerCardResponse(
                p.id(), p.municipio(), p.uf(), p.setor().name(), p.setor().rotulo(),
                p.nomeFantasia(), p.razaoSocial(), p.cnpj(),
                titular != null ? titular.nome() : null,
                p.contato(Partner.ContatoTipo.TELEFONE_FIXO),
                p.contato(Partner.ContatoTipo.TELEFONE_CELULAR),
                p.contato(Partner.ContatoTipo.WHATSAPP),
                p.contato(Partner.ContatoTipo.EMAIL),
                p.site(), p.status().name(), p.status().rotulo(), p.atualizadoEm());
    }

    public PartnerDetailResponse toDetail(Partner p) {
        String enderecoCompleto = p.endereco() != null
                ? p.endereco().enderecoCompleto(p.municipio(), p.uf())
                : p.municipio() + "-" + p.uf();

        return new PartnerDetailResponse(
                p.id(), p.municipio(), p.uf(), p.setor().name(), p.setor().rotulo(),
                p.nomeFantasia(), p.razaoSocial(), p.cnpj(), p.situacaoCadastral(),
                enderecoCompleto,
                p.endereco() != null ? p.endereco().logradouro() : null,
                p.endereco() != null ? p.endereco().numero() : null,
                p.endereco() != null ? p.endereco().complemento() : null,
                p.endereco() != null ? p.endereco().bairro() : null,
                p.endereco() != null ? p.endereco().cep() : null,
                p.site(),
                p.geolocalizacao() != null ? p.geolocalizacao().mapsUrl() : null,
                p.titulares() == null ? java.util.List.of() : p.titulares().stream()
                        .map(t -> new PartnerDetailResponse.Titular(t.nome(), t.cpfMascarado(),
                                t.qualificacao(), t.principal()))
                        .toList(),
                p.contatos() == null ? java.util.List.of() : p.contatos().stream()
                        .map(c -> new PartnerDetailResponse.Contato(c.tipo().name(), rotuloTipo(c.tipo()),
                                c.valor(), c.principal()))
                        .toList(),
                p.status().name(), p.status().rotulo(),
                p.fonteInformacao(), p.dataColeta(), p.observacoes(), p.criadoEm(), p.atualizadoEm(),
                p.historicoStatus() == null ? java.util.List.of() : p.historicoStatus().stream()
                        .map(e -> new PartnerDetailResponse.StatusEvento(e.status().name(), e.status().rotulo(),
                                e.data(), e.responsavel(), e.observacao()))
                        .toList());
    }

    private String rotuloTipo(Partner.ContatoTipo tipo) {
        return switch (tipo) {
            case TELEFONE_FIXO -> "Telefone fixo";
            case TELEFONE_CELULAR -> "Telefone celular";
            case WHATSAPP -> "WhatsApp";
            case EMAIL -> "E-mail";
        };
    }
}
