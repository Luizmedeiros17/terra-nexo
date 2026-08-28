package br.com.terranexo.lead.api.dto;

import java.time.OffsetDateTime;

public record LeadDetailResponse(
        String id,
        String protocolo,
        String nome,
        String email,
        String telefone,
        String mensagem,
        String melhorHorario,
        String origem,
        String origemRotulo,
        String imovelCodigo,
        String paginaOrigem,
        String utmSource,
        String utmMedium,
        String utmCampaign,
        boolean consentimento,
        String etapa,
        String etapaRotulo,
        String responsavel,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {
}
