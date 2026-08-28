package br.com.terranexo.lead.api.dto;

import java.time.OffsetDateTime;

public record LeadCardResponse(
        String id,
        String protocolo,
        String nome,
        String email,
        String telefone,
        String origem,
        String origemRotulo,
        String imovelCodigo,
        String etapa,
        String etapaRotulo,
        String responsavel,
        OffsetDateTime criadoEm) {
}
