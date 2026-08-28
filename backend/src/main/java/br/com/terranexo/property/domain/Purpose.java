package br.com.terranexo.property.domain;

/** Finalidade do anúncio (seção 7). */
public enum Purpose {
    VENDA("Venda"),
    LOCACAO("Locação"),
    VENDA_LOCACAO("Venda ou locação"),
    ARRENDAMENTO("Arrendamento rural");

    private final String rotulo;

    Purpose(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    /** Uma busca por VENDA também deve trazer imóveis marcados como VENDA_LOCACAO. */
    public boolean atende(Purpose filtro) {
        if (filtro == null || this == filtro) {
            return true;
        }
        return this == VENDA_LOCACAO && (filtro == VENDA || filtro == LOCACAO);
    }
}
