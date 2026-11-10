import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.tokens';
import { filtrosParaParams } from './query-params';
import { Dashboard, FiltroAdmin, NovoImovel } from '../../shared/models/admin.model';
import { PropertyCard } from '../../shared/models/property.model';

/**
 * Acesso ao ambiente administrativo (seção 23).
 *
 * As rotas `/admin` do backend ainda não exigem autenticação no protótipo; o
 * interceptor de token entra junto com o módulo de autenticação (etapa 1).
 */
@Injectable({ providedIn: 'root' })
export class AdminApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${inject(API_BASE_URL)}/v1/admin`;

  dashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${this.base}/dashboard`);
  }

  imoveis(filtros: FiltroAdmin = {}): Observable<PropertyCard[]> {
    return this.http.get<PropertyCard[]>(`${this.base}/properties`, { params: filtrosParaParams(filtros) });
  }

  cadastrar(imovel: NovoImovel): Observable<PropertyCard> {
    return this.http.post<PropertyCard>(`${this.base}/properties`, imovel);
  }

  /** Dados brutos do imóvel, no mesmo formato do cadastro — usado para pré-preencher a edição. */
  imovelParaEdicao(id: string): Observable<NovoImovel> {
    return this.http.get<NovoImovel>(`${this.base}/properties/${id}`);
  }

  atualizar(id: string, imovel: NovoImovel): Observable<PropertyCard> {
    return this.http.put<PropertyCard>(`${this.base}/properties/${id}`, imovel);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/properties/${id}`);
  }

  situacoes(): Observable<{ valor: string; rotulo: string }[]> {
    return this.http.get<{ valor: string; rotulo: string }[]>(`${this.base}/situacoes`);
  }
}
