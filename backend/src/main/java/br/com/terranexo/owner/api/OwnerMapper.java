package br.com.terranexo.owner.api;

import br.com.terranexo.owner.api.dto.OwnerCardResponse;
import br.com.terranexo.owner.api.dto.OwnerDetailResponse;
import br.com.terranexo.owner.domain.Owner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OwnerMapper {

    public OwnerCardResponse toCard(Owner o) {
        return new OwnerCardResponse(o.id(), o.tipoPessoa().name(), rotuloTipo(o.tipoPessoa()), o.nome(),
                o.documento(), o.endereco().municipio(), o.endereco().uf(), o.telefone(), o.email(),
                o.imoveisVinculados() == null ? 0 : o.imoveisVinculados().size(), o.atualizadoEm().toString());
    }

    public OwnerDetailResponse toDetail(Owner o) {
        return new OwnerDetailResponse(o.id(), o.tipoPessoa().name(), rotuloTipo(o.tipoPessoa()), o.nome(),
                o.documento(), o.email(), o.telefone(), o.telefoneAlternativo(),
                o.endereco().enderecoCompleto(), o.endereco().municipio(), o.endereco().uf(),
                o.endereco().logradouro(), o.endereco().numero(), o.endereco().complemento(),
                o.endereco().bairro(), o.endereco().cep(),
                o.imoveisVinculados() == null ? List.of() : o.imoveisVinculados(), o.observacoes(),
                o.criadoEm().toString(), o.atualizadoEm().toString());
    }

    private String rotuloTipo(Owner.TipoPessoa tipo) {
        return tipo == Owner.TipoPessoa.FISICA ? "Pessoa física" : "Pessoa jurídica";
    }
}
