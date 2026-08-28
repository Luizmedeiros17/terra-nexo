package br.com.terranexo.partner.domain;

/** Segmentos de prospecção cobertos pela base comercial. */
public enum Segment {
    AGROPECUARIA("Agropecuária"),
    IMOBILIARIA("Imobiliária"),
    IRRIGACAO("Irrigação"),
    SILOS_ARMAZENS("Silos/Armazéns"),
    LEILAO_ANIMAIS("Leilão de Animais"),
    LATICINIOS("Laticínios");

    private final String rotulo;

    Segment(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    /** Segmentos relevantes para sugestão de parceiros em imóveis rurais (exclui Imobiliária). */
    public static boolean relevanteParaRural(Segment segmento) {
        return segmento != IMOBILIARIA;
    }
}
