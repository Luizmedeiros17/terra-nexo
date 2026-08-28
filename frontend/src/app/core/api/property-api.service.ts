import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.tokens';
import {
  Corretor,
  LeadRequest,
  LeadResponse,
  PropertyCard,
  PropertyDetail,
  Regiao,
  SearchFilters,
  SearchResponse,
} from '../../shared/models/property.model';

/** Acesso aos endpoints públicos do portal (seção 23 da especificação). */
@Injectable({ providedIn: 'root' })
export class PropertyApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${inject(API_BASE_URL)}/v1/public`;

  buscar(filtros: SearchFilters): Observable<SearchResponse> {
    return this.http.get<SearchResponse>(`${this.base}/properties`, {
      params: toHttpParams(filtros),
    });
  }

  destaques(categoria?: 'URBANO' | 'RURAL', limite = 6): Observable<PropertyCard[]> {
    let params = new HttpParams().set('limite', limite);
    if (categoria) {
      params = params.set('categoria', categoria);
    }
    return this.http.get<PropertyCard[]>(`${this.base}/properties/featured`, { params });
  }

  recentes(limite = 6): Observable<PropertyCard[]> {
    return this.http.get<PropertyCard[]>(`${this.base}/properties/recent`, {
      params: new HttpParams().set('limite', limite),
    });
  }

  porCodigoOuSlug(codeOrSlug: string): Observable<PropertyDetail> {
    return this.http.get<PropertyDetail>(`${this.base}/properties/${encodeURIComponent(codeOrSlug)}`);
  }

  semelhantes(id: string, limite = 3): Observable<PropertyCard[]> {
    return this.http.get<PropertyCard[]>(`${this.base}/properties/similar/${id}`, {
      params: new HttpParams().set('limite', limite),
    });
  }

  regioes(): Observable<Regiao[]> {
    return this.http.get<Regiao[]>(`${this.base}/regions`);
  }

  corretores(): Observable<Corretor[]> {
    return this.http.get<Corretor[]>(`${this.base}/brokers`);
  }

  enviarLead(lead: LeadRequest): Observable<LeadResponse> {
    return this.http.post<LeadResponse>(`${this.base}/leads`, lead);
  }
}

/**
 * Converte os filtros em query string, omitindo vazios para manter a URL
 * limpa e rastreável (seção 20). Arrays viram parâmetros repetidos.
 */
export function toHttpParams(filtros: SearchFilters): HttpParams {
  let params = new HttpParams();
  for (const [chave, valor] of Object.entries(filtros)) {
    if (valor === null || valor === undefined || valor === '') {
      continue;
    }
    if (Array.isArray(valor)) {
      for (const item of valor) {
        if (item !== null && item !== undefined && item !== '') {
          params = params.append(chave, String(item));
        }
      }
      continue;
    }
    params = params.set(chave, String(valor));
  }
  return params;
}
