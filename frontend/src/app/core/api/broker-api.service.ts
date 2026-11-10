import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.tokens';
import { filtrosParaParams } from './query-params';
import { BrokerCard, BrokerDetail, CreateBrokerPayload, FiltroBroker } from '../../shared/models/broker.model';

/** Acesso ao CRUD de corretores (ambiente administrativo). */
@Injectable({ providedIn: 'root' })
export class BrokerApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${inject(API_BASE_URL)}/v1/admin/brokers`;

  listar(filtros: FiltroBroker = {}): Observable<BrokerCard[]> {
    return this.http.get<BrokerCard[]>(this.base, { params: filtrosParaParams(filtros) });
  }

  detalhe(id: string): Observable<BrokerDetail> {
    return this.http.get<BrokerDetail>(`${this.base}/${id}`);
  }

  criar(payload: CreateBrokerPayload): Observable<BrokerDetail> {
    return this.http.post<BrokerDetail>(this.base, payload);
  }

  atualizar(id: string, payload: CreateBrokerPayload): Observable<BrokerDetail> {
    return this.http.put<BrokerDetail>(`${this.base}/${id}`, payload);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
