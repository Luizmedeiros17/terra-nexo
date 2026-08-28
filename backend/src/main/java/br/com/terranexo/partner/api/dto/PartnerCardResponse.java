package br.com.terranexo.partner.api.dto;

import java.time.OffsetDateTime;

/** Linha de listagem — visão resumida de um parceiro/prospect. */
public record PartnerCardResponse(
        String id,
        String municipio,
        String uf,
        String setor,
        String setorRotulo,
        String nomeFantasia,
        String razaoSocial,
        String cnpj,
        String titularPrincipal,
        String telefoneFixo,
        String telefoneCelular,
        String whatsapp,
        String email,
        String site,
        String status,
        String statusRotulo,
        OffsetDateTime atualizadoEm) {
}
