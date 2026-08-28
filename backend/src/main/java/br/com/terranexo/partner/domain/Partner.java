package br.com.terranexo.partner.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Empresa/organização da base de prospecção comercial (clientes e parceiros:
 * agropecuária, imobiliárias, irrigação, silos/armazéns, leilão de animais,
 * laticínios). Independente do catálogo público de imóveis.
 *
 * <p>CNPJ, razão social e situação cadastral vêm da consulta pública da Receita
 * Federal. CPF de sócios é gravado sempre mascarado, exatamente como a própria
 * Receita divulga no QSA — nunca o número completo (LGPD).</p>
 */
public record Partner(
        String id,
        String municipio,
        String uf,
        Segment setor,
        String nomeFantasia,
        String razaoSocial,
        String cnpj,
        String situacaoCadastral,
        Address endereco,
        String site,
        Geo geolocalizacao,
        List<Titular> titulares,
        List<Contato> contatos,
        ProspectStatus status,
        String fonteInformacao,
        LocalDate dataColeta,
        String observacoes,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm,
        List<StatusEvent> historicoStatus) {

    public record Address(String logradouro, String numero, String complemento, String bairro, String cep) {

        public String enderecoCompleto(String municipio, String uf) {
            StringBuilder sb = new StringBuilder();
            if (logradouro != null) {
                sb.append(logradouro);
            }
            if (numero != null && !numero.isBlank()) {
                sb.append(sb.isEmpty() ? "" : ", ").append(numero);
            }
            if (bairro != null && !bairro.isBlank()) {
                sb.append(sb.isEmpty() ? "" : ", ").append(bairro);
            }
            sb.append(sb.isEmpty() ? "" : ", ").append(municipio).append("-").append(uf);
            if (cep != null && !cep.isBlank()) {
                sb.append(", ").append(cep);
            }
            return sb.toString();
        }
    }

    public record Geo(String mapsUrl, Double latitude, Double longitude) {
    }

    public record Titular(String nome, String cpfMascarado, String qualificacao, boolean principal) {
    }

    public enum ContatoTipo {
        TELEFONE_FIXO, TELEFONE_CELULAR, WHATSAPP, EMAIL
    }

    public record Contato(ContatoTipo tipo, String valor, boolean principal) {
    }

    public record StatusEvent(ProspectStatus status, LocalDate data, String responsavel, String observacao) {
    }

    /** Sócio/titular principal, se houver — o primeiro marcado como principal, senão o primeiro da lista. */
    public Titular titularPrincipal() {
        if (titulares == null || titulares.isEmpty()) {
            return null;
        }
        return titulares.stream().filter(Titular::principal).findFirst().orElse(titulares.get(0));
    }

    /** Valor do contato do tipo informado, preferindo o marcado como principal. */
    public String contato(ContatoTipo tipo) {
        if (contatos == null) {
            return null;
        }
        return contatos.stream()
                .filter(c -> c.tipo() == tipo)
                .sorted((a, b) -> Boolean.compare(b.principal(), a.principal()))
                .map(Contato::valor)
                .findFirst()
                .orElse(null);
    }

    /** Nova versão do registro com status atualizado e evento acrescentado ao histórico. */
    public Partner comNovoStatus(ProspectStatus novoStatus, String responsavel, String observacaoEvento,
                                  LocalDate data, OffsetDateTime agora) {
        List<StatusEvent> historico = new java.util.ArrayList<>(
                historicoStatus == null ? List.of() : historicoStatus);
        historico.add(new StatusEvent(novoStatus, data, responsavel, observacaoEvento));
        return new Partner(id, municipio, uf, setor, nomeFantasia, razaoSocial, cnpj, situacaoCadastral,
                endereco, site, geolocalizacao, titulares, contatos, novoStatus, fonteInformacao,
                dataColeta, observacoes, criadoEm, agora, List.copyOf(historico));
    }
}
