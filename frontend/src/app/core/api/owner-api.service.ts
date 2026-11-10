import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.tokens';
import { filtrosParaParams } from './query-params';
import { CreateOwnerPayload, FiltroOwner, OwnerCard, OwnerDetail } from '../../shared/models/owner.model';

/** Acesso ao CRUD de proprietários (ambiente administrativo). */
@Injectable({ providedIn: 'root' })
export class OwnerApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${inject(API_BASE_URL)}/v1/admin/owners`;

  listar(filtros: FiltroOwner = {}): Observable<OwnerCard[]> {
    return this.http.get<OwnerCard[]>(this.base, { params: filtrosParaParams(filtros) });
  }

  detalhe(id: string): Observable<OwnerDetail> {
    return this.http.get<OwnerDetail>(`${this.base}/${id}`);
  }

  criar(payload: CreateOwnerPayload): Observable<OwnerDetail> {
    return this.http.post<OwnerDetail>(this.base, payload);
  }

  atualizar(id: string, payload: CreateOwnerPayload): Observable<OwnerDetail> {
    return this.http.put<OwnerDetail>(`${this.base}/${id}`, payload);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
