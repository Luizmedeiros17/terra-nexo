package br.com.terranexo.owner.api.dto;

public record OwnerCardResponse(
        String id,
        String tipoPessoa,
        String tipoPessoaRotulo,
        String nome,
        String documento,
        String municipio,
        String uf,
        String telefone,
        String email,
        int qtdImoveisVinculados,
        String atualizadoEm) {
}
