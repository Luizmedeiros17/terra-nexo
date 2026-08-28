package br.com.terranexo.owner.api.dto;

import java.util.List;

public record OwnerDetailResponse(
        String id,
        String tipoPessoa,
        String tipoPessoaRotulo,
        String nome,
        String documento,
        String email,
        String telefone,
        String telefoneAlternativo,
        String enderecoCompleto,
        String municipio,
        String uf,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cep,
        List<String> imoveisVinculados,
        String observacoes,
        String criadoEm,
        String atualizadoEm) {
}
