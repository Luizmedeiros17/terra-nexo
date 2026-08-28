package br.com.terranexo.broker.api.dto;

import java.util.List;

public record BrokerCardResponse(
        String id,
        String slug,
        String nome,
        String cargo,
        String creci,
        String foto,
        List<String> especialidades,
        List<String> regioes,
        boolean ativo,
        long imoveis) {
}
