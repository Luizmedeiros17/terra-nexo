import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';

import { BrokerApiService } from '../../core/api/broker-api.service';
import { BrokerDetail, CreateBrokerPayload } from '../../shared/models/broker.model';

/** Cadastro/edição de um corretor da equipe. */
@Component({
  selector: 'tn-admin-broker-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './broker-form.html',
  styleUrl: './broker-form.css',
})
export class BrokerForm {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(BrokerApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly carregando = signal(false);
  readonly salvo = signal<BrokerDetail | null>(null);

  readonly corretorId = signal<string | null>(null);
  readonly modoEdicao = computed(() => this.corretorId() !== null);

  readonly REGIOES = ['Unaí', 'Paracatu', 'Formosa', 'Luziânia', 'Cristalina', 'Catalão', 'João Pinheiro'];

  readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    cargo: ['', Validators.maxLength(120)],
    creci: ['', Validators.maxLength(60)],
    foto: [''],
    bio: ['', Validators.maxLength(2000)],
    email: [''],
    telefone: [''],
    ativo: [true],

    especialidades: this.fb.array<ReturnType<BrokerForm['novaEspecialidade']>>([]),
    regioes: this.fb.array<ReturnType<BrokerForm['novaRegiao']>>([]),
  });

  get especialidades(): FormArray {
    return this.form.get('especialidades') as FormArray;
  }

  get regioes(): FormArray {
    return this.form.get('regioes') as FormArray;
  }

  novaEspecialidade() {
    return this.fb.nonNullable.control('', Validators.required);
  }

  novaRegiao() {
    return this.fb.nonNullable.control('', Validators.required);
  }

  adicionarEspecialidade(): void {
    this.especialidades.push(this.novaEspecialidade());
  }

  removerEspecialidade(indice: number): void {
    this.especialidades.removeAt(indice);
  }

  adicionarRegiao(): void {
    this.regioes.push(this.novaRegiao());
  }

  removerRegiao(indice: number): void {
    this.regioes.removeAt(indice);
  }

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.corretorId.set(id);
      this.carregando.set(true);
      this.api.detalhe(id).subscribe({
        next: (dados) => {
          this.preencherFormulario(dados);
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set('Não foi possível carregar este corretor para edição.');
          this.carregando.set(false);
        },
      });
    }
  }

  private preencherFormulario(b: BrokerDetail): void {
    this.especialidades.clear();
    this.regioes.clear();

    this.form.patchValue({
      nome: b.nome,
      cargo: b.cargo ?? '',
      creci: b.creci ?? '',
      foto: b.foto ?? '',
      bio: b.bio ?? '',
      email: b.email ?? '',
      telefone: b.telefone ?? '',
      ativo: b.ativo,
    });

    for (const e of b.especialidades) {
      this.especialidades.push(this.fb.nonNullable.control(e, Validators.required));
    }
    for (const r of b.regioes) {
      this.regioes.push(this.fb.nonNullable.control(r, Validators.required));
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
    const payload = this.montarPayload();
    const id = this.corretorId();
    const requisicao = id ? this.api.atualizar(id, payload) : this.api.criar(payload);

    requisicao
      .pipe(
        catchError((resposta) => {
          const mensagem = resposta?.error?.erro
            ?? (resposta?.error?.campos
              ? Object.values(resposta.error.campos).join(' · ')
              : 'Não foi possível salvar o corretor.');
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

  private montarPayload(): CreateBrokerPayload {
    const v = this.form.getRawValue();
    return {
      nome: v.nome.trim(),
      cargo: v.cargo || null,
      creci: v.creci || null,
      foto: v.foto || null,
      bio: v.bio || null,
      especialidades: this.especialidades.getRawValue(),
      regioes: this.regioes.getRawValue(),
      email: v.email || null,
      telefone: v.telefone || null,
      ativo: v.ativo,
    };
  }
}
