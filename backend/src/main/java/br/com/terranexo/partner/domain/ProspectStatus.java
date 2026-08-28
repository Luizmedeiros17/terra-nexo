package br.com.terranexo.partner.domain;

/** Funil de prospecção comercial de um parceiro/cliente potencial. */
public enum ProspectStatus {
    NAO_INICIADO("Não iniciado"),
    PESQUISADO("Pesquisado"),
    CONTATADO("Contatado"),
    RESPONDEU("Respondeu"),
    REUNIAO_MARCADA("Reunião marcada"),
    PROPOSTA_ENVIADA("Proposta enviada"),
    CONVERTIDO("Convertido"),
    SEM_INTERESSE("Sem interesse"),
    CONTATO_INVALIDO("Contato inválido");

    private final String rotulo;

    ProspectStatus(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
