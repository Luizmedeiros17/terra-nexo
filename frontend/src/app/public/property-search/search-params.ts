import { ParamMap, Params } from '@angular/router';

import { SearchFilters } from '../../shared/models/property.model';

/** Parâmetros que aceitam múltiplos valores na URL (?tipo=Casa&tipo=Fazenda). */
const MULTI = [
  'tipo',
  'caracteristica',
  'terreno',
  'atividade',
  'cultura',
  'agua',
  'benfeitoria',
  'documentacao',
] as const;

const NUMERICOS = [
  'precoMin',
  'precoMax',
  'areaMin',
  'areaMax',
  'dormitorios',
  'suites',
  'banheiros',
  'vagas',
  'areaHaMin',
  'areaHaMax',
  'distanciaAsfaltoMax',
  'pagina',
  'tamanho',
] as const;

const BOOLEANOS = ['permuta', 'financiamento', 'pecuaria', 'irrigacao', 'energiaTrifasica'] as const;

const TEXTOS = ['q', 'categoria', 'finalidade', 'uf', 'municipio', 'bairro', 'regiao', 'ordenar'] as const;

/** A URL é a fonte de verdade da busca: daqui sai o objeto de filtros. */
export function lerFiltros(pm: ParamMap): SearchFilters {
  const filtros: Record<string, unknown> = {};

  for (const chave of TEXTOS) {
    const valor = pm.get(chave);
    if (valor) {
      filtros[chave] = valor;
    }
  }
  for (const chave of NUMERICOS) {
    const valor = pm.get(chave);
    if (valor !== null && valor !== '' && !Number.isNaN(Number(valor))) {
      filtros[chave] = Number(valor);
    }
  }
  for (const chave of BOOLEANOS) {
    const valor = pm.get(chave);
    if (valor === 'true') {
      filtros[chave] = true;
    }
  }
  for (const chave of MULTI) {
    const valores = pm.getAll(chave).filter((v) => v !== '');
    if (valores.length) {
      filtros[chave] = valores;
    }
  }

  return filtros as SearchFilters;
}

/** Converte filtros em query params, omitindo vazios e o padrão de página. */
export function paraQueryParams(filtros: SearchFilters): Params {
  const params: Params = {};
  for (const [chave, valor] of Object.entries(filtros)) {
    if (valor === null || valor === undefined || valor === '' || valor === false) {
      continue;
    }
    if (Array.isArray(valor)) {
      if (valor.length) {
        params[chave] = valor;
      }
      continue;
    }
    if (chave === 'pagina' && Number(valor) <= 1) {
      continue;
    }
    params[chave] = valor;
  }
  return params;
}

/** Alterna um valor dentro de um filtro de múltipla escolha. */
export function alternarValor(atual: string[] | undefined, valor: string): string[] {
  const lista = atual ?? [];
  return lista.includes(valor) ? lista.filter((v) => v !== valor) : [...lista, valor];
}

/** Descrição legível de cada filtro ativo, usada nos chips removíveis. */
export interface FiltroAtivo {
  chave: keyof SearchFilters;
  valor?: string;
  rotulo: string;
}

const ROTULOS: Partial<Record<keyof SearchFilters, string>> = {
  q: 'Busca',
  categoria: 'Categoria',
  finalidade: 'Finalidade',
  tipo: 'Tipo',
  municipio: 'Município',
  bairro: 'Bairro',
  uf: 'UF',
  precoMin: 'Preço mínimo',
  precoMax: 'Preço máximo',
  areaMin: 'Área mínima',
  areaMax: 'Área máxima',
  dormitorios: 'Dormitórios',
  suites: 'Suítes',
  banheiros: 'Banheiros',
  vagas: 'Vagas',
  caracteristica: 'Característica',
  permuta: 'Aceita permuta',
  financiamento: 'Aceita financiamento',
  areaHaMin: 'Área mínima (ha)',
  areaHaMax: 'Área máxima (ha)',
  terreno: 'Terreno',
  atividade: 'Atividade',
  cultura: 'Cultura',
  pecuaria: 'Com pecuária',
  agua: 'Recurso hídrico',
  irrigacao: 'Com irrigação',
  energiaTrifasica: 'Energia trifásica',
  distanciaAsfaltoMax: 'Máx. do asfalto (km)',
  benfeitoria: 'Benfeitoria',
  documentacao: 'Documentação',
};

const VALORES_LEGIVEIS: Record<string, string> = {
  URBANO: 'Urbano',
  RURAL: 'Rural',
  VENDA: 'Venda',
  LOCACAO: 'Locação',
  VENDA_LOCACAO: 'Venda ou locação',
  ARRENDAMENTO: 'Arrendamento',
};

export function listarFiltrosAtivos(filtros: SearchFilters): FiltroAtivo[] {
  const ativos: FiltroAtivo[] = [];
  for (const [chave, valor] of Object.entries(filtros) as [keyof SearchFilters, unknown][]) {
    if (chave === 'pagina' || chave === 'ordenar' || chave === 'tamanho') {
      continue;
    }
    if (valor === null || valor === undefined || valor === '' || valor === false) {
      continue;
    }
    const rotulo = ROTULOS[chave] ?? String(chave);
    if (Array.isArray(valor)) {
      for (const item of valor) {
        ativos.push({ chave, valor: item, rotulo: `${rotulo}: ${item}` });
      }
      continue;
    }
    if (valor === true) {
      ativos.push({ chave, rotulo });
      continue;
    }
    const texto = VALORES_LEGIVEIS[String(valor)] ?? String(valor);
    ativos.push({ chave, rotulo: `${rotulo}: ${texto}` });
  }
  return ativos;
}
