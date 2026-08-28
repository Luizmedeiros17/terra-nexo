package br.com.terranexo.property.domain;

/** Situação administrativa do imóvel (seção 7). */
public enum PropertyStatus {
    RASCUNHO("Rascunho"),
    AGUARDANDO_REVISAO("Aguardando revisão"),
    APROVADO("Aprovado"),
    PUBLICADO("Publicado"),
    RESERVADO("Reservado"),
    EM_NEGOCIACAO("Em negociação"),
    VENDIDO("Vendido"),
    ALUGADO("Alugado"),
    SUSPENSO("Suspenso"),
    ARQUIVADO("Arquivado");

    private final String rotulo;

    PropertyStatus(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
