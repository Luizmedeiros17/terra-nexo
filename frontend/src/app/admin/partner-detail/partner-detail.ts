import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';

import { PartnerApiService } from '../../core/api/partner-api.service';
import { PartnerDetail } from '../../shared/models/partner.model';

/** Detalhe de um parceiro/prospect: dados coletados, contatos e atualização do funil. */
@Component({
  selector: 'tn-admin-partner-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './partner-detail.html',
  styleUrl: './partner-detail.css',
})
export class PartnerDetailPage {
  private readonly api = inject(PartnerApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly excluindo = signal(false);

  readonly STATUS = [
    { valor: 'NAO_INICIADO', rotulo: 'Não iniciado' },
    { valor: 'PESQUISADO', rotulo: 'Pesquisado' },
    { valor: 'CONTATADO', rotulo: 'Contatado' },
    { valor: 'RESPONDEU', rotulo: 'Respondeu' },
    { valor: 'REUNIAO_MARCADA', rotulo: 'Reunião marcada' },
    { valor: 'PROPOSTA_ENVIADA', rotulo: 'Proposta enviada' },
    { valor: 'CONVERTIDO', rotulo: 'Convertido' },
    { valor: 'SEM_INTERESSE', rotulo: 'Sem interesse' },
    { valor: 'CONTATO_INVALIDO', rotulo: 'Contato inválido' },
  ];

  private readonly recarregar = signal(0);
  readonly salvando = signal(false);
  readonly erroSalvar = signal<string | null>(null);

  readonly parceiro = toSignal(
    this.route.paramMap.pipe(
      switchMap((pm) => {
        this.recarregar();
        return this.api.detalhe(pm.get('id')!).pipe(catchError(() => of(null as PartnerDetail | null)));
      }),
    ),
    { initialValue: null as PartnerDetail | null },
  );

  readonly form = this.fb.nonNullable.group({
    status: ['', Validators.required],
    responsavel: ['Luiz Medeiros', [Validators.required, Validators.maxLength(120)]],
    observacao: ['', Validators.maxLength(2000)],
  });

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  atualizar(): void {
    const p = this.parceiro();
    if (!p) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erroSalvar.set('Revise os campos destacados antes de continuar.');
      this.focarPrimeiroInvalido();
      return;
    }
    this.salvando.set(true);
    this.erroSalvar.set(null);
    const valores = this.form.getRawValue();
    this.api.atualizarStatus(p.id, valores).subscribe({
      next: () => {
        this.salvando.set(false);
        this.form.reset({ status: '', responsavel: valores.responsavel, observacao: '' });
        this.recarregar.update((v) => v + 1);
      },
      error: () => {
        this.salvando.set(false);
        this.erroSalvar.set('Não foi possível atualizar o status. Tente novamente.');
      },
    });
  }

  excluir(): void {
    const p = this.parceiro();
    if (!p) {
      return;
    }
    const confirmado = typeof window !== 'undefined'
      && window.confirm(`Excluir "${p.nomeFantasia}" da base de prospecção? Essa ação não pode ser desfeita.`);
    if (!confirmado) {
      return;
    }
    this.excluindo.set(true);
    this.api.excluir(p.id).subscribe({
      next: () => this.router.navigateByUrl('/admin/parceiros'),
      error: () => {
        this.excluindo.set(false);
        if (typeof window !== 'undefined') {
          window.alert('Não foi possível excluir este parceiro.');
        }
      },
    });
  }

  private focarPrimeiroInvalido(): void {
    if (typeof document === 'undefined') {
      return;
    }
    queueMicrotask(() => {
      const campo = document.querySelector<HTMLElement>(
        '.form-status .is-invalid input, .form-status .is-invalid select, .form-status .is-invalid textarea',
      );
      campo?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      campo?.focus();
    });
  }

  contatosPorTipo(p: PartnerDetail, tipo: string) {
    return p.contatos.filter((c) => c.tipo === tipo);
  }

  historicoRecente(p: PartnerDetail) {
    return [...p.historicoStatus].reverse();
  }
}
