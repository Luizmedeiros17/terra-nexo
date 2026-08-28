package br.com.terranexo.property.api.dto;

import java.util.List;
import java.util.Map;

/**
 * Detalhe do imóvel (seção 14). Nesta fase serve a navegação a partir do
 * resultado de busca; a página completa entra na etapa seguinte do MVP.
 */
public record PropertyDetailResponse(
        PropertyCardResponse resumo,
        String descricao,
        List<PropertyCardResponse.Imagem> galeria,
        List<String> caracteristicas,
        Map<String, String> fichaTecnica,
        RuralDetalhe rural) {

    /**
     * Bloco rural declaratório (seção 10). Os números de documentos permanecem
     * privados: expõe-se apenas quais foram informados.
     */
    public record RuralDetalhe(
            double areaHectares,
            double areaProdutivaHa,
            double areaPastagemHa,
            double areaCultivadaHa,
            double areaMataHa,
            Double reservaLegalHa,
            Double areaIrrigadaHa,
            List<String> terreno,
            String soloPredominante,
            String soloFertilidade,
            List<String> atividades,
            List<Cultura> culturas,
            Pecuaria pecuaria,
            List<String> recursosHidricos,
            List<Benfeitoria> benfeitorias,
            Acesso acesso,
            List<String> documentacaoInformada,
            String situacaoDocumental,
            String fonteInformacao,
            String dataReferencia) {

        public record Cultura(String nome, String categoria, double areaHa, String safra, String produtividade) {
        }

        public record Pecuaria(String tipoCriacao, Integer capacidadeCabecas, int divisoesPasto, List<String> estruturas) {
        }

        public record Benfeitoria(String tipo, int quantidade, Double areaM2, String conservacao) {
        }

        public record Acesso(String tipoAcesso, double distanciaAsfaltoKm, double distanciaCidadeKm,
                             String municipioReferencia, boolean acessoAnoTodo, boolean energiaTrifasica,
                             boolean internet, boolean sinalCelular) {
        }
    }
}
