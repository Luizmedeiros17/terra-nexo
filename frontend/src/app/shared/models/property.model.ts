/**
 * Contratos da API pública (backend Spring Boot).
 * Espelham os DTOs de `br.com.terranexo.property.api.dto`.
 */

export type Categoria = 'URBANO' | 'RURAL';
export type Finalidade = 'VENDA' | 'LOCACAO' | 'VENDA_LOCACAO' | 'ARRENDAMENTO';

export interface Ambientes {
  dormitorios: number;
  suites: number;
  banheiros: number;
  vagas: number;
}

export interface Imagem {
  url: string;
  alt: string;
}

export interface CorretorResumo {
  slug: string;
  nome: string;
}

/** Cartão de resultado da pesquisa (seção 13 da especificação). */
export interface PropertyCard {
  id: string;
  codigo: string;
  slug: string;
  url: string;

  titulo: string;
  chamada: string;

  categoria: Categoria;
  categoriaRotulo: string;
  tipo: string;
  finalidade: Finalidade;
  finalidadeRotulo: string;
  situacao: string;

  localizacao: string;
  municipio: string;
  uf: string;

  preco?: number;
  exibirPreco: boolean;
  precoTexto: string;
  precoSufixo?: string;
  condominio?: number;

  areaM2: number;
  areaTexto: string;

  ambientes: Ambientes;
  resumoRural?: string;
  selos: string[];
  imagem: Imagem;
  corretor: CorretorResumo;

  atualizadoEm: string;
}

export interface Paginacao {
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
  primeira: boolean;
  ultima: boolean;
}

export interface FacetaValor {
  valor: string;
  rotulo: string;
  quantidade: number;
  selecionado: boolean;
}

export interface Faceta {
  chave: string;
  rotulo: string;
  valores: FacetaValor[];
}

export interface OrdenacaoOpcao {
  valor: string;
  rotulo: string;
}

export interface SeoBloco {
  titulo: string;
  h1: string;
  descricao: string;
  indexavel: boolean;
}

export interface SearchResponse {
  conteudo: PropertyCard[];
  paginacao: Paginacao;
  ordenacao: string;
  ordenacoes: OrdenacaoOpcao[];
  facetas: Faceta[];
  faixaPreco: { min: number; max: number };
  seo: SeoBloco;
}

export interface RuralDetalhe {
  areaHectares: number;
  areaProdutivaHa: number;
  areaPastagemHa: number;
  areaCultivadaHa: number;
  areaMataHa: number;
  reservaLegalHa?: number;
  areaIrrigadaHa?: number;
  terreno: string[];
  soloPredominante?: string;
  soloFertilidade?: string;
  atividades: string[];
  culturas: { nome: string; categoria: string; areaHa: number; safra: string; produtividade: string }[];
  pecuaria?: {
    tipoCriacao: string;
    capacidadeCabecas?: number;
    divisoesPasto: number;
    estruturas: string[];
  };
  recursosHidricos: string[];
  benfeitorias: { tipo: string; quantidade: number; areaM2?: number; conservacao: string }[];
  acesso: {
    tipoAcesso: string;
    distanciaAsfaltoKm: number;
    distanciaCidadeKm: number;
    municipioReferencia: string;
    acessoAnoTodo: boolean;
    energiaTrifasica: boolean;
    internet: boolean;
    sinalCelular: boolean;
  };
  documentacaoInformada: string[];
  situacaoDocumental?: string;
  fonteInformacao?: string;
  dataReferencia?: string;
}

export interface PropertyDetail {
  resumo: PropertyCard;
  descricao: string;
  galeria: Imagem[];
  caracteristicas: string[];
  fichaTecnica: Record<string, string>;
  rural?: RuralDetalhe;
}

export interface Regiao {
  slug: string;
  nome: string;
  uf: string;
  categoria: Categoria;
  descricao: string;
  imagem: string;
  quantidade: number;
}

export interface Corretor {
  slug: string;
  nome: string;
  cargo: string;
  creci: string;
  foto: string;
  bio: string;
  especialidades: string[];
  regioes: string[];
  imoveis: number;
}

export interface LeadRequest {
  nome: string;
  email: string;
  telefone: string;
  mensagem?: string;
  melhorHorario?: string;
  imovelCodigo?: string;
  paginaOrigem?: string;
  utmSource?: string;
  utmMedium?: string;
  utmCampaign?: string;
  consentimento: boolean;
}

export interface LeadResponse {
  protocolo: string;
  mensagem: string;
  possivelDuplicidade: boolean;
}

/** Filtros da pesquisa pública — espelha `PropertyQuery` no backend. */
export interface SearchFilters {
  q?: string;
  categoria?: Categoria | '';
  finalidade?: Finalidade | '';
  tipo?: string[];
  uf?: string;
  municipio?: string;
  bairro?: string;
  regiao?: string;
  precoMin?: number | null;
  precoMax?: number | null;
  areaMin?: number | null;
  areaMax?: number | null;
  dormitorios?: number | null;
  suites?: number | null;
  banheiros?: number | null;
  vagas?: number | null;
  caracteristica?: string[];
  permuta?: boolean | null;
  financiamento?: boolean | null;

  // rurais
  areaHaMin?: number | null;
  areaHaMax?: number | null;
  terreno?: string[];
  atividade?: string[];
  cultura?: string[];
  pecuaria?: boolean | null;
  agua?: string[];
  irrigacao?: boolean | null;
  energiaTrifasica?: boolean | null;
  distanciaAsfaltoMax?: number | null;
  benfeitoria?: string[];
  documentacao?: string[];

  ordenar?: string;
  pagina?: number;
  tamanho?: number;
}
