import { PropertyCard } from './property.model';

/** Contratos do ambiente administrativo (seções 18 e 23). */

export interface Contagem {
  chave: string;
  rotulo: string;
  quantidade: number;
}

export interface Alerta {
  tipo: 'urgente' | 'atencao' | 'info';
  mensagem: string;
  acaoRotulo: string;
  acaoUrl: string;
}

export interface IndicadoresImoveis {
  total: number;
  publicados: number;
  rascunhos: number;
  aguardandoRevisao: number;
  urbanos: number;
  rurais: number;
  semAtualizacaoRecente: number;
  destaques: number;
  valorEmCarteira: number;
}

export interface IndicadoresLeads {
  total: number;
  novos: number;
  semAtendimento: number;
  porEtapa: Contagem[];
  porOrigem: Contagem[];
}

export interface Dashboard {
  imoveis: IndicadoresImoveis;
  leads: IndicadoresLeads;
  porSituacao: Contagem[];
  porCorretor: Contagem[];
  porMunicipio: Contagem[];
  ultimosCadastros: PropertyCard[];
  alertas: Alerta[];
}

export interface FiltroAdmin {
  situacao?: string;
  categoria?: string;
  corretor?: string;
  municipio?: string;
  q?: string;
  limite?: number;
}

/** Payload de cadastro — espelha `CreatePropertyRequest` no backend. */
export interface NovoImovel {
  categoria: 'URBANO' | 'RURAL';
  tipo: string;
  finalidade: string;
  situacao: string;

  titulo: string;
  chamada?: string;
  descricao?: string;

  valores: {
    preco?: number | null;
    condominio?: number | null;
    iptu?: number | null;
    arrendamentoPorHectare?: number | null;
    exibirPreco: boolean;
    textoAlternativo?: string | null;
  };

  areas: {
    valor: number;
    unidade: string;
    areaConstruidaM2?: number | null;
    areaTerrenoM2?: number | null;
  };

  ambientes: {
    dormitorios: number;
    suites: number;
    banheiros: number;
    vagas: number;
  };

  endereco: {
    uf: string;
    municipio: string;
    bairro?: string | null;
    logradouro?: string | null;
    numero?: string | null;
    complemento?: string | null;
    cep?: string | null;
    regiaoSlug?: string | null;
    latitude?: number | null;
    longitude?: number | null;
    exposicao: string;
  };

  caracteristicas: string[];

  corretorSlug?: string | null;
  corretorNome?: string | null;
  proprietario?: string | null;

  destaque: boolean;
  exclusivo: boolean;
  lancamento: boolean;
  oportunidade: boolean;
  aceitaPermuta: boolean;
  aceitaFinanciamento: boolean;

  urbano?: {
    condominioNome?: string | null;
    andar?: number | null;
    anoConstrucao?: number | null;
    mobiliado: boolean;
    aceitaAnimais: boolean;
    transporteProximo: string[];
  } | null;

  rural?: {
    areaProdutivaHa: number;
    areaPastagemHa: number;
    areaCultivadaHa: number;
    areaMataHa: number;
    reservaLegalHa?: number | null;
    areaIrrigadaHa?: number | null;
    terreno: string[];
    soloPredominante?: string | null;
    soloFertilidade?: string | null;
    atividades: string[];
    culturas: {
      nome: string;
      categoria?: string | null;
      areaHa: number;
      safra?: string | null;
      produtividade?: string | null;
    }[];
    pecuaria?: {
      tipoCriacao?: string | null;
      capacidadeCabecas?: number | null;
      divisoesPasto: number;
      estruturas: string[];
    } | null;
    recursosHidricos: string[];
    benfeitorias: {
      tipo: string;
      quantidade: number;
      areaM2?: number | null;
      conservacao?: string | null;
    }[];
    acesso: {
      tipoAcesso?: string | null;
      distanciaAsfaltoKm: number;
      distanciaCidadeKm: number;
      municipioReferencia?: string | null;
      acessoAnoTodo: boolean;
      energiaTrifasica: boolean;
      internet: boolean;
      sinalCelular: boolean;
    };
    documentacao: {
      carInformado: boolean;
      ccirInformado: boolean;
      sigefInformado: boolean;
      matriculaInformada: boolean;
      georreferenciado: boolean;
      situacaoDeclarada?: string | null;

      matriculaNumero?: string | null;
      carReciboNumero?: string | null;
      carDemonstrativoCompletoInformado: boolean;
      carMapaPoligonoInformado: boolean;
      sigefSituacaoGeorreferenciamento?: string | null;
      sigefPlantaMemorialDisponivel: boolean;
      sncrIncraSituacaoCadastral?: string | null;
      cnirCafirCib?: string | null;
      cnirCafirSituacaoCadastral?: string | null;
    };
    fonteInformacao?: string | null;
    dataReferencia?: string | null;
  } | null;
}
