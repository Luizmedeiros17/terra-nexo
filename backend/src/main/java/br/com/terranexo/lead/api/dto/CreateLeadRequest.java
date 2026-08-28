package br.com.terranexo.lead.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cadastro manual (painel administrativo) ou edição dos dados de contato de um lead. */
public record CreateLeadRequest(
        @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
        @NotBlank(message = "Informe um e-mail") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "Informe um telefone") @Size(min = 10, max = 20) String telefone,
        @Size(max = 2000) String mensagem,
        @Size(max = 60) String melhorHorario,
        String imovelCodigo,
        @Size(max = 120) String responsavel) {
}
