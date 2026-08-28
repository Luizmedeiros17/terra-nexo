import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Params, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';

import { FiltersPanel } from './filters-panel/filters-panel';
import { PropertyApiService } from '../../core/api/property-api.service';
import { PropertyCardComponent } from '../../shared/components/property-card/property-card';
import { SearchFilters, SearchResponse } from '../../shared/models/property.model';
import { SeoService } from '../../core/seo/seo.service';
import {
  FiltroAtivo,
  alternarValor,
  listarFiltrosAtivos,
  lerFiltros,
  paraQueryParams,
} from './search-params';

/**
 * Módulo de resultado de busca (seção 13).
 *
 * A URL é a fonte de verdade: todo filtro navega, o que mantém os parâmetros
 * preservados, o botão voltar funcionando e as páginas rastreáveis. A renderização
 * acontece no servidor — os anúncios chegam no HTML, sem depender de JavaScript.
 */
@Component({
  selector: 'tn-property-search',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PropertyCardComponent, FiltersPanel],
  templateUrl: './property-search.html',
  styleUrl: './property-search.css',
})
export class PropertySearch {
  private readonly api = inject(PropertyApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly seo = inject(SeoService);

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly painelAberto = signal(false);

  /** Filtros correntes, derivados da query string. */
  readonly filtros = toSignal(
    this.route.queryParamMap.pipe(switchMap((pm) => of(lerFiltros(pm)))),
    { initialValue: {} as SearchFilters },
  );

  readonly resultado = toSignal(
    this.route.queryParamMap.pipe(
      tap(() => {
        this.carregando.set(true);
        this.erro.set(null);
      }),
      switchMap((pm) =>
        this.api.buscar(lerFiltros(pm)).pipe(
          catchError(() => {
            this.erro.set('Não foi possível carregar os imóveis. Tente novamente em instantes.');
            return of(null);
          }),
        ),
      ),
      tap(() => this.carregando.set(false)),
    ),
    { initialValue: null as SearchResponse | null },
  );

  readonly filtrosAtivos = computed<FiltroAtivo[]>(() => listarFiltrosAtivos(this.filtros()));

  readonly mostrarFiltrosRurais = computed(() => {
    const f = this.filtros();
    return f.categoria === 'RURAL';
  });

  /** Páginas exibidas na paginação, com reticências quando a lista é longa. */
  readonly paginas = computed<(number | '…')[]>(() => {
    const p = this.resultado()?.paginacao;
    if (!p) {
      return [];
    }
    const total = p.totalPaginas;
    const atual = p.pagina;
    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }
    const itens: (number | '…')[] = [1];
    const inicio = Math.max(2, atual - 1);
    const fim = Math.min(total - 1, atual + 1);
    if (inicio > 2) {
      itens.push('…');
    }
    for (let i = inicio; i <= fim; i++) {
      itens.push(i);
    }
    if (fim < total - 1) {
      itens.push('…');
    }
    itens.push(total);
    return itens;
  });

  constructor() {
    effect(() => {
      const r = this.resultado();
      if (!r) {
        return;
      }
      const caminho = this.caminhoCanonico(this.filtros());
      this.seo.aplicar({
        title: r.seo.titulo,
        description: r.seo.descricao,
        // Combinações arbitrárias apontam o canonical para a listagem base (seção 20).
        canonicalPath: r.seo.indexavel ? caminho : '/imoveis',
        index: r.seo.indexavel,
        image: r.conteudo[0]?.imagem.url,
      });

      const p = r.paginacao;
      this.seo.paginacao(
        p.primeira ? null : this.caminhoCanonico({ ...this.filtros(), pagina: p.pagina - 1 }),
        p.ultima ? null : this.caminhoCanonico({ ...this.filtros(), pagina: p.pagina + 1 }),
      );

      this.seo.jsonLd('resultados', {
        '@context': 'https://schema.org',
        '@type': 'ItemList',
        name: r.seo.h1,
        numberOfItems: r.paginacao.totalElementos,
        itemListElement: r.conteudo.map((im, i) => ({
          '@type': 'ListItem',
          position: (p.pagina - 1) * p.tamanho + i + 1,
          url: `${this.seo.siteUrl}${im.url}`,
          name: im.titulo,
        })),
      });
    });
  }

  // ------------------------------------------------------------------
  // Navegação: toda mudança de filtro reescreve a URL
  // ------------------------------------------------------------------

  aplicar(parciais: Partial<SearchFilters>): void {
    const novos: SearchFilters = { ...this.filtros(), ...parciais, pagina: 1 };
    this.navegar(novos);
  }

  alternarMulti(chave: keyof SearchFilters, valor: string): void {
    const atual = this.filtros()[chave] as string[] | undefined;
    this.aplicar({ [chave]: alternarValor(atual, valor) } as Partial<SearchFilters>);
  }

  remover(filtro: FiltroAtivo): void {
    const atuais: Record<string, unknown> = { ...this.filtros() };
    if (filtro.valor) {
      const lista = (atuais[filtro.chave] as string[] | undefined) ?? [];
      atuais[filtro.chave] = lista.filter((v) => v !== filtro.valor);
    } else {
      delete atuais[filtro.chave];
    }
    this.navegar({ ...(atuais as SearchFilters), pagina: 1 });
  }

  limparTudo(): void {
    this.router.navigate(['/imoveis']);
  }

  ordenarPor(valor: string): void {
    this.navegar({ ...this.filtros(), ordenar: valor, pagina: 1 });
  }

  /** Query params da paginação — usados em <a routerLink>, links reais para o rastreador. */
  queryParamsDaPagina(pagina: number): Params {
    return paraQueryParams({ ...this.filtros(), pagina });
  }

  private navegar(filtros: SearchFilters): void {
    this.router.navigate(['/imoveis'], { queryParams: paraQueryParams(filtros) });
  }

  private caminhoCanonico(filtros: SearchFilters): string {
    const params = paraQueryParams(filtros);
    const qs = new URLSearchParams();
    for (const [chave, valor] of Object.entries(params)) {
      if (Array.isArray(valor)) {
        valor.forEach((v) => qs.append(chave, String(v)));
      } else {
        qs.set(chave, String(valor));
      }
    }
    const query = qs.toString();
    return query ? `/imoveis?${query}` : '/imoveis';
  }

  alternarPainel(): void {
    this.painelAberto.update((v) => !v);
  }
}
