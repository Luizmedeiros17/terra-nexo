package br.com.terranexo.property.api.dto;

import java.math.BigDecimal;
import java.util.List;

/** Indicadores do painel administrativo (seção 18). */
public record DashboardResponse(
        Imoveis imoveis,
        Leads leads,
        List<Contagem> porSituacao,
        List<Contagem> porCorretor,
        List<Contagem> porMunicipio,
        List<PropertyCardResponse> ultimosCadastros,
        List<Alerta> alertas) {

    public record Imoveis(
            long total,
            long publicados,
            long rascunhos,
            long aguardandoRevisao,
            long urbanos,
            long rurais,
            long semAtualizacaoRecente,
            long destaques,
            BigDecimal valorEmCarteira) {
    }

    public record Leads(
            long total,
            long novos,
            long semAtendimento,
            List<Contagem> porEtapa,
            List<Contagem> porOrigem) {
    }

    public record Contagem(String chave, String rotulo, long quantidade) {
    }

    /** Pendências que exigem ação do gestor — o painel serve para agir, não só para olhar. */
    public record Alerta(String tipo, String mensagem, String acaoRotulo, String acaoUrl) {
    }
}
