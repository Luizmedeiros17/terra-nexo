import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';

import { AdminApiService } from '../../core/api/admin-api.service';
import { PartnerApiService } from '../../core/api/partner-api.service';
import { PropertyCard } from '../../shared/models/property.model';
import { PartnerCard } from '../../shared/models/partner.model';

/** Listagem administrativa com os filtros da seção 18. */
@Component({
  selector: 'tn-admin-property-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './property-list.html',
  styleUrl: './property-list.css',
})
export class PropertyList {
  private readonly api = inject(AdminApiService);
  private readonly partnerApi = inject(PartnerApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly carregando = signal(true);

  /** Painel expansível de "parceiros da região" (imóveis rurais) — ver PartnerSuggestionService no backend. */
  readonly imovelExpandido = signal<string | null>(null);
  readonly carregandoSugestoes = signal(false);
  readonly sugestoes = signal<PartnerCard[]>([]);

  readonly SITUACOES = [
    { valor: '', rotulo: 'Todas as situações' },
    { valor: 'PUBLICADO', rotulo: 'Publicado' },
    { valor: 'RASCUNHO', rotulo: 'Rascunho' },
    { valor: 'AGUARDANDO_REVISAO', rotulo: 'Aguardando revisão' },
    { valor: 'SUSPENSO', rotulo: 'Suspenso' },
    { valor: 'VENDIDO', rotulo: 'Vendido' },
    { valor: 'ALUGADO', rotulo: 'Alugado' },
  ];

  readonly filtros = toSignal(this.route.queryParamMap, { requireSync: true });

  /** Signal próprio (não derivado de toSignal) para permitir remoção otimista após excluir. */
  readonly imoveis = signal<PropertyCard[]>([]);

  constructor() {
    this.route.queryParamMap
      .pipe(
        tap(() => this.carregando.set(true)),
        switchMap((pm) =>
          this.api
            .imoveis({
              situacao: pm.get('situacao') ?? undefined,
              categoria: pm.get('categoria') ?? undefined,
              q: pm.get('q') ?? undefined,
              limite: 60,
            })
            .pipe(catchError(() => of([] as PropertyCard[]))),
        ),
      )
      .subscribe((lista) => {
        this.imoveis.set(lista);
        this.carregando.set(false);
      });
  }

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
    this.router.navigate(['/admin/imoveis'], { queryParams: params });
  }

  aoDigitar(evento: Event): void {
    this.aplicar('q', (evento.target as HTMLInputElement).value.trim());
  }

  aoSelecionar(chave: string, evento: Event): void {
    this.aplicar(chave, (evento.target as HTMLSelectElement).value);
  }

  limpar(): void {
    this.router.navigate(['/admin/imoveis']);
  }

  alternarSugestoes(imovel: PropertyCard): void {
    if (this.imovelExpandido() === imovel.id) {
      this.imovelExpandido.set(null);
      return;
    }
    this.imovelExpandido.set(imovel.id);
    this.sugestoes.set([]);
    this.carregandoSugestoes.set(true);
    this.partnerApi
      .sugestoes(imovel.id)
      .pipe(catchError(() => of([] as PartnerCard[])))
      .subscribe((lista) => {
        this.sugestoes.set(lista);
        this.carregandoSugestoes.set(false);
      });
  }

  excluir(imovel: PropertyCard): void {
    const confirmado = typeof window !== 'undefined'
      && window.confirm(`Excluir o imóvel "${imovel.titulo}"? Essa ação não pode ser desfeita.`);
    if (!confirmado) {
      return;
    }
    this.api.excluir(imovel.id).subscribe({
      next: () => this.imoveis.update((lista) => lista.filter((i) => i.id !== imovel.id)),
      error: () => {
        if (typeof window !== 'undefined') {
          window.alert('Não foi possível excluir este imóvel.');
        }
      },
    });
  }

  rotuloSituacao(situacao: string): string {
    return this.SITUACOES.find((s) => s.valor === situacao)?.rotulo
      ?? situacao.charAt(0) + situacao.slice(1).toLowerCase().replace('_', ' ');
  }
}
