package br.com.terranexo.property.domain;

/** Classificação principal do imóvel (seção 7). */
public enum Category {
    URBANO("Urbano"),
    RURAL("Rural");

    private final String rotulo;

    Category(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
