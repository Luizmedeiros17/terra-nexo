import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';

import { OwnerApiService } from '../../core/api/owner-api.service';
import { CreateOwnerPayload, OwnerDetail } from '../../shared/models/owner.model';

/** Cadastro/edição de um proprietário de imóvel. */
@Component({
  selector: 'tn-admin-owner-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './owner-form.html',
  styleUrl: './owner-form.css',
})
export class OwnerForm {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(OwnerApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly carregando = signal(false);
  readonly salvo = signal<OwnerDetail | null>(null);

  readonly proprietarioId = signal<string | null>(null);
  readonly modoEdicao = computed(() => this.proprietarioId() !== null);

  readonly CIDADES = ['Unaí', 'Paracatu', 'Formosa', 'Luziânia', 'Cristalina', 'Catalão', 'João Pinheiro'];

  readonly form = this.fb.nonNullable.group({
    tipoPessoa: ['FISICA', Validators.required],
    nome: ['', [Validators.required, Validators.maxLength(200)]],
    documento: [''],
    email: [''],
    telefone: [''],
    telefoneAlternativo: [''],

    municipio: ['', Validators.required],
    uf: ['MG', [Validators.required, Validators.minLength(2), Validators.maxLength(2)]],
    logradouro: [''],
    numero: [''],
    complemento: [''],
    bairro: [''],
    cep: [''],

    observacoes: ['', Validators.maxLength(2000)],

    imoveisVinculados: this.fb.array<ReturnType<OwnerForm['novoImovel']>>([]),
  });

  get imoveisVinculados(): FormArray {
    return this.form.get('imoveisVinculados') as FormArray;
  }

  novoImovel() {
    return this.fb.nonNullable.control('', Validators.required);
  }

  adicionarImovel(): void {
    this.imoveisVinculados.push(this.novoImovel());
  }

  removerImovel(indice: number): void {
    this.imoveisVinculados.removeAt(indice);
  }

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.proprietarioId.set(id);
      this.carregando.set(true);
      this.api.detalhe(id).subscribe({
        next: (dados) => {
          this.preencherFormulario(dados);
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set('Não foi possível carregar este proprietário para edição.');
          this.carregando.set(false);
        },
      });
    }
  }

  private preencherFormulario(o: OwnerDetail): void {
    this.imoveisVinculados.clear();

    this.form.patchValue({
      tipoPessoa: o.tipoPessoa,
      nome: o.nome,
      documento: o.documento ?? '',
      email: o.email ?? '',
      telefone: o.telefone ?? '',
      telefoneAlternativo: o.telefoneAlternativo ?? '',
      municipio: o.municipio,
      uf: o.uf,
      logradouro: o.logradouro ?? '',
      numero: o.numero ?? '',
      complemento: o.complemento ?? '',
      bairro: o.bairro ?? '',
      cep: o.cep ?? '',
      observacoes: o.observacoes ?? '',
    });

    for (const codigo of o.imoveisVinculados) {
      this.imoveisVinculados.push(this.fb.nonNullable.control(codigo, Validators.required));
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
    const id = this.proprietarioId();
    const requisicao = id ? this.api.atualizar(id, payload) : this.api.criar(payload);

    requisicao
      .pipe(
        catchError((resposta) => {
          const mensagem = resposta?.error?.erro
            ?? (resposta?.error?.campos
              ? Object.values(resposta.error.campos).join(' · ')
              : 'Não foi possível salvar o proprietário.');
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

  private montarPayload(): CreateOwnerPayload {
    const v = this.form.getRawValue();
    return {
      tipoPessoa: v.tipoPessoa,
      nome: v.nome.trim(),
      documento: v.documento || null,
      email: v.email || null,
      telefone: v.telefone || null,
      telefoneAlternativo: v.telefoneAlternativo || null,

      municipio: v.municipio.trim(),
      uf: v.uf.toUpperCase(),
      logradouro: v.logradouro || null,
      numero: v.numero || null,
      complemento: v.complemento || null,
      bairro: v.bairro || null,
      cep: v.cep || null,

      imoveisVinculados: this.imoveisVinculados.getRawValue(),
      observacoes: v.observacoes || null,
    };
  }
}
