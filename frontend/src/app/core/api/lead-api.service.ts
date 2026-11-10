import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.tokens';
import { filtrosParaParams } from './query-params';
import {
  AtualizarEtapaPayload,
  CreateLeadPayload,
  FiltroLead,
  LeadCard,
  LeadDetail,
  Opcao,
} from '../../shared/models/lead.model';

/** Acesso ao CRUD/funil de leads (ambiente administrativo). */
@Injectable({ providedIn: 'root' })
export class LeadApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${inject(API_BASE_URL)}/v1/admin/leads`;

  listar(filtros: FiltroLead = {}): Observable<LeadCard[]> {
    return this.http.get<LeadCard[]>(this.base, { params: filtrosParaParams(filtros) });
  }

  detalhe(id: string): Observable<LeadDetail> {
    return this.http.get<LeadDetail>(`${this.base}/${id}`);
  }

  criar(payload: CreateLeadPayload): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(this.base, payload);
  }

  atualizar(id: string, payload: CreateLeadPayload): Observable<LeadDetail> {
    return this.http.put<LeadDetail>(`${this.base}/${id}`, payload);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  atualizarEtapa(id: string, payload: AtualizarEtapaPayload): Observable<LeadDetail> {
    return this.http.patch<LeadDetail>(`${this.base}/${id}/etapa`, payload);
  }

  etapas(): Observable<Opcao[]> {
    return this.http.get<Opcao[]>(`${this.base}/etapas`);
  }

  origens(): Observable<Opcao[]> {
    return this.http.get<Opcao[]>(`${this.base}/origens`);
  }
}
