import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';

import { OwnerApiService } from '../../core/api/owner-api.service';
import { OwnerCard } from '../../shared/models/owner.model';

/** Cadastro de proprietários de imóveis — listagem com filtro por cidade e busca livre. */
@Component({
  selector: 'tn-admin-owner-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './owner-list.html',
  styleUrl: './owner-list.css',
})
export class OwnerList {
  private readonly api = inject(OwnerApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly carregando = signal(true);

  readonly CIDADES = [
    { valor: '', rotulo: 'Todas as cidades' },
    { valor: 'Unaí', rotulo: 'Unaí' },
    { valor: 'Paracatu', rotulo: 'Paracatu' },
    { valor: 'Formosa', rotulo: 'Formosa' },
    { valor: 'Luziânia', rotulo: 'Luziânia' },
    { valor: 'Cristalina', rotulo: 'Cristalina' },
    { valor: 'Catalão', rotulo: 'Catalão' },
    { valor: 'João Pinheiro', rotulo: 'João Pinheiro' },
  ];

  readonly filtros = toSignal(this.route.queryParamMap, { requireSync: true });

  readonly proprietarios = toSignal(
    this.route.queryParamMap.pipe(
      tap(() => this.carregando.set(true)),
      switchMap((pm) =>
        this.api
          .listar({ cidade: pm.get('cidade') ?? undefined, q: pm.get('q') ?? undefined })
          .pipe(catchError(() => of([] as OwnerCard[]))),
      ),
      tap(() => this.carregando.set(false)),
    ),
    { initialValue: [] as OwnerCard[] },
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
    this.router.navigate(['/admin/proprietarios'], { queryParams: params });
  }

  aoDigitar(evento: Event): void {
    this.aplicar('q', (evento.target as HTMLInputElement).value.trim());
  }

  aoSelecionar(chave: string, evento: Event): void {
    this.aplicar(chave, (evento.target as HTMLSelectElement).value);
  }

  limpar(): void {
    this.router.navigate(['/admin/proprietarios']);
  }
}
