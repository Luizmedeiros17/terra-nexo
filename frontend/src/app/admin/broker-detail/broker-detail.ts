import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';

import { BrokerApiService } from '../../core/api/broker-api.service';
import { BrokerDetail } from '../../shared/models/broker.model';

/** Detalhe de um corretor: dados cadastrais e carteira de imóveis. */
@Component({
  selector: 'tn-admin-broker-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe],
  templateUrl: './broker-detail.html',
  styleUrl: './broker-detail.css',
})
export class BrokerDetailPage {
  private readonly api = inject(BrokerApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly excluindo = signal(false);

  readonly corretor = toSignal(
    this.route.paramMap.pipe(
      switchMap((pm) => this.api.detalhe(pm.get('id')!).pipe(catchError(() => of(null as BrokerDetail | null)))),
    ),
    { initialValue: null as BrokerDetail | null },
  );

  excluir(): void {
    const c = this.corretor();
    if (!c) {
      return;
    }
    const confirmado = typeof window !== 'undefined'
      && window.confirm(`Excluir "${c.nome}" da equipe de corretores? Essa ação não pode ser desfeita.`);
    if (!confirmado) {
      return;
    }
    this.excluindo.set(true);
    this.api.excluir(c.id).subscribe({
      next: () => this.router.navigateByUrl('/admin/corretores'),
      error: () => {
        this.excluindo.set(false);
        if (typeof window !== 'undefined') {
          window.alert('Não foi possível excluir este corretor. Ele pode ter imóveis vinculados.');
        }
      },
    });
  }
}
