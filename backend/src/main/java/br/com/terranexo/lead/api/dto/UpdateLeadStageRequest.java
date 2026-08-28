package br.com.terranexo.lead.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo de {@code PATCH /api/v1/admin/leads/{id}/etapa}. */
public record UpdateLeadStageRequest(
        @NotBlank(message = "Informe a nova etapa") String etapa,
        @Size(max = 120) String responsavel) {
}
