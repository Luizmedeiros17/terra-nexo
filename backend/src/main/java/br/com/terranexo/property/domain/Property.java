package br.com.terranexo.property.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Imóvel publicável — raiz do agregado (seções 7 a 10 da especificação).
 *
 * <p>Os dados específicos de imóveis rurais ficam em {@link Rural}, estrutura própria
 * vinculada ao imóvel principal, conforme a seção 10. Um imóvel urbano tem
 * {@code rural == null} e vice-versa.</p>
 */
public record Property(
        String id,
        String codigo,
        String slug,
        Category categoria,
        String tipo,
        Purpose finalidade,
        PropertyStatus situacao,

        String titulo,
        String chamada,
        String descricao,

        Money valores,
        Areas areas,
        Rooms ambientes,
        Address endereco,

        Set<String> caracteristicas,
        List<Media> midias,

        Urban urbano,
        Rural rural,

        String corretorSlug,
        String corretorNome,

        boolean destaque,
        boolean exclusivo,
        boolean lancamento,
        boolean oportunidade,
        boolean aceitaPermuta,
        boolean aceitaFinanciamento,

        LocalDate publicadoEm,
        LocalDate atualizadoEm) {

    /** Valores comerciais (seção 8). O preço pode ser ocultado publicamente. */
    public record Money(
            BigDecimal preco,
            BigDecimal condominio,
            BigDecimal iptu,
            BigDecimal arrendamentoPorHectare,
            boolean exibirPreco,
            String textoAlternativo) {

        public static Money venda(long preco, long condominio, long iptu) {
            return new Money(BigDecimal.valueOf(preco), BigDecimal.valueOf(condominio),
                    BigDecimal.valueOf(iptu), null, true, null);
        }

        public static Money sobConsulta(long precoInterno) {
            return new Money(BigDecimal.valueOf(precoInterno), null, null, null, false, "Consulte-nos");
        }
    }

    /**
     * Áreas do imóvel (seção 8). {@code areaTotalM2} é o valor normalizado usado
     * em filtros e ordenação; {@code unidadeInformada} preserva como o corretor cadastrou.
     */
    public record Areas(
            double areaTotalM2,
            Double areaConstruidaM2,
            Double areaTerrenoM2,
            String unidadeInformada,
            double valorNaUnidadeInformada) {

        public static Areas urbana(double util, Double construida) {
            return new Areas(util, construida, null, "m²", util);
        }

        /** 1 hectare = 10.000 m². */
        public static Areas hectares(double hectares) {
            return new Areas(hectares * 10_000d, null, hectares * 10_000d, "ha", hectares);
        }

        /** Alqueire paulista = 24.200 m² (fator armazenado explicitamente, seção 8). */
        public static Areas alqueiresPaulistas(double alqueires) {
            return new Areas(alqueires * 24_200d, null, alqueires * 24_200d, "alq", alqueires);
        }
    }

    /** Ambientes (seção 8). */
    public record Rooms(int dormitorios, int suites, int banheiros, int vagas) {
        public static Rooms nenhum() {
            return new Rooms(0, 0, 0, 0);
        }
    }

    /** Endereço e nível de exposição (seção 11). */
    public record Address(
            String uf,
            String municipio,
            String bairro,
            String logradouro,
            String numero,
            String complemento,
            String cep,
            String regiaoSlug,
            Double latitude,
            Double longitude,
            Exposure exposicao) {

        /**
         * Resumo público, respeitando o nível de exposição configurado.
         * O número só aparece em ENDERECO_COMPLETO; ENDERECO_PARCIAL omite,
         * conforme a seção 11.
         */
        public String resumoPublico() {
            return switch (exposicao) {
                case ENDERECO_COMPLETO -> "%s%s — %s, %s/%s".formatted(
                        logradouro,
                        numero == null || numero.isBlank() ? "" : ", " + numero,
                        bairro, municipio, uf);
                case ENDERECO_PARCIAL -> "%s — %s, %s/%s".formatted(logradouro, bairro, municipio, uf);
                case BAIRRO_MUNICIPIO -> "%s, %s/%s".formatted(bairro, municipio, uf);
                case SOMENTE_MUNICIPIO -> "%s/%s".formatted(municipio, uf);
            };
        }

        public enum Exposure {ENDERECO_COMPLETO, ENDERECO_PARCIAL, BAIRRO_MUNICIPIO, SOMENTE_MUNICIPIO}
    }

    /** Fotografia ou vídeo do imóvel (seção 12). */
    public record Media(String url, String textoAlternativo, int ordem, boolean capa, MediaType tipo) {
    }

    /** Tipo de arquivo de mídia enviado (seção 12). */
    public enum MediaType {FOTO, VIDEO}

    /** Campos exclusivos de imóveis urbanos (seção 9). */
    public record Urban(
            String condominioNome,
            Integer andar,
            Integer anoConstrucao,
            boolean mobiliado,
            boolean aceitaAnimais,
            List<String> transporteProximo) {
    }

    /**
     * Extensão rural (seção 10). Todos os dados técnicos são declaratórios —
     * a plataforma armazena e indica a fonte, sem validação jurídica ou fundiária.
     */
    public record Rural(
            double areaProdutivaHa,
            double areaPastagemHa,
            double areaCultivadaHa,
            double areaMataHa,
            Double reservaLegalHa,
            Double areaIrrigadaHa,
            Set<String> terreno,
            String soloPredominante,
            String soloFertilidade,
            Set<String> atividades,
            List<Culture> culturas,
            Livestock pecuaria,
            Set<String> recursosHidricos,
            List<Improvement> benfeitorias,
            Access acesso,
            Documentation documentacao,
            String fonteInformacao,
            LocalDate dataReferencia) {

        /** Cultura declarada (seção 10). */
        public record Culture(String nome, String categoria, double areaHa, String safra, String produtividade) {
        }

        /** Pecuária (seção 10). */
        public record Livestock(String tipoCriacao, Integer capacidadeCabecas, int divisoesPasto, Set<String> estruturas) {
        }

        /** Benfeitoria (seção 10). */
        public record Improvement(String tipo, int quantidade, Double areaM2, String conservacao) {
        }

        /** Infraestrutura e acesso (seção 10). */
        public record Access(
                String tipoAcesso,
                double distanciaAsfaltoKm,
                double distanciaCidadeKm,
                String municipioReferencia,
                boolean acessoAnoTodo,
                boolean energiaTrifasica,
                boolean internet,
                boolean sinalCelular) {
        }

        /**
         * Documentação rural (seção 10). Os números em si são privados; a API pública
         * expõe apenas quais documentos foram informados.
         *
         * <p>Os campos {@code carInformado}, {@code ccirInformado}, {@code sigefInformado},
         * {@code matriculaInformada} e {@code georreferenciado} alimentam os filtros públicos
         * de busca (seção 13) — não renomear/remover sem atualizar {@code PropertySearchService}
         * e o filtro "Documentos" do portal. Os campos abaixo detalham cada documento para uso
         * exclusivamente interno (nunca expostos na API pública).</p>
         */
        public record Documentation(
                boolean carInformado,
                boolean ccirInformado,
                boolean sigefInformado,
                boolean matriculaInformada,
                boolean georreferenciado,
                String situacaoDeclarada,

                String matriculaNumero,
                String carReciboNumero,
                boolean carDemonstrativoCompletoInformado,
                boolean carMapaPoligonoInformado,
                String sigefSituacaoGeorreferenciamento,
                boolean sigefPlantaMemorialDisponivel,
                String sncrIncraSituacaoCadastral,
                String cnirCafirCib,
                String cnirCafirSituacaoCadastral) {

            /** Forma reduzida (seed/dados antigos) — os campos detalhados ficam sem informação. */
            public Documentation(boolean carInformado, boolean ccirInformado, boolean sigefInformado,
                                  boolean matriculaInformada, boolean georreferenciado, String situacaoDeclarada) {
                this(carInformado, ccirInformado, sigefInformado, matriculaInformada, georreferenciado,
                        situacaoDeclarada, null, null, false, false, null, false, null, null, null);
            }
        }
    }

    // ---- Conveniências de domínio ----

    public boolean isRural() {
        return categoria == Category.RURAL && rural != null;
    }

    /** Capa vem sempre de uma fotografia — um vídeo nunca vira a miniatura pública do card. */
    public String capaUrl() {
        return fotos().filter(Media::capa).findFirst()
                .or(() -> fotos().findFirst())
                .map(Media::url)
                .orElse(null);
    }

    public String capaAlt() {
        return fotos().filter(Media::capa).findFirst()
                .or(() -> fotos().findFirst())
                .map(Media::textoAlternativo)
                .orElse(titulo);
    }

    private java.util.stream.Stream<Media> fotos() {
        return midias.stream().filter(m -> m.tipo() == MediaType.FOTO);
    }

    /** Área em hectares — usada nos filtros rurais (seção 13). */
    public double areaHectares() {
        return areas.areaTotalM2() / 10_000d;
    }

    /** Nova versão com a galeria de mídias substituída — usado pelo gerenciador de upload. */
    public Property comMidias(List<Media> novasMidias) {
        return new Property(id, codigo, slug, categoria, tipo, finalidade, situacao,
                titulo, chamada, descricao, valores, areas, ambientes, endereco,
                caracteristicas, novasMidias, urbano, rural,
                corretorSlug, corretorNome, destaque, exclusivo, lancamento, oportunidade,
                aceitaPermuta, aceitaFinanciamento, publicadoEm, java.time.LocalDate.now());
    }

    /** Somente imóveis publicados aparecem no portal (regra 24.8). */
    public boolean visivelNaBusca() {
        return situacao == PropertyStatus.PUBLICADO
                || situacao == PropertyStatus.RESERVADO
                || situacao == PropertyStatus.EM_NEGOCIACAO;
    }
}
