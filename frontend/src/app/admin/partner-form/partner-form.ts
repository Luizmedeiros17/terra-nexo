import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';

import { PartnerApiService } from '../../core/api/partner-api.service';
import { CreatePartnerPayload, PartnerDetail } from '../../shared/models/partner.model';

/** Cadastro/edição de um parceiro ou prospect da base comercial. */
@Component({
  selector: 'tn-admin-partner-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './partner-form.html',
  styleUrl: './partner-form.css',
})
export class PartnerForm {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(PartnerApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly carregando = signal(false);
  readonly salvo = signal<PartnerDetail | null>(null);

  readonly parceiroId = signal<string | null>(null);
  readonly modoEdicao = computed(() => this.parceiroId() !== null);

  readonly CIDADES = [
    'Unaí', 'Paracatu', 'Formosa', 'Luziânia', 'Cristalina', 'Catalão', 'João Pinheiro',
  ];

  readonly SETORES = [
    { valor: 'AGROPECUARIA', rotulo: 'Agropecuária' },
    { valor: 'IMOBILIARIA', rotulo: 'Imobiliária' },
    { valor: 'IRRIGACAO', rotulo: 'Irrigação' },
    { valor: 'SILOS_ARMAZENS', rotulo: 'Silos/Armazéns' },
    { valor: 'LEILAO_ANIMAIS', rotulo: 'Leilão de Animais' },
    { valor: 'LATICINIOS', rotulo: 'Laticínios' },
  ];

  readonly TIPOS_CONTATO = [
    { valor: 'TELEFONE_FIXO', rotulo: 'Telefone fixo' },
    { valor: 'TELEFONE_CELULAR', rotulo: 'Telefone celular' },
    { valor: 'WHATSAPP', rotulo: 'WhatsApp' },
    { valor: 'EMAIL', rotulo: 'E-mail' },
  ];

  readonly form = this.fb.nonNullable.group({
    municipio: ['', Validators.required],
    uf: ['MG', [Validators.required, Validators.minLength(2), Validators.maxLength(2)]],
    setor: ['AGROPECUARIA', Validators.required],

    nomeFantasia: ['', [Validators.required, Validators.maxLength(200)]],
    razaoSocial: [''],
    cnpj: [''],
    situacaoCadastral: [''],

    logradouro: [''],
    numero: [''],
    complemento: [''],
    bairro: [''],
    cep: [''],

    site: [''],

    fonteInformacao: [''],
    dataColeta: [''],
    observacoes: ['', Validators.maxLength(2000)],

    titulares: this.fb.array<ReturnType<PartnerForm['novoTitular']>>([]),
    contatos: this.fb.array<ReturnType<PartnerForm['novoContato']>>([]),
  });

  get titulares(): FormArray {
    return this.form.get('titulares') as FormArray;
  }

  get contatos(): FormArray {
    return this.form.get('contatos') as FormArray;
  }

  novoTitular() {
    return this.fb.nonNullable.group({
      nome: ['', Validators.required],
      cpfMascarado: [''],
      qualificacao: [''],
      principal: [false],
    });
  }

  novoContato() {
    return this.fb.nonNullable.group({
      tipo: ['WHATSAPP', Validators.required],
      valor: ['', Validators.required],
      principal: [false],
    });
  }

  adicionarTitular(): void {
    this.titulares.push(this.novoTitular());
  }

  removerTitular(indice: number): void {
    this.titulares.removeAt(indice);
  }

  adicionarContato(): void {
    this.contatos.push(this.novoContato());
  }

  removerContato(indice: number): void {
    this.contatos.removeAt(indice);
  }

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  /** Mesma checagem, para um campo dentro de um item de titulares/contatos. */
  invalidoItem(array: FormArray, indice: number, campo: string): boolean {
    const c = array.at(indice)?.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.parceiroId.set(id);
      this.carregando.set(true);
      this.api.detalhe(id).subscribe({
        next: (dados) => {
          this.preencherFormulario(dados);
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set('Não foi possível carregar este parceiro para edição.');
          this.carregando.set(false);
        },
      });
    }
  }

  private preencherFormulario(p: PartnerDetail): void {
    this.titulares.clear();
    this.contatos.clear();

    this.form.patchValue({
      municipio: p.municipio,
      uf: p.uf,
      setor: p.setor,
      nomeFantasia: p.nomeFantasia,
      razaoSocial: p.razaoSocial ?? '',
      cnpj: p.cnpj ?? '',
      situacaoCadastral: p.situacaoCadastral ?? '',
      logradouro: p.logradouro ?? '',
      numero: p.numero ?? '',
      complemento: p.complemento ?? '',
      bairro: p.bairro ?? '',
      cep: p.cep ?? '',
      site: p.site ?? '',
      fonteInformacao: p.fonteInformacao ?? '',
      dataColeta: p.dataColeta ?? '',
      observacoes: p.observacoes ?? '',
    });

    for (const t of p.titulares) {
      const grupo = this.novoTitular();
      grupo.patchValue({
        nome: t.nome,
        cpfMascarado: t.cpfMascarado ?? '',
        qualificacao: t.qualificacao ?? '',
        principal: t.principal,
      });
      this.titulares.push(grupo);
    }
    for (const c of p.contatos) {
      const grupo = this.novoContato();
      grupo.patchValue({ tipo: c.tipo, valor: c.valor, principal: c.principal });
      this.contatos.push(grupo);
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
    const id = this.parceiroId();
    const requisicao = id ? this.api.atualizar(id, payload) : this.api.criar(payload);

    requisicao
      .pipe(
        catchError((resposta) => {
          const mensagem = resposta?.error?.erro
            ?? (resposta?.error?.campos
              ? Object.values(resposta.error.campos).join(' · ')
              : 'Não foi possível salvar o parceiro.');
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

  /** Leva o usuário direto ao primeiro campo que está bloqueando o envio — inclusive
   * dentro de titulares/contatos, onde antes não havia nenhum indicativo visual. */
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

  private montarPayload(): CreatePartnerPayload {
    const v = this.form.getRawValue();
    return {
      municipio: v.municipio.trim(),
      uf: v.uf.toUpperCase(),
      setor: v.setor,

      nomeFantasia: v.nomeFantasia.trim(),
      razaoSocial: v.razaoSocial || null,
      cnpj: v.cnpj || null,
      situacaoCadastral: v.situacaoCadastral || null,

      logradouro: v.logradouro || null,
      numero: v.numero || null,
      complemento: v.complemento || null,
      bairro: v.bairro || null,
      cep: v.cep || null,

      site: v.site || null,

      titulares: this.titulares.getRawValue(),
      contatos: this.contatos.getRawValue(),

      fonteInformacao: v.fonteInformacao || null,
      dataColeta: v.dataColeta || null,
      observacoes: v.observacoes || null,
    };
  }
}
