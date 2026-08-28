package br.com.terranexo.broker.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record BrokerDetailResponse(
        String id,
        String slug,
        String nome,
        String cargo,
        String creci,
        String foto,
        String bio,
        List<String> especialidades,
        List<String> regioes,
        String email,
        String telefone,
        boolean ativo,
        long imoveis,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {
}
