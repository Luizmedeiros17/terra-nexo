/** Contratos do cadastro de proprietários (espelha o backend `owner`). */

export interface OwnerCard {
  id: string;
  tipoPessoa: string;
  tipoPessoaRotulo: string;
  nome: string;
  documento: string | null;
  municipio: string;
  uf: string;
  telefone: string | null;
  email: string | null;
  qtdImoveisVinculados: number;
  atualizadoEm: string;
}

export interface OwnerDetail {
  id: string;
  tipoPessoa: string;
  tipoPessoaRotulo: string;
  nome: string;
  documento: string | null;
  email: string | null;
  telefone: string | null;
  telefoneAlternativo: string | null;
  enderecoCompleto: string;
  municipio: string;
  uf: string;
  logradouro: string | null;
  numero: string | null;
  complemento: string | null;
  bairro: string | null;
  cep: string | null;
  imoveisVinculados: string[];
  observacoes: string | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface FiltroOwner {
  cidade?: string;
  q?: string;
}

/** Corpo de criação/edição — espelha CreateOwnerRequest no backend. */
export interface CreateOwnerPayload {
  tipoPessoa: string;
  nome: string;
  documento?: string | null;
  email?: string | null;
  telefone?: string | null;
  telefoneAlternativo?: string | null;

  municipio: string;
  uf: string;
  logradouro?: string | null;
  numero?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  cep?: string | null;

  imoveisVinculados: string[];
  observacoes?: string | null;
}
