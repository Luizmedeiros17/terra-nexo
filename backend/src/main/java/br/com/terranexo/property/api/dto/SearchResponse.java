package br.com.terranexo.property.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta da pesquisa pública: página de resultados, facetas para a barra de
 * filtros e o bloco {@code seo}, que instrui o frontend sobre title, H1,
 * canonical e indexação da combinação de filtros (seção 20).
 */
public record SearchResponse(
        List<PropertyCardResponse> conteudo,
        Paginacao paginacao,
        String ordenacao,
        List<OrdenacaoOpcao> ordenacoes,
        List<Faceta> facetas,
        FaixaPreco faixaPreco,
        Seo seo) {

    public record Paginacao(
            int pagina,
            int tamanho,
            long totalElementos,
            int totalPaginas,
            boolean primeira,
            boolean ultima) {
    }

    public record OrdenacaoOpcao(String valor, String rotulo) {
    }

    public record Faceta(String chave, String rotulo, List<FacetaValor> valores) {
    }

    public record FacetaValor(String valor, String rotulo, long quantidade, boolean selecionado) {
    }

    public record FaixaPreco(BigDecimal min, BigDecimal max) {
    }

    public record Seo(String titulo, String h1, String descricao, boolean indexavel) {
    }
}
