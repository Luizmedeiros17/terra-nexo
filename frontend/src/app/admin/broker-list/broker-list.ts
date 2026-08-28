import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';

import { BrokerApiService } from '../../core/api/broker-api.service';
import { BrokerCard } from '../../shared/models/broker.model';

/** Equipe de corretores — listagem com filtro por status e busca livre. */
@Component({
  selector: 'tn-admin-broker-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './broker-list.html',
  styleUrl: './broker-list.css',
})
export class BrokerList {
  private readonly api = inject(BrokerApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly carregando = signal(true);

  readonly filtros = toSignal(this.route.queryParamMap, { requireSync: true });

  readonly corretores = toSignal(
    this.route.queryParamMap.pipe(
      tap(() => this.carregando.set(true)),
      switchMap((pm) => {
        const ativoParam = pm.get('ativo');
        return this.api
          .listar({
            ativo: ativoParam === '' || ativoParam === null ? undefined : ativoParam === 'true',
            q: pm.get('q') ?? undefined,
          })
          .pipe(catchError(() => of([] as BrokerCard[])));
      }),
      tap(() => this.carregando.set(false)),
    ),
    { initialValue: [] as BrokerCard[] },
  );

  valorDe(chave: string): string {
    return this.filtros().get(chave) ?? '';
  }

  aplicar(chave: string, valor: string): void {
    const params: Record<string, string> = {};
    for (const k of this.filtros().keys) {
      const v = this.filtros().get(k);
      if (v) {
        params[k] = v;
      }
    }
    if (valor) {
      params[chave] = valor;
    } else {
      delete params[chave];
    }
    this.router.navigate(['/admin/corretores'], { queryParams: params });
  }

  aoDigitar(evento: Event): void {
    this.aplicar('q', (evento.target as HTMLInputElement).value.trim());
  }

  aoSelecionar(chave: string, evento: Event): void {
    this.aplicar(chave, (evento.target as HTMLSelectElement).value);
  }

  limpar(): void {
    this.router.navigate(['/admin/corretores']);
  }
}
