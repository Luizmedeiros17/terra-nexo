import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { SeoService } from '../../core/seo/seo.service';

/** Página 404 — não indexável, com caminhos de volta (seção 20). */
@Component({
  selector: 'tn-not-found',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  template: `
    <section class="section">
      <div class="wrap conteudo">
        <p class="eyebrow">Erro 404</p>
        <h1 class="display">Esta página não existe</h1>
        <p>
          O endereço pode ter mudado ou o anúncio saiu do ar. Comece pela busca —
          a carteira tem imóveis urbanos e rurais publicados agora.
        </p>
        <div class="acoes">
          <a class="btn btn--primary btn--lg" routerLink="/imoveis">Ver imóveis</a>
          <a class="btn btn--ghost btn--lg" routerLink="/">Voltar ao início</a>
        </div>
      </div>
    </section>
  `,
  styles: [
    `
      .conteudo {
        max-width: 620px;
        text-align: center;
        padding-block: clamp(2rem, 5vw, 5rem);
      }

      h1 {
        font-size: var(--t-3xl);
        margin: .8rem 0 1rem;
      }

      p {
        color: var(--c-muted);
      }

      .acoes {
        display: flex;
        gap: .8rem;
        justify-content: center;
        flex-wrap: wrap;
        margin-top: 2rem;
      }
    `,
  ],
})
export class NotFound {
  constructor() {
    inject(SeoService).aplicar({
      title: 'Página não encontrada | Terra Nexo',
      description: 'A página procurada não existe ou saiu do ar.',
      canonicalPath: '/404',
      index: false,
    });
  }
}
