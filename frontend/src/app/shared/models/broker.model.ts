/** Contratos do cadastro de corretores (espelha o backend `broker`). */

export interface BrokerCard {
  id: string;
  slug: string;
  nome: string;
  cargo: string | null;
  creci: string | null;
  foto: string | null;
  especialidades: string[];
  regioes: string[];
  ativo: boolean;
  imoveis: number;
}

export interface BrokerDetail {
  id: string;
  slug: string;
  nome: string;
  cargo: string | null;
  creci: string | null;
  foto: string | null;
  bio: string | null;
  especialidades: string[];
  regioes: string[];
  email: string | null;
  telefone: string | null;
  ativo: boolean;
  imoveis: number;
  criadoEm: string;
  atualizadoEm: string;
}

export interface FiltroBroker {
  ativo?: boolean;
  q?: string;
}

/** Corpo de criação/edição — espelha CreateBrokerRequest no backend. */
export interface CreateBrokerPayload {
  nome: string;
  cargo?: string | null;
  creci?: string | null;
  foto?: string | null;
  bio?: string | null;
  especialidades: string[];
  regioes: string[];
  email?: string | null;
  telefone?: string | null;
  ativo: boolean;
}
