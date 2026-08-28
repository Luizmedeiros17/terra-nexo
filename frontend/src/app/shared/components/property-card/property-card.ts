import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { PropertyCard } from '../../models/property.model';

/**
 * Cartão de resultado (seção 13): imagem, código, título, localização,
 * finalidade, preço ou "Consulte-nos", área, ambientes aplicáveis,
 * resumo rural e selos.
 */
@Component({
  selector: 'tn-property-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './property-card.html',
  styleUrl: './property-card.css',
})
export class PropertyCardComponent {
  readonly imovel = input.required<PropertyCard>();
  /** A primeira dobra carrega ansiosamente; o restante fica com lazy loading (seção 20). */
  readonly prioritaria = input(false);

  readonly ehRural = computed(() => this.imovel().categoria === 'RURAL');

  /** Ambientes só aparecem quando fazem sentido para o tipo (terreno, laje, área). */
  readonly mostrarAmbientes = computed(() => {
    const a = this.imovel().ambientes;
    return a.dormitorios > 0 || a.banheiros > 0 || a.vagas > 0;
  });
}
