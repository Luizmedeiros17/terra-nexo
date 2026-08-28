package br.com.terranexo.property.application;

import br.com.terranexo.property.domain.Property;

import java.util.List;

/** Resultado paginado da pesquisa, com facetas para a barra de filtros. */
public record SearchResult(
        List<Property> itens,
        long total,
        int pagina,
        int tamanho,
        int totalPaginas,
        SortOption ordenacao,
        List<FacetGroup> facetas,
        PriceRange faixaPreco,
        boolean indexavel) {

    public boolean primeira() {
        return pagina <= 1;
    }

    public boolean ultima() {
        return pagina >= totalPaginas;
    }

    /** Grupo de faceta (ex.: "tipo") com seus valores e respectivas contagens. */
    public record FacetGroup(String chave, String rotulo, List<FacetValue> valores) {
    }

    public record FacetValue(String valor, String rotulo, long quantidade, boolean selecionado) {
    }

    /** Limites de preço do conjunto filtrado, para calibrar o slider da interface. */
    public record PriceRange(java.math.BigDecimal min, java.math.BigDecimal max) {
    }
}
