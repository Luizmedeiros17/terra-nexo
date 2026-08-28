package br.com.terranexo.partner.api.dto;

import java.util.List;

/** Contagens para o painel — mesma visão da aba "Painel" da planilha de trabalho. */
public record PartnerResumoResponse(
        int total,
        List<Contagem> porCidade,
        List<Contagem> porSetor,
        List<Contagem> porStatus) {

    public record Contagem(String chave, String rotulo, long quantidade) {
    }
}
