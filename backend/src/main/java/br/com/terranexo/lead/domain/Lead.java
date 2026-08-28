package br.com.terranexo.lead.domain;

import java.time.OffsetDateTime;

/**
 * Contato recebido pelo portal (seção 17). No MVP o lead nasce em
 * {@link Stage#NOVO} e a distribuição para o corretor é manual.
 */
public record Lead(
        String id,
        String protocolo,
        String nome,
        String email,
        String telefone,
        String mensagem,
        String melhorHorario,
        Origin origem,
        String imovelCodigo,
        String paginaOrigem,
        String utmSource,
        String utmMedium,
        String utmCampaign,
        boolean consentimento,
        String consentimentoVersao,
        Stage etapa,
        String responsavel,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {

    /** Origens previstas na seção 17. */
    public enum Origin {
        FORMULARIO_IMOVEL("Formulário do imóvel"), CONTATO_GERAL("Contato geral"), WHATSAPP("WhatsApp"),
        TELEFONE("Telefone"), EMAIL("E-mail"), CADASTRO_MANUAL("Cadastro manual"),
        REDES_SOCIAIS("Redes sociais"), INDICACAO("Indicação"), CAMPANHA("Campanha"), OUTRA("Outra");

        private final String rotulo;

        Origin(String rotulo) {
            this.rotulo = rotulo;
        }

        public String rotulo() {
            return rotulo;
        }
    }

    /** Funil comercial (seção 17). */
    public enum Stage {
        NOVO("Novo"), AGUARDANDO_ATENDIMENTO("Aguardando atendimento"), CONTATO_INICIADO("Contato iniciado"),
        SEM_RESPOSTA("Sem resposta"), QUALIFICADO("Qualificado"), VISITA_AGENDADA("Visita agendada"),
        VISITA_REALIZADA("Visita realizada"), PROPOSTA("Proposta"), NEGOCIACAO("Negociação"),
        CONVERTIDO("Convertido"), PERDIDO("Perdido"), DUPLICADO("Duplicado"), DESCARTADO("Descartado");

        private final String rotulo;

        Stage(String rotulo) {
            this.rotulo = rotulo;
        }

        public String rotulo() {
            return rotulo;
        }
    }

    /** Nova versão com dados de contato/atribuição atualizados pelo painel administrativo. */
    public Lead comEdicao(String nome, String email, String telefone, String mensagem, String melhorHorario,
                           String responsavel, OffsetDateTime agora) {
        return new Lead(id, protocolo, nome, email, telefone, mensagem, melhorHorario, origem, imovelCodigo,
                paginaOrigem, utmSource, utmMedium, utmCampaign, consentimento, consentimentoVersao,
                etapa, responsavel, criadoEm, agora);
    }

    /** Nova versão com a etapa do funil e/ou responsável atualizados. */
    public Lead comEtapa(Stage novaEtapa, String responsavel, OffsetDateTime agora) {
        return new Lead(id, protocolo, nome, email, telefone, mensagem, melhorHorario, origem, imovelCodigo,
                paginaOrigem, utmSource, utmMedium, utmCampaign, consentimento, consentimentoVersao,
                novaEtapa, responsavel, criadoEm, agora);
    }
}
