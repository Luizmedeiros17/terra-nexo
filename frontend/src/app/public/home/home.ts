import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';

import { PropertyApiService } from '../../core/api/property-api.service';
import { PropertyCardComponent } from '../../shared/components/property-card/property-card';
import { SeoService } from '../../core/seo/seo.service';
import {
  Categoria,
  Corretor,
  PropertyCard,
  Regiao,
  SearchFilters,
} from '../../shared/models/property.model';
import { paraQueryParams } from '../property-search/search-params';

/**
 * Página inicial (seção 6): banner e proposta de valor, busca simplificada,
 * destaques gerais, destaques urbanos e rurais, imóveis recentes, apresentação
 * da imobiliária, regiões atendidas, serviços, chamada para proprietários,
 * corretores, depoimentos e contato.
 */
@Component({
  selector: 'tn-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ReactiveFormsModule, PropertyCardComponent],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  private readonly api = inject(PropertyApiService);
  private readonly router = inject(Router);
  private readonly seo = inject(SeoService);
  private readonly fb = inject(FormBuilder);

  // ---- Dados do portal ----
  readonly destaques = toSignal(this.api.destaques(undefined, 6).pipe(catchError(() => of([]))), {
    initialValue: [] as PropertyCard[],
  });
  readonly destaquesUrbanos = toSignal(this.api.destaques('URBANO', 3).pipe(catchError(() => of([]))), {
    initialValue: [] as PropertyCard[],
  });
  readonly destaquesRurais = toSignal(this.api.destaques('RURAL', 3).pipe(catchError(() => of([]))), {
    initialValue: [] as PropertyCard[],
  });
  readonly recentes = toSignal(this.api.recentes(3).pipe(catchError(() => of([]))), {
    initialValue: [] as PropertyCard[],
  });
  readonly regioes = toSignal(this.api.regioes().pipe(catchError(() => of([] as Regiao[]))), {
    initialValue: [] as Regiao[],
  });
  readonly corretores = toSignal(this.api.corretores().pipe(catchError(() => of([] as Corretor[]))), {
    initialValue: [] as Corretor[],
  });

  readonly regioesUrbanas = computed(() => this.regioes().filter((r) => r.categoria === 'URBANO'));
  readonly regioesRurais = computed(() => this.regioes().filter((r) => r.categoria === 'RURAL'));

  // ---- Busca simplificada ----
  readonly abaBusca = signal<Categoria>('URBANO');
  readonly buscaForm = this.fb.nonNullable.group({
    q: [''],
    finalidade: ['VENDA'],
    tipo: [''],
    precoMax: [''],
  });

  readonly tiposUrbanos = ['Apartamento', 'Casa', 'Casa em condomínio', 'Cobertura', 'Loft', 'Kitnet', 'Sala comercial', 'Terreno'];
  readonly tiposRurais = ['Fazenda', 'Sítio', 'Chácara', 'Haras', 'Propriedade agrícola', 'Propriedade pecuária', 'Área de reflorestamento'];

  readonly tiposDaAba = computed(() =>
    this.abaBusca() === 'RURAL' ? this.tiposRurais : this.tiposUrbanos,
  );

  readonly faixasUrbanas = [
    { valor: '1000000', rotulo: 'Até R$ 1 milhão' },
    { valor: '2500000', rotulo: 'Até R$ 2,5 milhões' },
    { valor: '5000000', rotulo: 'Até R$ 5 milhões' },
  ];

  readonly faixasRurais = [
    { valor: '2000000', rotulo: 'Até R$ 2 milhões' },
    { valor: '10000000', rotulo: 'Até R$ 10 milhões' },
    { valor: '50000000', rotulo: 'Até R$ 50 milhões' },
  ];

  readonly faixasDaAba = computed(() => (this.abaBusca() === 'RURAL' ? this.faixasRurais : this.faixasUrbanas));

  // ---- Conteúdo institucional (viria do CMS, seção 19) ----
  readonly servicos = [
    {
      icone: 'busca',
      titulo: 'Compra e venda',
      texto: 'Curadoria de imóveis urbanos e rurais, avaliação com base em transações reais e negociação conduzida por um consultor dedicado.',
    },
    {
      icone: 'chave',
      titulo: 'Locação',
      texto: 'Análise cadastral, contrato, vistoria e acompanhamento do inquilino durante toda a vigência.',
    },
    {
      icone: 'campo',
      titulo: 'Imóveis rurais',
      texto: 'Avaliação de terras, análise de matrícula, CAR, CCIR e georreferenciamento, com apoio na due diligence.',
    },
    {
      icone: 'escudo',
      titulo: 'Assessoria jurídica',
      texto: 'Conferência documental antes da proposta: matrícula, certidões, condomínio e pendências declaradas.',
    },
    {
      icone: 'grafico',
      titulo: 'Avaliação e captação',
      texto: 'Laudo em até 48 horas, produção fotográfica profissional e plano de divulgação com relatório quinzenal.',
    },
    {
      icone: 'banco',
      titulo: 'Crédito imobiliário',
      texto: 'Comparação de propostas de seis bancos, organização da documentação e acompanhamento até a assinatura.',
    },
  ];

  readonly depoimentos = [
    {
      texto: 'Visitamos seis casas em três fins de semana e o consultor já sabia, antes de nós, qual seria a escolhida. Fechamos abaixo do valor anunciado.',
      nome: 'Renata Bittencourt',
      contexto: 'Comprou no Centro de Unaí',
      foto: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&q=70',
    },
    {
      texto: 'Comprei uma fazenda em Paracatu com a documentação toda conferida antes da proposta. O relatório de matrícula e CAR economizou meses de dor de cabeça.',
      nome: 'Jorge Sampaio',
      contexto: 'Comprou área rural em Paracatu',
      foto: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=120&q=70',
    },
    {
      texto: 'Minha fazenda estava há oito meses parada em outra imobiliária. A Terra Nexo refez as fotos, ajustou o preço e vendeu em 41 dias.',
      nome: 'Paulo Menezes',
      contexto: 'Vendeu em Formosa',
      foto: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&q=70',
    },
  ];

  // ---- Formulário de contato (lead, seção 17) ----
  readonly contatoForm = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    telefone: ['', [Validators.required, Validators.minLength(10)]],
    assunto: ['Quero comprar um imóvel'],
    mensagem: ['', [Validators.maxLength(2000)]],
    consentimento: [false, [Validators.requiredTrue]],
  });

  readonly enviando = signal(false);
  readonly protocolo = signal<string | null>(null);
  readonly erroEnvio = signal<string | null>(null);

  constructor() {
    this.seo.aplicar({
      title: 'Terra Nexo | Imóveis urbanos e rurais à venda e para alugar',
      description:
        'Imobiliária especializada em imóveis urbanos e rurais em Unaí, Paracatu e João Pinheiro (MG) e Formosa, Luziânia, Cristalina e Catalão (GO): apartamentos e casas na cidade, fazendas e áreas produtivas no campo. Curadoria, assessoria jurídica e crédito imobiliário.',
      canonicalPath: '/',
      image: 'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=1200&q=75',
      imageAlt: 'Sala de estar de cobertura de alto padrão com vista para a cidade',
    });
  }

  selecionarAba(categoria: Categoria): void {
    this.abaBusca.set(categoria);
    this.buscaForm.patchValue({ tipo: '', precoMax: '' });
  }

  buscar(): void {
    const v = this.buscaForm.getRawValue();
    const filtros: SearchFilters = {
      categoria: this.abaBusca(),
      finalidade: v.finalidade as SearchFilters['finalidade'],
      q: v.q || undefined,
      tipo: v.tipo ? [v.tipo] : undefined,
      precoMax: v.precoMax ? Number(v.precoMax) : undefined,
    };
    this.router.navigate(['/imoveis'], { queryParams: paraQueryParams(filtros) });
  }

  enviarContato(): void {
    if (this.contatoForm.invalid) {
      this.contatoForm.markAllAsTouched();
      return;
    }
    const v = this.contatoForm.getRawValue();
    this.enviando.set(true);
    this.erroEnvio.set(null);

    this.api
      .enviarLead({
        nome: v.nome,
        email: v.email,
        telefone: v.telefone,
        mensagem: `[${v.assunto}] ${v.mensagem}`.trim(),
        paginaOrigem: '/',
        consentimento: v.consentimento,
      })
      .pipe(
        catchError(() => {
          this.erroEnvio.set('Não foi possível enviar agora. Tente novamente ou fale pelo WhatsApp.');
          return of(null);
        }),
      )
      .subscribe((resposta) => {
        this.enviando.set(false);
        if (resposta) {
          this.protocolo.set(resposta.protocolo);
          this.contatoForm.reset({ assunto: 'Quero comprar um imóvel', consentimento: false });
        }
      });
  }

  invalido(campo: string): boolean {
    const c = this.contatoForm.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }
}
