package br.com.terranexo.partner.api.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** Detalhe completo de um parceiro/prospect, incluindo sócios, contatos e histórico de status. */
public record PartnerDetailResponse(
        String id,
        String municipio,
        String uf,
        String setor,
        String setorRotulo,
        String nomeFantasia,
        String razaoSocial,
        String cnpj,
        String situacaoCadastral,
        String enderecoCompleto,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cep,
        String site,
        String geolocalizacaoUrl,
        List<Titular> titulares,
        List<Contato> contatos,
        String status,
        String statusRotulo,
        String fonteInformacao,
        LocalDate dataColeta,
        String observacoes,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm,
        List<StatusEvento> historicoStatus) {

    public record Titular(String nome, String cpfMascarado, String qualificacao, boolean principal) {
    }

    public record Contato(String tipo, String tipoRotulo, String valor, boolean principal) {
    }

    public record StatusEvento(String status, String statusRotulo, LocalDate data, String responsavel,
                                String observacao) {
    }
}
