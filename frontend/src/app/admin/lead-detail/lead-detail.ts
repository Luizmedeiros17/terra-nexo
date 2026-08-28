import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';

import { LeadApiService } from '../../core/api/lead-api.service';
import { LeadDetail } from '../../shared/models/lead.model';

/** Detalhe de um lead: dados de contato, origem e atualização da etapa do funil. */
@Component({
  selector: 'tn-admin-lead-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './lead-detail.html',
  styleUrl: './lead-detail.css',
})
export class LeadDetailPage {
  private readonly api = inject(LeadApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly excluindo = signal(false);

  readonly ETAPAS = [
    { valor: 'NOVO', rotulo: 'Novo' },
    { valor: 'AGUARDANDO_ATENDIMENTO', rotulo: 'Aguardando atendimento' },
    { valor: 'CONTATO_INICIADO', rotulo: 'Contato iniciado' },
    { valor: 'SEM_RESPOSTA', rotulo: 'Sem resposta' },
    { valor: 'QUALIFICADO', rotulo: 'Qualificado' },
    { valor: 'VISITA_AGENDADA', rotulo: 'Visita agendada' },
    { valor: 'VISITA_REALIZADA', rotulo: 'Visita realizada' },
    { valor: 'PROPOSTA', rotulo: 'Proposta' },
    { valor: 'NEGOCIACAO', rotulo: 'Negociação' },
    { valor: 'CONVERTIDO', rotulo: 'Convertido' },
    { valor: 'PERDIDO', rotulo: 'Perdido' },
    { valor: 'DUPLICADO', rotulo: 'Duplicado' },
    { valor: 'DESCARTADO', rotulo: 'Descartado' },
  ];

  private readonly recarregar = signal(0);
  readonly salvando = signal(false);
  readonly erroSalvar = signal<string | null>(null);

  readonly lead = toSignal(
    this.route.paramMap.pipe(
      switchMap((pm) => {
        this.recarregar();
        return this.api.detalhe(pm.get('id')!).pipe(catchError(() => of(null as LeadDetail | null)));
      }),
    ),
    { initialValue: null as LeadDetail | null },
  );

  readonly form = this.fb.nonNullable.group({
    etapa: ['', Validators.required],
    responsavel: ['Luiz Medeiros', Validators.maxLength(120)],
  });

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  atualizar(): void {
    const l = this.lead();
    if (!l) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erroSalvar.set('Selecione a nova etapa antes de continuar.');
      return;
    }
    this.salvando.set(true);
    this.erroSalvar.set(null);
    const valores = this.form.getRawValue();
    this.api.atualizarEtapa(l.id, valores).subscribe({
      next: () => {
        this.salvando.set(false);
        this.recarregar.update((v) => v + 1);
      },
      error: () => {
        this.salvando.set(false);
        this.erroSalvar.set('Não foi possível atualizar a etapa. Tente novamente.');
      },
    });
  }

  excluir(): void {
    const l = this.lead();
    if (!l) {
      return;
    }
    const confirmado = typeof window !== 'undefined'
      && window.confirm(`Excluir o lead de "${l.nome}"? Essa ação não pode ser desfeita.`);
    if (!confirmado) {
      return;
    }
    this.excluindo.set(true);
    this.api.excluir(l.id).subscribe({
      next: () => this.router.navigateByUrl('/admin/leads'),
      error: () => {
        this.excluindo.set(false);
        if (typeof window !== 'undefined') {
          window.alert('Não foi possível excluir este lead.');
        }
      },
    });
  }
}
