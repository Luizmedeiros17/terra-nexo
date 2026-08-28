package br.com.terranexo.property.application;

import br.com.terranexo.property.domain.Property;

import java.math.BigDecimal;
import java.util.Comparator;

/** Opções de ordenação da pesquisa pública (seção 13). */
public enum SortOption {
    RELEVANCIA("relevancia", "Relevância"),
    RECENTES("recentes", "Mais recentes"),
    PRECO_ASC("preco-asc", "Menor preço"),
    PRECO_DESC("preco-desc", "Maior preço"),
    AREA_ASC("area-asc", "Menor área"),
    AREA_DESC("area-desc", "Maior área"),
    DESTAQUE("destaque", "Destaque");

    private final String codigo;
    private final String rotulo;

    SortOption(String codigo, String rotulo) {
        this.codigo = codigo;
        this.rotulo = rotulo;
    }

    public String codigo() {
        return codigo;
    }

    public String rotulo() {
        return rotulo;
    }

    public static SortOption porCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return RELEVANCIA;
        }
        for (SortOption s : values()) {
            if (s.codigo.equalsIgnoreCase(codigo)) {
                return s;
            }
        }
        return RELEVANCIA;
    }

    /**
     * Imóveis com preço oculto ficam ao final das ordenações por preço — não é
     * possível posicioná-los de forma honesta na sequência.
     */
    public Comparator<Property> comparator() {
        Comparator<Property> porPublicacao = Comparator.comparing(Property::publicadoEm).reversed();
        return switch (this) {
            case RECENTES -> porPublicacao;
            case PRECO_ASC -> comparadorDePreco(true).thenComparing(porPublicacao);
            case PRECO_DESC -> comparadorDePreco(false).thenComparing(porPublicacao);
            case AREA_ASC -> Comparator.comparingDouble((Property p) -> p.areas().areaTotalM2()).thenComparing(porPublicacao);
            case AREA_DESC -> Comparator.comparingDouble((Property p) -> p.areas().areaTotalM2()).reversed().thenComparing(porPublicacao);
            case DESTAQUE -> Comparator.comparing(Property::destaque).reversed().thenComparing(porPublicacao);
            case RELEVANCIA -> Comparator.comparing(Property::destaque).reversed()
                    .thenComparing(Comparator.comparing(Property::exclusivo).reversed())
                    .thenComparing(porPublicacao);
        };
    }

    private static Comparator<Property> comparadorDePreco(boolean crescente) {
        Comparator<BigDecimal> ordem = crescente
                ? Comparator.<BigDecimal>naturalOrder()
                : Comparator.<BigDecimal>reverseOrder();
        return Comparator.comparing(
                (Property p) -> p.valores().exibirPreco() ? p.valores().preco() : null,
                Comparator.nullsLast(ordem));
    }
}
