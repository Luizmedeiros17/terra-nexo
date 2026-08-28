import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';

import { OwnerApiService } from '../../core/api/owner-api.service';
import { OwnerDetail } from '../../shared/models/owner.model';

/** Detalhe de um proprietário: dados cadastrais, endereço e imóveis vinculados. */
@Component({
  selector: 'tn-admin-owner-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe],
  templateUrl: './owner-detail.html',
  styleUrl: './owner-detail.css',
})
export class OwnerDetailPage {
  private readonly api = inject(OwnerApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly excluindo = signal(false);

  readonly proprietario = toSignal(
    this.route.paramMap.pipe(
      switchMap((pm) => this.api.detalhe(pm.get('id')!).pipe(catchError(() => of(null as OwnerDetail | null)))),
    ),
    { initialValue: null as OwnerDetail | null },
  );

  excluir(): void {
    const o = this.proprietario();
    if (!o) {
      return;
    }
    const confirmado = typeof window !== 'undefined'
      && window.confirm(`Excluir "${o.nome}" do cadastro de proprietários? Essa ação não pode ser desfeita.`);
    if (!confirmado) {
      return;
    }
    this.excluindo.set(true);
    this.api.excluir(o.id).subscribe({
      next: () => this.router.navigateByUrl('/admin/proprietarios'),
      error: () => {
        this.excluindo.set(false);
        if (typeof window !== 'undefined') {
          window.alert('Não foi possível excluir este proprietário.');
        }
      },
    });
  }
}
