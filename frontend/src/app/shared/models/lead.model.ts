/** Contratos do funil de leads (espelha o backend `lead`). */

export interface LeadCard {
  id: string;
  protocolo: string;
  nome: string;
  email: string;
  telefone: string;
  origem: string;
  origemRotulo: string;
  imovelCodigo: string | null;
  etapa: string;
  etapaRotulo: string;
  responsavel: string | null;
  criadoEm: string;
}

export interface LeadDetail {
  id: string;
  protocolo: string;
  nome: string;
  email: string;
  telefone: string;
  mensagem: string | null;
  melhorHorario: string | null;
  origem: string;
  origemRotulo: string;
  imovelCodigo: string | null;
  paginaOrigem: string | null;
  utmSource: string | null;
  utmMedium: string | null;
  utmCampaign: string | null;
  consentimento: boolean;
  etapa: string;
  etapaRotulo: string;
  responsavel: string | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface Opcao {
  valor: string;
  rotulo: string;
}

export interface FiltroLead {
  etapa?: string;
  origem?: string;
  q?: string;
}

/** Corpo de cadastro manual/edição — espelha CreateLeadRequest no backend. */
export interface CreateLeadPayload {
  nome: string;
  email: string;
  telefone: string;
  mensagem?: string | null;
  melhorHorario?: string | null;
  imovelCodigo?: string | null;
  responsavel?: string | null;
}

/** Corpo de {@code PATCH /admin/leads/{id}/etapa} — espelha UpdateLeadStageRequest. */
export interface AtualizarEtapaPayload {
  etapa: string;
  responsavel?: string | null;
}
