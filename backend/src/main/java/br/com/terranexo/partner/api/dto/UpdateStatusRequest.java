package br.com.terranexo.partner.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo de {@code PATCH /api/v1/admin/partners/{id}/status}. */
public record UpdateStatusRequest(
        @NotBlank(message = "Informe o novo status") String status,
        @NotBlank(message = "Informe quem fez a atualização") @Size(max = 120) String responsavel,
        @Size(max = 2000) String observacao) {
}
