package br.com.terranexo.property.application;

import br.com.terranexo.property.domain.Category;
import br.com.terranexo.property.domain.Purpose;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Critérios da pesquisa pública (seção 13). Reúne a pesquisa simples, a avançada
 * e os filtros rurais adicionais.
 */
public record PropertyQuery(
        String q,
        Category categoria,
        Purpose finalidade,
        Set<String> tipos,

        String uf,
        String municipio,
        String bairro,
        String regiao,

        BigDecimal precoMin,
        BigDecimal precoMax,
        Double areaMinM2,
        Double areaMaxM2,

        Integer dormitorios,
        Integer suites,
        Integer banheiros,
        Integer vagas,

        Set<String> caracteristicas,
        String corretor,
        String codigo,
        Boolean permuta,
        Boolean financiamento,

        // ---- Filtros rurais adicionais (seção 13) ----
        Double areaHaMin,
        Double areaHaMax,
        Set<String> terreno,
        Set<String> atividades,
        Set<String> culturas,
        Boolean pecuaria,
        Set<String> agua,
        Boolean irrigacao,
        Boolean energiaTrifasica,
        Double distanciaAsfaltoMaxKm,
        Set<String> benfeitorias,
        Set<String> documentacao,

        SortOption ordenacao,
        int pagina,
        int tamanho) {

    public static final int TAMANHO_PADRAO = 12;
    public static final int TAMANHO_MAXIMO = 48;

    /** Normaliza paginação e ordenação para valores utilizáveis. */
    public PropertyQuery {
        pagina = Math.max(1, pagina);
        tamanho = tamanho <= 0 ? TAMANHO_PADRAO : Math.min(tamanho, TAMANHO_MAXIMO);
        ordenacao = ordenacao == null ? SortOption.RELEVANCIA : ordenacao;
    }

    /**
     * Filtros considerados "simples" geram páginas indexáveis; qualquer combinação
     * além disso recebe noindex e canonical para a listagem base (seção 20).
     */
    public boolean elegivelParaIndexacao() {
        // `tipos` não entra aqui: um único tipo ainda gera página indexável
        // (ex.: /imoveis?categoria=RURAL&finalidade=VENDA&tipo=Fazenda).
        boolean semFiltrosFinos = isVazio(caracteristicas) && isVazio(terreno)
                && isVazio(atividades) && isVazio(culturas) && isVazio(agua) && isVazio(benfeitorias)
                && isVazio(documentacao)
                && precoMin == null && precoMax == null && areaMinM2 == null && areaMaxM2 == null
                && areaHaMin == null && areaHaMax == null && dormitorios == null && suites == null
                && banheiros == null && vagas == null && pecuaria == null && irrigacao == null
                && energiaTrifasica == null && permuta == null && financiamento == null
                && distanciaAsfaltoMaxKm == null && isBranco(q) && isBranco(corretor) && isBranco(codigo)
                && isBranco(bairro);
        boolean tiposIndexaveis = tipos == null || tipos.size() <= 1;
        return semFiltrosFinos && tiposIndexaveis && pagina <= 10;
    }

    /**
     * Plural dos tipos de imóvel. Tipos compostos não seguem regra simples
     * ("Sala comercial" → "Salas comerciais"), por isso a tabela é explícita.
     */
    private static final java.util.Map<String, String> PLURAIS = java.util.Map.ofEntries(
            java.util.Map.entry("Apartamento", "Apartamentos"),
            java.util.Map.entry("Casa", "Casas"),
            java.util.Map.entry("Casa em condomínio", "Casas em condomínio"),
            java.util.Map.entry("Cobertura", "Coberturas"),
            java.util.Map.entry("Kitnet", "Kitnets"),
            java.util.Map.entry("Loft", "Lofts"),
            java.util.Map.entry("Sala comercial", "Salas comerciais"),
            java.util.Map.entry("Terreno", "Terrenos"),
            java.util.Map.entry("Fazenda", "Fazendas"),
            java.util.Map.entry("Sítio", "Sítios"),
            java.util.Map.entry("Chácara", "Chácaras"),
            java.util.Map.entry("Haras", "Haras"),
            java.util.Map.entry("Propriedade agrícola", "Propriedades agrícolas"),
            java.util.Map.entry("Propriedade pecuária", "Propriedades pecuárias"),
            java.util.Map.entry("Área de reflorestamento", "Áreas de reflorestamento"));

    private static String plural(String tipo) {
        String conhecido = PLURAIS.get(tipo);
        if (conhecido != null) {
            return conhecido;
        }
        return tipo.endsWith("s") ? tipo : tipo + "s";
    }

    /** Rótulo humano da busca, usado em H1 e title da página de resultados. */
    public String tituloDaBusca() {
        StringBuilder sb = new StringBuilder();
        if (tipos != null && tipos.size() == 1) {
            sb.append(plural(tipos.iterator().next()));
        } else if (categoria == Category.RURAL) {
            sb.append("Imóveis rurais");
        } else if (categoria == Category.URBANO) {
            sb.append("Imóveis urbanos");
        } else {
            sb.append("Imóveis");
        }
        if (finalidade != null) {
            sb.append(switch (finalidade) {
                case VENDA -> " à venda";
                case LOCACAO -> " para alugar";
                case ARRENDAMENTO -> " para arrendamento";
                case VENDA_LOCACAO -> " à venda ou locação";
            });
        }
        if (municipio != null && !municipio.isBlank()) {
            sb.append(" em ").append(municipio);
            if (uf != null && !uf.isBlank()) {
                sb.append("/").append(uf.toUpperCase());
            }
        } else if (uf != null && !uf.isBlank()) {
            sb.append(" em ").append(uf.toUpperCase());
        }
        return sb.toString();
    }

    /** Reescreve a query preservando filtros, usada para calcular facetas. */
    public PropertyQuery semCategoria() {
        return new PropertyQuery(q, null, finalidade, tipos, uf, municipio, bairro, regiao,
                precoMin, precoMax, areaMinM2, areaMaxM2, dormitorios, suites, banheiros, vagas,
                caracteristicas, corretor, codigo, permuta, financiamento,
                areaHaMin, areaHaMax, terreno, atividades, culturas, pecuaria, agua, irrigacao,
                energiaTrifasica, distanciaAsfaltoMaxKm, benfeitorias, documentacao,
                ordenacao, pagina, tamanho);
    }

    public PropertyQuery semTipos() {
        return new PropertyQuery(q, categoria, finalidade, null, uf, municipio, bairro, regiao,
                precoMin, precoMax, areaMinM2, areaMaxM2, dormitorios, suites, banheiros, vagas,
                caracteristicas, corretor, codigo, permuta, financiamento,
                areaHaMin, areaHaMax, terreno, atividades, culturas, pecuaria, agua, irrigacao,
                energiaTrifasica, distanciaAsfaltoMaxKm, benfeitorias, documentacao,
                ordenacao, pagina, tamanho);
    }

    public PropertyQuery semMunicipio() {
        return new PropertyQuery(q, categoria, finalidade, tipos, uf, null, bairro, regiao,
                precoMin, precoMax, areaMinM2, areaMaxM2, dormitorios, suites, banheiros, vagas,
                caracteristicas, corretor, codigo, permuta, financiamento,
                areaHaMin, areaHaMax, terreno, atividades, culturas, pecuaria, agua, irrigacao,
                energiaTrifasica, distanciaAsfaltoMaxKm, benfeitorias, documentacao,
                ordenacao, pagina, tamanho);
    }

    public PropertyQuery semFinalidade() {
        return new PropertyQuery(q, categoria, null, tipos, uf, municipio, bairro, regiao,
                precoMin, precoMax, areaMinM2, areaMaxM2, dormitorios, suites, banheiros, vagas,
                caracteristicas, corretor, codigo, permuta, financiamento,
                areaHaMin, areaHaMax, terreno, atividades, culturas, pecuaria, agua, irrigacao,
                energiaTrifasica, distanciaAsfaltoMaxKm, benfeitorias, documentacao,
                ordenacao, pagina, tamanho);
    }

    static boolean isVazio(Set<String> s) {
        return s == null || s.isEmpty();
    }

    static boolean isBranco(String s) {
        return s == null || s.isBlank();
    }

    /** Converte listas de query string ("a,b" ou repetição do parâmetro) em conjunto normalizado. */
    public static Set<String> conjunto(List<String> valores) {
        if (valores == null || valores.isEmpty()) {
            return null;
        }
        Set<String> out = valores.stream()
                .filter(v -> v != null && !v.isBlank())
                .flatMap(v -> java.util.Arrays.stream(v.split(",")))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        return out.isEmpty() ? null : out;
    }
}
