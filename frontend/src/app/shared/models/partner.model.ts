/** Contratos da base de prospecção de clientes e parceiros (espelha o backend `partner`). */

export interface PartnerCard {
  id: string;
  municipio: string;
  uf: string;
  setor: string;
  setorRotulo: string;
  nomeFantasia: string;
  razaoSocial: string | null;
  cnpj: string | null;
  titularPrincipal: string | null;
  telefoneFixo: string | null;
  telefoneCelular: string | null;
  whatsapp: string | null;
  email: string | null;
  site: string | null;
  status: string;
  statusRotulo: string;
  atualizadoEm: string;
}

export interface PartnerTitular {
  nome: string;
  cpfMascarado: string | null;
  qualificacao: string | null;
  principal: boolean;
}

export interface PartnerContato {
  tipo: string;
  tipoRotulo: string;
  valor: string;
  principal: boolean;
}

export interface PartnerStatusEvento {
  status: string;
  statusRotulo: string;
  data: string;
  responsavel: string | null;
  observacao: string | null;
}

export interface PartnerDetail {
  id: string;
  municipio: string;
  uf: string;
  setor: string;
  setorRotulo: string;
  nomeFantasia: string;
  razaoSocial: string | null;
  cnpj: string | null;
  situacaoCadastral: string | null;
  enderecoCompleto: string;
  logradouro: string | null;
  numero: string | null;
  complemento: string | null;
  bairro: string | null;
  cep: string | null;
  site: string | null;
  geolocalizacaoUrl: string | null;
  titulares: PartnerTitular[];
  contatos: PartnerContato[];
  status: string;
  statusRotulo: string;
  fonteInformacao: string | null;
  dataColeta: string | null;
  observacoes: string | null;
  criadoEm: string;
  atualizadoEm: string;
  historicoStatus: PartnerStatusEvento[];
}

export interface Opcao {
  valor: string;
  rotulo: string;
}

export interface ContagemPartner {
  chave: string;
  rotulo: string;
  quantidade: number;
}

export interface PartnerResumo {
  total: number;
  porCidade: ContagemPartner[];
  porSetor: ContagemPartner[];
  porStatus: ContagemPartner[];
}

export interface FiltroPartner {
  cidade?: string;
  setor?: string;
  status?: string;
  q?: string;
}

export interface AtualizarStatusPayload {
  status: string;
  responsavel: string;
  observacao?: string;
}

/** Corpo de criação/edição — espelha CreatePartnerRequest no backend. */
export interface CreatePartnerPayload {
  municipio: string;
  uf: string;
  setor: string;

  nomeFantasia: string;
  razaoSocial?: string | null;
  cnpj?: string | null;
  situacaoCadastral?: string | null;

  logradouro?: string | null;
  numero?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  cep?: string | null;

  site?: string | null;

  titulares: { nome: string; cpfMascarado?: string | null; qualificacao?: string | null; principal: boolean }[];
  contatos: { tipo: string; valor: string; principal: boolean }[];

  fonteInformacao?: string | null;
  dataColeta?: string | null;
  observacoes?: string | null;
}
