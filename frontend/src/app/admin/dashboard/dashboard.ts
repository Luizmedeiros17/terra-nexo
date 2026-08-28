import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';

import { AdminApiService } from '../../core/api/admin-api.service';
import { Contagem, Dashboard as DashboardModel } from '../../shared/models/admin.model';

interface Barra extends Contagem {
  proporcao: number;
}

/**
 * Tela principal da administração (seção 18): indicadores, pendências
 * acionáveis, distribuição da carteira e últimos cadastros.
 */
@Component({
  selector: 'tn-admin-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DecimalPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  private readonly api = inject(AdminApiService);

  readonly dados = toSignal(this.api.dashboard().pipe(catchError(() => of(null))), {
    initialValue: null as DashboardModel | null,
  });

  readonly carregando = computed(() => this.dados() === null);

  /** Percentual de cada categoria na carteira — usado na barra de composição. */
  readonly composicao = computed(() => {
    const i = this.dados()?.imoveis;
    if (!i || i.total === 0) {
      return { urbanoPct: 0, ruralPct: 0 };
    }
    return {
      urbanoPct: (i.urbanos / i.total) * 100,
      ruralPct: (i.rurais / i.total) * 100,
    };
  });

  readonly porSituacao = computed(() => this.barras(this.dados()?.porSituacao ?? []));
  readonly porCorretor = computed(() => this.barras(this.dados()?.porCorretor ?? []));
  readonly porMunicipio = computed(() => this.barras(this.dados()?.porMunicipio ?? []));

  /** Percentual relativo ao maior valor da série — comprimento visual da barra. */
  private barras(itens: Contagem[]): Barra[] {
    const maior = Math.max(1, ...itens.map((i) => i.quantidade));
    return itens.map((i) => ({ ...i, proporcao: (i.quantidade / maior) * 100 }));
  }

  /** Valores grandes ficam ilegíveis por extenso no cartão. */
  formatarValor(valor: number): string {
    if (valor >= 1_000_000_000) {
      return `R$ ${(valor / 1_000_000_000).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} bi`;
    }
    if (valor >= 1_000_000) {
      return `R$ ${(valor / 1_000_000).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} mi`;
    }
    return `R$ ${valor.toLocaleString('pt-BR', { maximumFractionDigits: 0 })}`;
  }
}
