import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.tokens';
import {
  AtualizarStatusPayload,
  CreatePartnerPayload,
  FiltroPartner,
  Opcao,
  PartnerCard,
  PartnerDetail,
  PartnerResumo,
} from '../../shared/models/partner.model';

/** Acesso à base de prospecção de clientes e parceiros (uso interno, ambiente administrativo). */
@Injectable({ providedIn: 'root' })
export class PartnerApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${inject(API_BASE_URL)}/v1/admin/partners`;

  listar(filtros: FiltroPartner = {}): Observable<PartnerCard[]> {
    let params = new HttpParams();
    for (const [chave, valor] of Object.entries(filtros)) {
      if (valor !== null && valor !== undefined && valor !== '') {
        params = params.set(chave, String(valor));
      }
    }
    return this.http.get<PartnerCard[]>(this.base, { params });
  }

  detalhe(id: string): Observable<PartnerDetail> {
    return this.http.get<PartnerDetail>(`${this.base}/${id}`);
  }

  atualizarStatus(id: string, payload: AtualizarStatusPayload): Observable<PartnerDetail> {
    return this.http.patch<PartnerDetail>(`${this.base}/${id}/status`, payload);
  }

  criar(payload: CreatePartnerPayload): Observable<PartnerDetail> {
    return this.http.post<PartnerDetail>(this.base, payload);
  }

  atualizar(id: string, payload: CreatePartnerPayload): Observable<PartnerDetail> {
    return this.http.put<PartnerDetail>(`${this.base}/${id}`, payload);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  sugestoes(imovelId: string, limite = 6): Observable<PartnerCard[]> {
    const params = new HttpParams().set('imovelId', imovelId).set('limite', String(limite));
    return this.http.get<PartnerCard[]>(`${this.base}/sugestoes`, { params });
  }

  setores(): Observable<Opcao[]> {
    return this.http.get<Opcao[]>(`${this.base}/setores`);
  }

  status(): Observable<Opcao[]> {
    return this.http.get<Opcao[]>(`${this.base}/status`);
  }

  resumo(): Observable<PartnerResumo> {
    return this.http.get<PartnerResumo>(`${this.base}/resumo`);
  }
}
