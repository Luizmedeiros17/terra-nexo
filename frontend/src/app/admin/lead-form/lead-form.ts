import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';

import { LeadApiService } from '../../core/api/lead-api.service';
import { CreateLeadPayload, LeadDetail } from '../../shared/models/lead.model';

/** Cadastro manual de um lead ou edição dos seus dados de contato. */
@Component({
  selector: 'tn-admin-lead-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './lead-form.html',
  styleUrl: './lead-form.css',
})
export class LeadForm {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(LeadApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly carregando = signal(false);
  readonly salvo = signal<LeadDetail | null>(null);

  readonly leadId = signal<string | null>(null);
  readonly modoEdicao = computed(() => this.leadId() !== null);

  readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    telefone: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(20)]],
    mensagem: ['', Validators.maxLength(2000)],
    melhorHorario: ['', Validators.maxLength(60)],
    imovelCodigo: [''],
    responsavel: ['', Validators.maxLength(120)],
  });

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.leadId.set(id);
      this.carregando.set(true);
      this.api.detalhe(id).subscribe({
        next: (dados) => {
          this.form.patchValue({
            nome: dados.nome,
            email: dados.email,
            telefone: dados.telefone,
            mensagem: dados.mensagem ?? '',
            melhorHorario: dados.melhorHorario ?? '',
            imovelCodigo: dados.imovelCodigo ?? '',
            responsavel: dados.responsavel ?? '',
          });
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set('Não foi possível carregar este lead para edição.');
          this.carregando.set(false);
        },
      });
    }
  }

  salvar(): void {
    this.erro.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erro.set('Revise os campos destacados antes de continuar.');
      this.focarPrimeiroInvalido();
      return;
    }

    this.salvando.set(true);
    const v = this.form.getRawValue();
    const payload: CreateLeadPayload = {
      nome: v.nome.trim(),
      email: v.email.trim(),
      telefone: v.telefone.trim(),
      mensagem: v.mensagem || null,
      melhorHorario: v.melhorHorario || null,
      imovelCodigo: v.imovelCodigo || null,
      responsavel: v.responsavel || null,
    };
    const id = this.leadId();
    const requisicao = id ? this.api.atualizar(id, payload) : this.api.criar(payload);

    requisicao
      .pipe(
        catchError((resposta) => {
          const mensagem = resposta?.error?.erro
            ?? (resposta?.error?.campos
              ? Object.values(resposta.error.campos).join(' · ')
              : 'Não foi possível salvar o lead.');
          this.erro.set(mensagem);
          return of(null);
        }),
      )
      .subscribe((resultado) => {
        this.salvando.set(false);
        if (resultado) {
          this.salvo.set(resultado);
        }
      });
  }

  private focarPrimeiroInvalido(): void {
    if (typeof document === 'undefined') {
      return;
    }
    queueMicrotask(() => {
      const campo = document.querySelector<HTMLElement>(
        '.formulario .is-invalid input, .formulario .is-invalid select, .formulario .is-invalid textarea',
      );
      campo?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      campo?.focus();
    });
  }
}
