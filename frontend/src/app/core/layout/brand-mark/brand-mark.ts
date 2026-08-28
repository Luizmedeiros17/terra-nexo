import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Emblema da marca Terra Nexo: casa rural sobre lavoura em sulcos, dentro do
 * círculo com seta ascendente. Recortado de docs/Logotipo Terra-Nexo.jpg
 * (ver frontend/public/brand/ — circle.png para uso inline, lockup.png para
 * o logotipo completo com assinatura e slogan).
 */
@Component({
  selector: 'tn-brand-mark',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <img
      class="mark"
      src="/brand/circle.png"
      alt=""
      aria-hidden="true"
      [style.width.px]="tamanho()"
      [style.height.px]="tamanho()"
    />
  `,
  styles: [
    `
      :host {
        display: inline-flex;
        flex: none;
      }

      .mark {
        display: block;
        object-fit: contain;
        transition: transform .5s cubic-bezier(.22, .61, .36, 1);
      }
    `,
  ],
})
export class BrandMark {
  readonly tamanho = input(42);
}
