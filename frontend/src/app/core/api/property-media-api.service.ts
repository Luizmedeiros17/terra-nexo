import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.tokens';
import { PropertyMedia } from '../../shared/models/property-media.model';

/** Upload e gerenciamento da galeria de fotos e vídeos de um imóvel (ambiente administrativo). */
@Injectable({ providedIn: 'root' })
export class PropertyMediaApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${inject(API_BASE_URL)}/v1/admin/properties`;

  listar(propertyId: string): Observable<PropertyMedia[]> {
    return this.http.get<PropertyMedia[]>(`${this.base}/${propertyId}/midias`);
  }

  enviar(propertyId: string, arquivo: File, textoAlternativo?: string): Observable<PropertyMedia[]> {
    const form = new FormData();
    form.append('arquivo', arquivo);
    if (textoAlternativo) {
      form.append('textoAlternativo', textoAlternativo);
    }
    return this.http.post<PropertyMedia[]>(`${this.base}/${propertyId}/midias`, form);
  }

  remover(propertyId: string, ordem: number): Observable<PropertyMedia[]> {
    return this.http.delete<PropertyMedia[]>(`${this.base}/${propertyId}/midias/${ordem}`);
  }

  definirCapa(propertyId: string, ordem: number): Observable<PropertyMedia[]> {
    return this.http.patch<PropertyMedia[]>(`${this.base}/${propertyId}/midias/${ordem}/capa`, {});
  }

  mover(propertyId: string, ordem: number, direcao: 'cima' | 'baixo'): Observable<PropertyMedia[]> {
    const params = new HttpParams().set('direcao', direcao);
    return this.http.patch<PropertyMedia[]>(`${this.base}/${propertyId}/midias/${ordem}/mover`, {}, { params });
  }
}
