import { HttpParams } from '@angular/common/http';

const vazio = (v: unknown): boolean => v === null || v === undefined || v === '';

/**
 * Converte um objeto de filtros em HttpParams, omitindo valores vazios
 * (null, undefined ou string vazia) para manter a URL limpa e rastreável
 * (seção 20). Arrays viram parâmetros repetidos.
 */
export function filtrosParaParams(filtros: object): HttpParams {
  let params = new HttpParams();
  for (const [chave, valor] of Object.entries(filtros)) {
    if (vazio(valor)) {
      continue;
    }
    if (Array.isArray(valor)) {
      for (const item of valor) {
        if (!vazio(item)) {
          params = params.append(chave, String(item));
        }
      }
      continue;
    }
    params = params.set(chave, String(valor));
  }
  return params;
}
