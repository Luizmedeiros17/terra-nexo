package br.com.terranexo.owner.domain;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Proprietário de imóvel — pessoa física ou jurídica dona de um ou mais
 * imóveis cadastrados na plataforma. Cadastro de uso interno da equipe
 * comercial, independente do catálogo público de imóveis.
 */
public record Owner(
        String id,
        TipoPessoa tipoPessoa,
        String nome,
        String documento,
        String email,
        String telefone,
        String telefoneAlternativo,
        Address endereco,
        List<String> imoveisVinculados,
        String observacoes,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {

    public enum TipoPessoa {FISICA, JURIDICA}

    public record Address(String municipio, String uf, String logradouro, String numero,
                           String complemento, String bairro, String cep) {

        public String enderecoCompleto() {
            StringBuilder sb = new StringBuilder();
            if (logradouro != null && !logradouro.isBlank()) {
                sb.append(logradouro);
            }
            if (numero != null && !numero.isBlank()) {
                sb.append(sb.isEmpty() ? "" : ", ").append(numero);
            }
            if (bairro != null && !bairro.isBlank()) {
                sb.append(sb.isEmpty() ? "" : ", ").append(bairro);
            }
            if (municipio != null && !municipio.isBlank()) {
                sb.append(sb.isEmpty() ? "" : ", ").append(municipio);
                if (uf != null && !uf.isBlank()) {
                    sb.append("/").append(uf);
                }
            }
            return sb.isEmpty() ? "—" : sb.toString();
        }
    }

    public Owner comAtualizacao(TipoPessoa tipoPessoa, String nome, String documento, String email,
                                 String telefone, String telefoneAlternativo, Address endereco,
                                 List<String> imoveisVinculados, String observacoes, OffsetDateTime agora) {
        return new Owner(id, tipoPessoa, nome, documento, email, telefone, telefoneAlternativo, endereco,
                imoveisVinculados, observacoes, criadoEm, agora);
    }
}
