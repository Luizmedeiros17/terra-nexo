package br.com.terranexo.broker.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Cadastro/edição de um corretor. */
public record CreateBrokerRequest(
        @NotBlank(message = "Informe o nome") @Size(max = 150) String nome,
        @Size(max = 120) String cargo,
        @Size(max = 60) String creci,
        String foto,
        @Size(max = 2000) String bio,
        List<String> especialidades,
        List<String> regioes,
        String email,
        String telefone,
        Boolean ativo) {
}
