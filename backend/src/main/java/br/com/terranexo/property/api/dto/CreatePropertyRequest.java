package br.com.terranexo.property.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cadastro de imóvel (seções 8, 9 e 10).
 *
 * <p>Os blocos {@code urbano} e {@code rural} são mutuamente exclusivos: vale o
 * que corresponde à {@code categoria}. As validações de publicação (regras 24.1
 * e 24.2) são aplicadas no serviço, porque dependem da situação escolhida —
 * um rascunho pode ficar incompleto.</p>
 */
public record CreatePropertyRequest(
        @NotBlank(message = "Informe a categoria") String categoria,
        @NotBlank(message = "Informe o tipo do imóvel") String tipo,
        @NotBlank(message = "Informe a finalidade") String finalidade,
        String situacao,

        @NotBlank(message = "Informe o título") @Size(max = 160) String titulo,
        @Size(max = 300) String chamada,
        String descricao,

        @Valid Valores valores,
        @Valid @NotNull(message = "Informe as áreas") Areas areas,
        @Valid Ambientes ambientes,
        @Valid @NotNull(message = "Informe a localização") Endereco endereco,

        List<String> caracteristicas,

        String corretorSlug,
        String corretorNome,
        String proprietario,

        boolean destaque,
        boolean exclusivo,
        boolean lancamento,
        boolean oportunidade,
        boolean aceitaPermuta,
        boolean aceitaFinanciamento,

        @Valid Urbano urbano,
        @Valid Rural rural) {

    public record Valores(
            @PositiveOrZero(message = "O preço não pode ser negativo") BigDecimal preco,
            @PositiveOrZero BigDecimal condominio,
            @PositiveOrZero BigDecimal iptu,
            @PositiveOrZero BigDecimal arrendamentoPorHectare,
            boolean exibirPreco,
            String textoAlternativo) {
    }

    /** A unidade informada é preservada; o serviço calcula o equivalente em m². */
    public record Areas(
            @Positive(message = "Informe a área total") double valor,
            @NotBlank(message = "Informe a unidade da área") String unidade,
            @PositiveOrZero Double areaConstruidaM2,
            @PositiveOrZero Double areaTerrenoM2) {
    }

    public record Ambientes(
            @PositiveOrZero int dormitorios,
            @PositiveOrZero int suites,
            @PositiveOrZero int banheiros,
            @PositiveOrZero int vagas) {
    }

    public record Endereco(
            @NotBlank(message = "Informe a UF") @Size(min = 2, max = 2) String uf,
            @NotBlank(message = "Informe o município") String municipio,
            String bairro,
            String logradouro,
            String numero,
            String complemento,
            @Pattern(regexp = "^$|^\\d{5}-?\\d{3}$", message = "CEP inválido") String cep,
            String regiaoSlug,
            Double latitude,
            Double longitude,
            @NotBlank(message = "Informe o nível de exposição") String exposicao) {
    }

    public record Urbano(
            String condominioNome,
            Integer andar,
            Integer anoConstrucao,
            boolean mobiliado,
            boolean aceitaAnimais,
            List<String> transporteProximo) {
    }

    /** Extensão rural declaratória (seção 10). */
    public record Rural(
            @PositiveOrZero double areaProdutivaHa,
            @PositiveOrZero double areaPastagemHa,
            @PositiveOrZero double areaCultivadaHa,
            @PositiveOrZero double areaMataHa,
            @PositiveOrZero Double reservaLegalHa,
            @PositiveOrZero Double areaIrrigadaHa,
            List<String> terreno,
            String soloPredominante,
            String soloFertilidade,
            List<String> atividades,
            List<@Valid Cultura> culturas,
            @Valid Pecuaria pecuaria,
            List<String> recursosHidricos,
            List<@Valid Benfeitoria> benfeitorias,
            @Valid Acesso acesso,
            @Valid Documentacao documentacao,
            String fonteInformacao,
            String dataReferencia) {

        public record Cultura(
                @NotBlank(message = "Informe o nome da cultura") String nome,
                String categoria,
                @PositiveOrZero double areaHa,
                String safra,
                String produtividade) {
        }

        public record Pecuaria(
                String tipoCriacao,
                @PositiveOrZero Integer capacidadeCabecas,
                @PositiveOrZero int divisoesPasto,
                List<String> estruturas) {
        }

        public record Benfeitoria(
                @NotBlank(message = "Informe o tipo da benfeitoria") String tipo,
                @Positive(message = "A quantidade deve ser maior que zero") int quantidade,
                @PositiveOrZero Double areaM2,
                String conservacao) {
        }

        public record Acesso(
                String tipoAcesso,
                @DecimalMin(value = "0.0") double distanciaAsfaltoKm,
                @DecimalMin(value = "0.0") double distanciaCidadeKm,
                String municipioReferencia,
                boolean acessoAnoTodo,
                boolean energiaTrifasica,
                boolean internet,
                boolean sinalCelular) {
        }

        public record Documentacao(
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
        }
    }
}
