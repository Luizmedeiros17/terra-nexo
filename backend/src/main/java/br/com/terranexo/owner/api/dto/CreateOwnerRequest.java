package br.com.terranexo.owner.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Cadastro/edição de um proprietário. */
public record CreateOwnerRequest(
        @NotBlank(message = "Informe o tipo de pessoa") String tipoPessoa,
        @NotBlank(message = "Informe o nome") @Size(max = 200) String nome,
        String documento,
        String email,
        String telefone,
        String telefoneAlternativo,

        @NotBlank(message = "Informe a cidade") String municipio,
        @NotBlank(message = "Informe a UF") @Size(min = 2, max = 2) String uf,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cep,

        List<String> imoveisVinculados,
        @Size(max = 2000) String observacoes) {
}
