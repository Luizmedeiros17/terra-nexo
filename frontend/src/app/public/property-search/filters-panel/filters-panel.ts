import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

import { Faceta, SearchFilters } from '../../../shared/models/property.model';

/**
 * Barra de filtros da pesquisa (seção 13).
 *
 * As facetas de categoria, finalidade, tipo e município vêm do backend com a
 * contagem de resultados. Os vocabulários rurais (terreno, atividade, cultura,
 * água, benfeitorias, documentação) refletem o cadastro configurável da seção 10;
 * no MVP completo eles também virão da API.
 */
@Component({
  selector: 'tn-filters-panel',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './filters-panel.html',
  styleUrl: './filters-panel.css',
})
export class FiltersPanel {
  readonly filtros = input.required<SearchFilters>();
  readonly facetas = input.required<Faceta[]>();
  readonly mostrarRurais = input(false);

  readonly mudou = output<Partial<SearchFilters>>();
  readonly alternou = output<{ chave: keyof SearchFilters; valor: string }>();
  readonly limpou = output<void>();

  readonly TERRENOS = ['Plano', 'Semiplano', 'Ondulado', 'Montanhoso', 'Várzea', 'Mata'];
  readonly ATIVIDADES = ['Agricultura', 'Pecuária', 'Silvicultura', 'Horticultura', 'Lazer'];
  readonly CULTURAS = ['Soja', 'Milho', 'Café arábica', 'Cana-de-açúcar', 'Eucalipto', 'Braquiária', 'Hortaliças'];
  readonly AGUAS = ['Rio', 'Córrego', 'Nascente', 'Represa', 'Lago', 'Poço artesiano', 'Irrigação'];
  readonly BENFEITORIAS = ['Casa sede', 'Curral', 'Galpão', 'Silo', 'Armazém', 'Confinamento', 'Cocheiras', 'Balança', 'Pista de pouso'];
  readonly DOCUMENTOS = ['CAR', 'CCIR', 'SIGEF', 'Matrícula', 'Georreferenciado'];
  readonly COMODIDADES = ['Piscina', 'Academia', 'Portaria 24h', 'Aceita pet', 'Mobiliado', 'Churrasqueira', 'Energia solar'];

  readonly QUANTIDADES = [1, 2, 3, 4];

  faceta(chave: string): Faceta | undefined {
    return this.facetas().find((f) => f.chave === chave);
  }

  readonly categorias = computed(() => this.faceta('categoria')?.valores ?? []);
  readonly finalidades = computed(() => this.faceta('finalidade')?.valores ?? []);
  readonly tipos = computed(() => this.faceta('tipo')?.valores ?? []);
  readonly municipios = computed(() => this.faceta('municipio')?.valores ?? []);

  temValor(chave: keyof SearchFilters, valor: string): boolean {
    const atual = this.filtros()[chave];
    return Array.isArray(atual) ? atual.includes(valor) : atual === valor;
  }

  numero(chave: keyof SearchFilters): number | null {
    const valor = this.filtros()[chave];
    return typeof valor === 'number' ? valor : null;
  }

  ativo(chave: keyof SearchFilters): boolean {
    return this.filtros()[chave] === true;
  }

  definir(chave: keyof SearchFilters, valor: unknown): void {
    this.mudou.emit({ [chave]: valor } as Partial<SearchFilters>);
  }

  /** Campos numéricos: string vazia limpa o filtro. */
  definirNumero(chave: keyof SearchFilters, evento: Event): void {
    const alvo = evento.target as HTMLInputElement;
    const valor = alvo.value.trim();
    this.definir(chave, valor === '' ? null : Number(valor));
  }

  definirTexto(chave: keyof SearchFilters, evento: Event): void {
    const alvo = evento.target as HTMLInputElement;
    this.definir(chave, alvo.value.trim() || null);
  }

  definirBooleano(chave: keyof SearchFilters, evento: Event): void {
    const alvo = evento.target as HTMLInputElement;
    this.definir(chave, alvo.checked ? true : null);
  }

  alternarLista(chave: keyof SearchFilters, valor: string): void {
    this.alternou.emit({ chave, valor });
  }

  /** Categoria e finalidade são exclusivas: clicar no valor ativo limpa o filtro. */
  selecionarUnico(chave: keyof SearchFilters, valor: string): void {
    this.definir(chave, this.filtros()[chave] === valor ? null : valor);
  }
}
