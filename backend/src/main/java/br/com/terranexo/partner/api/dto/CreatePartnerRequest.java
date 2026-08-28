package br.com.terranexo.partner.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Cadastro/edição de um parceiro ou prospect da base comercial. */
public record CreatePartnerRequest(
        @NotBlank(message = "Informe a cidade") String municipio,
        @NotBlank(message = "Informe a UF") @Size(min = 2, max = 2) String uf,
        @NotBlank(message = "Informe o setor") String setor,

        @NotBlank(message = "Informe o nome da empresa") @Size(max = 200) String nomeFantasia,
        String razaoSocial,
        String cnpj,
        String situacaoCadastral,

        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cep,

        String site,

        List<@Valid Titular> titulares,
        List<@Valid Contato> contatos,

        String fonteInformacao,
        String dataColeta,
        @Size(max = 2000) String observacoes) {

    public record Titular(
            @NotBlank(message = "Informe o nome do titular") String nome,
            String cpfMascarado,
            String qualificacao,
            boolean principal) {
    }

    public record Contato(
            @NotBlank(message = "Informe o tipo de contato") String tipo,
            @NotBlank(message = "Informe o valor do contato") String valor,
            boolean principal) {
    }
}
