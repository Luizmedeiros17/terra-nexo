package br.com.terranexo.lead.api;

import br.com.terranexo.lead.api.dto.LeadCardResponse;
import br.com.terranexo.lead.api.dto.LeadDetailResponse;
import br.com.terranexo.lead.domain.Lead;
import org.springframework.stereotype.Component;

@Component
public class LeadMapper {

    public LeadCardResponse toCard(Lead l) {
        return new LeadCardResponse(l.id(), l.protocolo(), l.nome(), l.email(), l.telefone(),
                l.origem().name(), l.origem().rotulo(), l.imovelCodigo(),
                l.etapa().name(), l.etapa().rotulo(), l.responsavel(), l.criadoEm());
    }

    public LeadDetailResponse toDetail(Lead l) {
        return new LeadDetailResponse(l.id(), l.protocolo(), l.nome(), l.email(), l.telefone(), l.mensagem(),
                l.melhorHorario(), l.origem().name(), l.origem().rotulo(), l.imovelCodigo(), l.paginaOrigem(),
                l.utmSource(), l.utmMedium(), l.utmCampaign(), l.consentimento(),
                l.etapa().name(), l.etapa().rotulo(), l.responsavel(), l.criadoEm(), l.atualizadoEm());
    }
}
