import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, firstValueFrom, of } from 'rxjs';

import { AdminApiService } from '../../core/api/admin-api.service';
import { BrokerApiService } from '../../core/api/broker-api.service';
import { CepService } from '../../core/api/cep.service';
import { PropertyMediaApiService } from '../../core/api/property-media-api.service';
import { NovoImovel } from '../../shared/models/admin.model';
import { PropertyCard } from '../../shared/models/property.model';
import { PropertyMedia } from '../../shared/models/property-media.model';

/**
 * Cadastro de imóvel (seções 8, 9 e 10).
 *
 * O bloco específico segue a categoria: urbano exibe condomínio, andar e
 * transporte; rural exibe terreno, solo, culturas, pecuária, água, benfeitorias,
 * acesso e documentação declarada. Rascunhos podem ficar incompletos — a
 * validação exigente só vale para publicar (regras 24.1 e 24.2).
 */
@Component({
  selector: 'tn-admin-property-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './property-form.html',
  styleUrl: './property-form.css',
})
export class PropertyForm {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(AdminApiService);
  private readonly cepService = inject(CepService);
  private readonly brokerApi = inject(BrokerApiService);
  private readonly mediaApi = inject(PropertyMediaApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly criado = signal<PropertyCard | null>(null);

  /** Presente só em modo edição (rota /admin/imoveis/:id/editar). */
  readonly imovelId = signal<string | null>(null);
  readonly modoEdicao = computed(() => this.imovelId() !== null);
  readonly carregando = signal(false);

  // ---- Galeria de fotos e vídeos (só disponível depois que o imóvel existe) ----
  readonly midias = signal<PropertyMedia[]>([]);
  readonly enviandoMidia = signal(false);
  readonly erroMidia = signal<string | null>(null);

  // ---- Vocabulários (viriam do cadastro configurável, seção 8) ----
  readonly TIPOS_URBANOS = [
    'Apartamento', 'Casa', 'Casa em condomínio', 'Cobertura', 'Kitnet', 'Loft',
    'Terreno', 'Sala comercial', 'Loja', 'Galpão', 'Prédio', 'Chácara urbana',
  ];
  readonly TIPOS_RURAIS = [
    'Fazenda', 'Sítio', 'Chácara', 'Estância', 'Haras', 'Área rural',
    'Propriedade agrícola', 'Propriedade pecuária', 'Propriedade agropecuária',
    'Área de reflorestamento', 'Área de preservação',
  ];
  readonly EXPOSICOES = [
    { valor: 'ENDERECO_COMPLETO', rotulo: 'Endereço completo público' },
    { valor: 'ENDERECO_PARCIAL', rotulo: 'Endereço parcial (sem número)' },
    { valor: 'BAIRRO_MUNICIPIO', rotulo: 'Bairro e município' },
    { valor: 'SOMENTE_MUNICIPIO', rotulo: 'Somente município' },
  ];
  readonly TERRENOS = ['Plano', 'Semiplano', 'Ondulado', 'Montanhoso', 'Várzea', 'Cerrado', 'Campo', 'Mata', 'Área alagável'];
  readonly ATIVIDADES = ['Agricultura', 'Pecuária', 'Silvicultura', 'Horticultura', 'Fruticultura', 'Lazer', 'Preservação'];
  readonly AGUAS = ['Rio', 'Córrego', 'Nascente', 'Lago', 'Represa', 'Açude', 'Poço artesiano', 'Cisterna', 'Irrigação'];
  readonly ESTRUTURAS_PECUARIA = ['Curral', 'Confinamento', 'Embarcadouro', 'Mangueira', 'Balança', 'Cocheiras', 'Enfermaria'];
  readonly COMODIDADES_URBANAS = ['Piscina', 'Churrasqueira', 'Varanda gourmet', 'Área gourmet', 'Elevador', 'Academia', 'Portaria 24h', 'Salão de festas', 'Playground', 'Mobiliado', 'Aceita pet', 'Energia solar', 'Acessibilidade'];
  readonly COMODIDADES_RURAIS = ['Casa sede', 'Energia trifásica', 'Internet', 'Pista de pouso', 'Balança', 'Estradas internas', 'Sistema de irrigação', 'Gerador'];
  readonly CONSERVACAO = ['Ótimo', 'Bom', 'Regular', 'Ruim', 'Em construção'];
  readonly CATEGORIAS_CULTURA = ['Cultura anual', 'Cultura perene', 'Fruticultura', 'Horticultura', 'Silvicultura', 'Pastagem'];
  /** Fallback caso a API de corretores esteja indisponível. */
  private readonly CORRETORES_PADRAO = [{ slug: 'luiz-medeiros', nome: 'Luiz Medeiros' }];

  /** Corretores ativos cadastrados — alimenta o seletor "Corretor responsável". */
  readonly CORRETORES = signal<{ slug: string; nome: string }[]>(this.CORRETORES_PADRAO);

  readonly form = this.fb.nonNullable.group({
    categoria: ['URBANO' as 'URBANO' | 'RURAL', Validators.required],
    tipo: ['Apartamento', Validators.required],
    finalidade: ['VENDA', Validators.required],

    titulo: ['', [Validators.required, Validators.maxLength(160)]],
    chamada: ['', Validators.maxLength(300)],
    descricao: [''],

    preco: [null as number | null],
    condominio: [null as number | null],
    iptu: [null as number | null],
    arrendamentoPorHectare: [null as number | null],
    exibirPreco: [true],
    textoAlternativo: ['Consulte-nos'],

    // O mínimo espelha o @Positive do backend (>0) — min(1) rejeitava sítios
    // pequenos com menos de 1 hectare/alqueire, mesmo sendo um valor válido.
    areaValor: [null as number | null, [Validators.required, Validators.min(0.01)]],
    areaUnidade: ['m²', Validators.required],
    areaConstruidaM2: [null as number | null],
    areaTerrenoM2: [null as number | null],

    dormitorios: [0],
    suites: [0],
    banheiros: [0],
    vagas: [0],

    uf: ['SP', [Validators.required, Validators.minLength(2), Validators.maxLength(2)]],
    municipio: ['', Validators.required],
    bairro: [''],
    logradouro: [''],
    numero: [''],
    complemento: [''],
    cep: ['', Validators.pattern(/^$|^\d{5}-?\d{3}$/)],
    regiaoSlug: [''],
    latitude: [null as number | null],
    longitude: [null as number | null],
    exposicao: ['BAIRRO_MUNICIPIO', Validators.required],

    caracteristicas: [[] as string[]],

    corretorSlug: ['luiz-medeiros'],
    proprietario: [''],

    destaque: [false],
    exclusivo: [false],
    lancamento: [false],
    oportunidade: [false],
    aceitaPermuta: [false],
    aceitaFinanciamento: [true],

    // ---- Bloco urbano (seção 9) ----
    condominioNome: [''],
    andar: [null as number | null],
    anoConstrucao: [null as number | null],
    mobiliado: [false],
    aceitaAnimais: [false],
    transporteProximo: [''],

    // ---- Bloco rural (seção 10) ----
    areaProdutivaHa: [0],
    areaPastagemHa: [0],
    areaCultivadaHa: [0],
    areaMataHa: [0],
    reservaLegalHa: [null as number | null],
    areaIrrigadaHa: [null as number | null],
    terreno: [[] as string[]],
    soloPredominante: [''],
    soloFertilidade: [''],
    atividades: [[] as string[]],
    recursosHidricos: [[] as string[]],

    pecuariaTipo: [''],
    pecuariaCapacidade: [null as number | null],
    pecuariaDivisoes: [0],
    pecuariaEstruturas: [[] as string[]],

    acessoTipo: ['Estrada de terra'],
    distanciaAsfaltoKm: [0],
    distanciaCidadeKm: [0],
    municipioReferencia: [''],
    acessoAnoTodo: [true],
    energiaTrifasica: [false],
    internet: [false],
    sinalCelular: [false],

    carInformado: [false],
    ccirInformado: [false],
    sigefInformado: [false],
    matriculaInformada: [false],
    georreferenciado: [false],
    situacaoDeclarada: [''],

    matriculaNumero: [''],
    carReciboNumero: [''],
    carDemonstrativoCompletoInformado: [false],
    carMapaPoligonoInformado: [false],
    sigefSituacaoGeorreferenciamento: [''],
    sigefPlantaMemorialDisponivel: [false],
    sncrIncraSituacaoCadastral: [''],
    cnirCafirCib: [''],
    cnirCafirSituacaoCadastral: [''],

    fonteInformacao: ['Declarado pelo proprietário'],
    dataReferencia: [''],

    culturas: this.fb.array<ReturnType<PropertyForm['novaCultura']>>([]),
    benfeitorias: this.fb.array<ReturnType<PropertyForm['novaBenfeitoria']>>([]),
  });

  readonly categoria = signal<'URBANO' | 'RURAL'>('URBANO');

  readonly ehRural = computed(() => this.categoria() === 'RURAL');
  readonly tiposDisponiveis = computed(() =>
    this.ehRural() ? this.TIPOS_RURAIS : this.TIPOS_URBANOS,
  );
  readonly comodidades = computed(() =>
    this.ehRural() ? this.COMODIDADES_RURAIS : this.COMODIDADES_URBANAS,
  );

  get culturas(): FormArray {
    return this.form.get('culturas') as FormArray;
  }

  get benfeitorias(): FormArray {
    return this.form.get('benfeitorias') as FormArray;
  }

  // ------------------------------------------------------------------
  // Busca de endereço por CEP (ViaCEP)
  // ------------------------------------------------------------------

  readonly cepStatus = signal<'ocioso' | 'buscando' | 'ok' | 'nao-encontrado' | 'invalido' | 'indisponivel'>('ocioso');

  /** Faixa de numeração informada pelo ViaCEP — ajuda a conferir o número digitado. */
  readonly cepFaixa = signal<string | null>(null);

  readonly cepMensagem = computed(() => {
    switch (this.cepStatus()) {
      case 'buscando':
        return 'Consultando o CEP…';
      case 'ok':
        return this.ehRural()
          ? 'Município e UF preenchidos pelo CEP.'
          : 'Endereço preenchido pelo CEP. Informe o número.';
      case 'nao-encontrado':
        return 'CEP não encontrado. Preencha o endereço manualmente.';
      case 'invalido':
        return 'O CEP precisa ter 8 dígitos.';
      case 'indisponivel':
        return 'Não foi possível consultar o CEP agora. Preencha manualmente.';
      default:
        return '';
    }
  });

  /** Mantém o campo no formato 00000-000 e dispara a busca ao completar os 8 dígitos. */
  aoDigitarCep(evento: Event): void {
    const alvo = evento.target as HTMLInputElement;
    const formatado = CepService.formatar(alvo.value);
    alvo.value = formatado;
    this.form.controls.cep.setValue(formatado, { emitEvent: false });

    if (CepService.valido(formatado)) {
      this.buscarCep();
    } else if (this.cepStatus() !== 'ocioso') {
      this.cepStatus.set('ocioso');
    }
  }

  buscarCep(): void {
    const cep = this.form.controls.cep.value;

    if (!CepService.valido(cep)) {
      this.cepStatus.set('invalido');
      return;
    }

    this.cepStatus.set('buscando');
    this.cepFaixa.set(null);
    this.cepService.buscar(cep).subscribe((resultado) => {
      if (resultado.situacao !== 'encontrado') {
        this.cepStatus.set(resultado.situacao === 'invalido' ? 'invalido' : resultado.situacao);
        return;
      }

      const e = resultado.endereco;
      // O ViaCEP devolve o município em `localidade`; logradouro e bairro vêm
      // vazios em CEPs de município inteiro, comuns em zona rural.
      //
      // O campo `complemento` da resposta NÃO é o complemento do imóvel: traz a
      // faixa de numeração do CEP ("de 612 a 1510 - lado par"). Vira dica, não valor.
      this.form.patchValue({
        municipio: e.localidade || this.form.controls.municipio.value,
        uf: e.uf || this.form.controls.uf.value,
        bairro: e.bairro || this.form.controls.bairro.value,
        logradouro: e.logradouro || this.form.controls.logradouro.value,
        cep: e.cep || CepService.formatar(cep),
      });
      this.cepFaixa.set(e.complemento?.trim() || null);
      this.cepStatus.set('ok');
      this.focar(this.ehRural() ? null : 'f-numero');
    });
  }

  private focar(id: string | null): void {
    if (!id || typeof document === 'undefined') {
      return;
    }
    queueMicrotask(() => document.getElementById(id)?.focus());
  }

  /** Leva o usuário direto ao primeiro campo que está bloqueando o envio — inclusive
   * dentro de culturas/benfeitorias, onde antes não havia nenhum indicativo visual. */
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

  constructor() {
    this.brokerApi.listar({ ativo: true }).subscribe({
      next: (corretores) => {
        if (corretores.length > 0) {
          this.CORRETORES.set(corretores.map((c) => ({ slug: c.slug, nome: c.nome })));
        }
      },
      error: () => {
        // Mantém o fallback já definido em CORRETORES_PADRAO.
      },
    });

    // Ao trocar de categoria o tipo e a unidade de área acompanham o contexto —
    // só quando a troca vem do usuário, não do preenchimento inicial da edição.
    this.form.controls.categoria.valueChanges.subscribe((valor) => {
      this.categoria.set(valor);
      if (this.carregando()) {
        return;
      }
      this.form.patchValue(
        {
          tipo: valor === 'RURAL' ? 'Fazenda' : 'Apartamento',
          areaUnidade: valor === 'RURAL' ? 'ha' : 'm²',
          exposicao: valor === 'RURAL' ? 'SOMENTE_MUNICIPIO' : 'BAIRRO_MUNICIPIO',
          caracteristicas: [],
        },
        { emitEvent: false },
      );
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.imovelId.set(id);
      this.carregando.set(true);
      this.api.imovelParaEdicao(id).subscribe({
        next: (dados) => {
          this.preencherFormulario(dados);
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set('Não foi possível carregar este imóvel para edição.');
          this.carregando.set(false);
        },
      });
      this.carregarMidias(id);
    }
  }

  private carregarMidias(id: string): void {
    this.mediaApi.listar(id).subscribe({
      next: (lista) => this.midias.set(lista),
      error: () => this.erroMidia.set('Não foi possível carregar a galeria deste imóvel.'),
    });
  }

  /** Envia um ou mais arquivos selecionados no seletor de fotos/vídeos, um de cada vez. */
  async aoSelecionarArquivos(evento: Event): Promise<void> {
    const id = this.imovelId();
    const input = evento.target as HTMLInputElement;
    const arquivos = input.files ? Array.from(input.files) : [];
    input.value = '';
    if (!id || arquivos.length === 0) {
      return;
    }

    this.erroMidia.set(null);
    this.enviandoMidia.set(true);
    for (const arquivo of arquivos) {
      try {
        const atualizado = await firstValueFrom(this.mediaApi.enviar(id, arquivo));
        this.midias.set(atualizado);
      } catch (resposta: any) {
        const mensagem = resposta?.error?.erro ?? `Não foi possível enviar "${arquivo.name}".`;
        this.erroMidia.set(mensagem);
      }
    }
    this.enviandoMidia.set(false);
  }

  removerMidia(ordem: number): void {
    const id = this.imovelId();
    if (!id) {
      return;
    }
    const confirmado = typeof window === 'undefined' || window.confirm('Remover este arquivo da galeria?');
    if (!confirmado) {
      return;
    }
    this.mediaApi.remover(id, ordem).subscribe({
      next: (lista) => this.midias.set(lista),
      error: () => this.erroMidia.set('Não foi possível remover este arquivo.'),
    });
  }

  definirCapaMidia(ordem: number): void {
    const id = this.imovelId();
    if (!id) {
      return;
    }
    this.mediaApi.definirCapa(id, ordem).subscribe({
      next: (lista) => this.midias.set(lista),
      error: () => this.erroMidia.set('Não foi possível definir a capa.'),
    });
  }

  moverMidia(ordem: number, direcao: 'cima' | 'baixo'): void {
    const id = this.imovelId();
    if (!id) {
      return;
    }
    this.mediaApi.mover(id, ordem, direcao).subscribe({
      next: (lista) => this.midias.set(lista),
      error: () => this.erroMidia.set('Não foi possível reordenar a galeria.'),
    });
  }

  /** Repõe o formulário a partir dos dados brutos do imóvel — inverso de montarPayload(). */
  private preencherFormulario(v: NovoImovel): void {
    this.categoria.set(v.categoria);
    this.culturas.clear();
    this.benfeitorias.clear();

    this.form.patchValue({
      categoria: v.categoria,
      tipo: v.tipo,
      finalidade: v.finalidade,

      titulo: v.titulo,
      chamada: v.chamada ?? '',
      descricao: v.descricao ?? '',

      preco: v.valores.preco ?? null,
      condominio: v.valores.condominio ?? null,
      iptu: v.valores.iptu ?? null,
      arrendamentoPorHectare: v.valores.arrendamentoPorHectare ?? null,
      exibirPreco: v.valores.exibirPreco,
      textoAlternativo: v.valores.textoAlternativo ?? 'Consulte-nos',

      areaValor: v.areas.valor,
      areaUnidade: v.areas.unidade,
      areaConstruidaM2: v.areas.areaConstruidaM2 ?? null,
      areaTerrenoM2: v.areas.areaTerrenoM2 ?? null,

      dormitorios: v.ambientes?.dormitorios ?? 0,
      suites: v.ambientes?.suites ?? 0,
      banheiros: v.ambientes?.banheiros ?? 0,
      vagas: v.ambientes?.vagas ?? 0,

      uf: v.endereco.uf,
      municipio: v.endereco.municipio,
      bairro: v.endereco.bairro ?? '',
      logradouro: v.endereco.logradouro ?? '',
      numero: v.endereco.numero ?? '',
      complemento: v.endereco.complemento ?? '',
      cep: v.endereco.cep ?? '',
      regiaoSlug: v.endereco.regiaoSlug ?? '',
      latitude: v.endereco.latitude ?? null,
      longitude: v.endereco.longitude ?? null,
      exposicao: v.endereco.exposicao,

      caracteristicas: v.caracteristicas ?? [],

      corretorSlug: v.corretorSlug ?? 'luiz-medeiros',
      proprietario: v.proprietario ?? '',

      destaque: v.destaque,
      exclusivo: v.exclusivo,
      lancamento: v.lancamento,
      oportunidade: v.oportunidade,
      aceitaPermuta: v.aceitaPermuta,
      aceitaFinanciamento: v.aceitaFinanciamento,

      condominioNome: v.urbano?.condominioNome ?? '',
      andar: v.urbano?.andar ?? null,
      anoConstrucao: v.urbano?.anoConstrucao ?? null,
      mobiliado: v.urbano?.mobiliado ?? false,
      aceitaAnimais: v.urbano?.aceitaAnimais ?? false,
      transporteProximo: (v.urbano?.transporteProximo ?? []).join('\n'),

      areaProdutivaHa: v.rural?.areaProdutivaHa ?? 0,
      areaPastagemHa: v.rural?.areaPastagemHa ?? 0,
      areaCultivadaHa: v.rural?.areaCultivadaHa ?? 0,
      areaMataHa: v.rural?.areaMataHa ?? 0,
      reservaLegalHa: v.rural?.reservaLegalHa ?? null,
      areaIrrigadaHa: v.rural?.areaIrrigadaHa ?? null,
      terreno: v.rural?.terreno ?? [],
      soloPredominante: v.rural?.soloPredominante ?? '',
      soloFertilidade: v.rural?.soloFertilidade ?? '',
      atividades: v.rural?.atividades ?? [],
      recursosHidricos: v.rural?.recursosHidricos ?? [],

      pecuariaTipo: v.rural?.pecuaria?.tipoCriacao ?? '',
      pecuariaCapacidade: v.rural?.pecuaria?.capacidadeCabecas ?? null,
      pecuariaDivisoes: v.rural?.pecuaria?.divisoesPasto ?? 0,
      pecuariaEstruturas: v.rural?.pecuaria?.estruturas ?? [],

      acessoTipo: v.rural?.acesso?.tipoAcesso ?? 'Estrada de terra',
      distanciaAsfaltoKm: v.rural?.acesso?.distanciaAsfaltoKm ?? 0,
      distanciaCidadeKm: v.rural?.acesso?.distanciaCidadeKm ?? 0,
      municipioReferencia: v.rural?.acesso?.municipioReferencia ?? '',
      acessoAnoTodo: v.rural?.acesso?.acessoAnoTodo ?? true,
      energiaTrifasica: v.rural?.acesso?.energiaTrifasica ?? false,
      internet: v.rural?.acesso?.internet ?? false,
      sinalCelular: v.rural?.acesso?.sinalCelular ?? false,

      carInformado: v.rural?.documentacao?.carInformado ?? false,
      ccirInformado: v.rural?.documentacao?.ccirInformado ?? false,
      sigefInformado: v.rural?.documentacao?.sigefInformado ?? false,
      matriculaInformada: v.rural?.documentacao?.matriculaInformada ?? false,
      georreferenciado: v.rural?.documentacao?.georreferenciado ?? false,
      situacaoDeclarada: v.rural?.documentacao?.situacaoDeclarada ?? '',

      matriculaNumero: v.rural?.documentacao?.matriculaNumero ?? '',
      carReciboNumero: v.rural?.documentacao?.carReciboNumero ?? '',
      carDemonstrativoCompletoInformado: v.rural?.documentacao?.carDemonstrativoCompletoInformado ?? false,
      carMapaPoligonoInformado: v.rural?.documentacao?.carMapaPoligonoInformado ?? false,
      sigefSituacaoGeorreferenciamento: v.rural?.documentacao?.sigefSituacaoGeorreferenciamento ?? '',
      sigefPlantaMemorialDisponivel: v.rural?.documentacao?.sigefPlantaMemorialDisponivel ?? false,
      sncrIncraSituacaoCadastral: v.rural?.documentacao?.sncrIncraSituacaoCadastral ?? '',
      cnirCafirCib: v.rural?.documentacao?.cnirCafirCib ?? '',
      cnirCafirSituacaoCadastral: v.rural?.documentacao?.cnirCafirSituacaoCadastral ?? '',

      fonteInformacao: v.rural?.fonteInformacao ?? 'Declarado pelo proprietário',
      dataReferencia: v.rural?.dataReferencia ?? '',
    });

    for (const c of v.rural?.culturas ?? []) {
      const grupo = this.novaCultura();
      grupo.patchValue({
        nome: c.nome,
        categoria: c.categoria ?? 'Cultura anual',
        areaHa: c.areaHa,
        safra: c.safra ?? '',
        produtividade: c.produtividade ?? '',
      });
      this.culturas.push(grupo);
    }
    for (const b of v.rural?.benfeitorias ?? []) {
      const grupo = this.novaBenfeitoria();
      grupo.patchValue({
        tipo: b.tipo,
        quantidade: b.quantidade,
        areaM2: b.areaM2 ?? null,
        conservacao: b.conservacao ?? 'Bom',
      });
      this.benfeitorias.push(grupo);
    }
  }

  novaCultura() {
    return this.fb.nonNullable.group({
      nome: ['', Validators.required],
      categoria: ['Cultura anual'],
      areaHa: [0],
      safra: [''],
      produtividade: [''],
    });
  }

  novaBenfeitoria() {
    return this.fb.nonNullable.group({
      tipo: ['', Validators.required],
      quantidade: [1, Validators.min(1)],
      areaM2: [null as number | null],
      conservacao: ['Bom'],
    });
  }

  adicionarCultura(): void {
    this.culturas.push(this.novaCultura());
  }

  removerCultura(indice: number): void {
    this.culturas.removeAt(indice);
  }

  adicionarBenfeitoria(): void {
    this.benfeitorias.push(this.novaBenfeitoria());
  }

  removerBenfeitoria(indice: number): void {
    this.benfeitorias.removeAt(indice);
  }

  /** Alterna um valor em um controle de múltipla escolha. */
  alternar(campo: 'caracteristicas' | 'terreno' | 'atividades' | 'recursosHidricos' | 'pecuariaEstruturas', valor: string): void {
    const controle = this.form.get(campo)!;
    const atual = (controle.value as string[]) ?? [];
    controle.setValue(
      atual.includes(valor) ? atual.filter((v) => v !== valor) : [...atual, valor],
    );
  }

  marcado(campo: 'caracteristicas' | 'terreno' | 'atividades' | 'recursosHidricos' | 'pecuariaEstruturas', valor: string): boolean {
    return ((this.form.get(campo)!.value as string[]) ?? []).includes(valor);
  }

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  /** Mesma checagem, para um campo dentro de um item de culturas/benfeitorias. */
  invalidoItem(array: FormArray, indice: number, campo: string): boolean {
    const c = array.at(indice)?.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  salvar(situacao: 'RASCUNHO' | 'PUBLICADO'): void {
    this.erro.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erro.set('Revise os campos destacados antes de continuar.');
      this.focarPrimeiroInvalido();
      return;
    }

    this.salvando.set(true);
    const payload = this.montarPayload(situacao);
    const id = this.imovelId();
    const requisicao = id ? this.api.atualizar(id, payload) : this.api.cadastrar(payload);

    requisicao
      .pipe(
        catchError((resposta) => {
          const mensagem = resposta?.error?.erro
            ?? (resposta?.error?.campos
              ? Object.values(resposta.error.campos).join(' · ')
              : 'Não foi possível salvar o imóvel.');
          this.erro.set(mensagem);
          return of(null);
        }),
      )
      .subscribe((criado) => {
        this.salvando.set(false);
        if (criado) {
          this.criado.set(criado);
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
      });
  }

  novoCadastro(): void {
    this.criado.set(null);
    this.form.reset({
      categoria: 'URBANO',
      tipo: 'Apartamento',
      finalidade: 'VENDA',
      areaUnidade: 'm²',
      exibirPreco: true,
      textoAlternativo: 'Consulte-nos',
      uf: 'MG',
      exposicao: 'BAIRRO_MUNICIPIO',
      corretorSlug: 'luiz-medeiros',
      aceitaFinanciamento: true,
      acessoTipo: 'Estrada de terra',
      acessoAnoTodo: true,
      fonteInformacao: 'Declarado pelo proprietário',
      caracteristicas: [],
      terreno: [],
      atividades: [],
      recursosHidricos: [],
      pecuariaEstruturas: [],
    });
    this.culturas.clear();
    this.benfeitorias.clear();
  }

  irParaAnuncio(): void {
    const url = this.criado()?.url;
    if (url) {
      this.router.navigateByUrl(url);
    }
  }

  private montarPayload(situacao: string): NovoImovel {
    const v = this.form.getRawValue();
    const rural = v.categoria === 'RURAL';

    return {
      categoria: v.categoria,
      tipo: v.tipo,
      finalidade: v.finalidade,
      situacao,

      titulo: v.titulo.trim(),
      chamada: v.chamada?.trim() || undefined,
      descricao: v.descricao?.trim() || undefined,

      valores: {
        preco: v.preco,
        condominio: v.condominio,
        iptu: v.iptu,
        arrendamentoPorHectare: v.arrendamentoPorHectare,
        exibirPreco: v.exibirPreco,
        textoAlternativo: v.exibirPreco ? null : v.textoAlternativo || 'Consulte-nos',
      },

      areas: {
        valor: v.areaValor ?? 0,
        unidade: v.areaUnidade,
        areaConstruidaM2: v.areaConstruidaM2,
        areaTerrenoM2: v.areaTerrenoM2,
      },

      ambientes: {
        dormitorios: v.dormitorios ?? 0,
        suites: v.suites ?? 0,
        banheiros: v.banheiros ?? 0,
        vagas: v.vagas ?? 0,
      },

      endereco: {
        uf: v.uf.toUpperCase(),
        municipio: v.municipio.trim(),
        bairro: v.bairro || null,
        logradouro: v.logradouro || null,
        numero: v.numero || null,
        complemento: v.complemento || null,
        cep: v.cep || null,
        regiaoSlug: v.regiaoSlug || null,
        latitude: v.latitude,
        longitude: v.longitude,
        exposicao: v.exposicao,
      },

      caracteristicas: v.caracteristicas ?? [],

      corretorSlug: v.corretorSlug || null,
      corretorNome: this.CORRETORES().find((c) => c.slug === v.corretorSlug)?.nome ?? null,
      proprietario: v.proprietario || null,

      destaque: v.destaque,
      exclusivo: v.exclusivo,
      lancamento: v.lancamento,
      oportunidade: v.oportunidade,
      aceitaPermuta: v.aceitaPermuta,
      aceitaFinanciamento: v.aceitaFinanciamento,

      urbano: rural
        ? null
        : {
            condominioNome: v.condominioNome || null,
            andar: v.andar,
            anoConstrucao: v.anoConstrucao,
            mobiliado: v.mobiliado,
            aceitaAnimais: v.aceitaAnimais,
            transporteProximo: this.separarLinhas(v.transporteProximo),
          },

      rural: rural
        ? {
            areaProdutivaHa: v.areaProdutivaHa ?? 0,
            areaPastagemHa: v.areaPastagemHa ?? 0,
            areaCultivadaHa: v.areaCultivadaHa ?? 0,
            areaMataHa: v.areaMataHa ?? 0,
            reservaLegalHa: v.reservaLegalHa,
            areaIrrigadaHa: v.areaIrrigadaHa,
            terreno: v.terreno ?? [],
            soloPredominante: v.soloPredominante || null,
            soloFertilidade: v.soloFertilidade || null,
            atividades: v.atividades ?? [],
            culturas: this.culturas.getRawValue(),
            pecuaria: v.pecuariaTipo
              ? {
                  tipoCriacao: v.pecuariaTipo,
                  capacidadeCabecas: v.pecuariaCapacidade,
                  divisoesPasto: v.pecuariaDivisoes ?? 0,
                  estruturas: v.pecuariaEstruturas ?? [],
                }
              : null,
            recursosHidricos: v.recursosHidricos ?? [],
            benfeitorias: this.benfeitorias.getRawValue(),
            acesso: {
              tipoAcesso: v.acessoTipo || null,
              distanciaAsfaltoKm: v.distanciaAsfaltoKm ?? 0,
              distanciaCidadeKm: v.distanciaCidadeKm ?? 0,
              municipioReferencia: v.municipioReferencia || v.municipio,
              acessoAnoTodo: v.acessoAnoTodo,
              energiaTrifasica: v.energiaTrifasica,
              internet: v.internet,
              sinalCelular: v.sinalCelular,
            },
            documentacao: {
              carInformado: v.carInformado,
              ccirInformado: v.ccirInformado,
              sigefInformado: v.sigefInformado,
              matriculaInformada: v.matriculaInformada,
              georreferenciado: v.georreferenciado,
              situacaoDeclarada: v.situacaoDeclarada || null,

              matriculaNumero: v.matriculaNumero || null,
              carReciboNumero: v.carReciboNumero || null,
              carDemonstrativoCompletoInformado: v.carDemonstrativoCompletoInformado,
              carMapaPoligonoInformado: v.carMapaPoligonoInformado,
              sigefSituacaoGeorreferenciamento: v.sigefSituacaoGeorreferenciamento || null,
              sigefPlantaMemorialDisponivel: v.sigefPlantaMemorialDisponivel,
              sncrIncraSituacaoCadastral: v.sncrIncraSituacaoCadastral || null,
              cnirCafirCib: v.cnirCafirCib || null,
              cnirCafirSituacaoCadastral: v.cnirCafirSituacaoCadastral || null,
            },
            fonteInformacao: v.fonteInformacao || null,
            dataReferencia: v.dataReferencia || null,
          }
        : null,
    };
  }

  /** Campos de texto multilinha viram listas (uma URL ou item por linha). */
  private separarLinhas(valor: string | null | undefined): string[] {
    if (!valor) {
      return [];
    }
    return valor
      .split('\n')
      .map((linha) => linha.trim())
      .filter((linha) => linha.length > 0);
  }
}
