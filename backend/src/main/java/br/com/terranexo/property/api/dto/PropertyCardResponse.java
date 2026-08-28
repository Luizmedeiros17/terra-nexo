package br.com.terranexo.property.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Cartão de resultado da pesquisa (seção 13). Traz o preço já formatado para
 * garantir a mesma apresentação no SSR e no cliente, e o valor numérico para
 * quem precisar reprocessar.
 */
public record PropertyCardResponse(
        String id,
        String codigo,
        String slug,
        String url,

        String titulo,
        String chamada,

        String categoria,
        String categoriaRotulo,
        String tipo,
        String finalidade,
        String finalidadeRotulo,
        String situacao,

        String localizacao,
        String municipio,
        String uf,

        BigDecimal preco,
        boolean exibirPreco,
        String precoTexto,
        String precoSufixo,
        BigDecimal condominio,

        double areaM2,
        String areaTexto,

        Ambientes ambientes,
        String resumoRural,
        List<String> selos,
        Imagem imagem,
        Corretor corretor,

        LocalDate atualizadoEm) {

    public record Ambientes(int dormitorios, int suites, int banheiros, int vagas) {
    }

    public record Imagem(String url, String alt) {
    }

    public record Corretor(String slug, String nome) {
    }
}
