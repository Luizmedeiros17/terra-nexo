import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';

import { PropertyApiService } from '../../core/api/property-api.service';
import { PropertyCardComponent } from '../../shared/components/property-card/property-card';
import { PropertyCard, PropertyDetail } from '../../shared/models/property.model';
import { SeoService } from '../../core/seo/seo.service';

/**
 * Página do imóvel (seção 14). Nesta fase entrega o essencial para dar
 * continuidade à navegação a partir do resultado de busca: galeria, ficha,
 * bloco rural declaratório, corretor responsável e imóveis semelhantes.
 */
@Component({
  selector: 'tn-property-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DecimalPipe, PropertyCardComponent],
  templateUrl: './property-detail.html',
  styleUrl: './property-detail.css',
})
export class PropertyDetailPage {
  private readonly api = inject(PropertyApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly seo = inject(SeoService);

  readonly carregando = signal(true);
  readonly naoEncontrado = signal(false);

  readonly imovel = toSignal(
    this.route.paramMap.pipe(
      tap(() => {
        this.carregando.set(true);
        this.naoEncontrado.set(false);
      }),
      switchMap((pm) =>
        this.api.porCodigoOuSlug(pm.get('slug') ?? '').pipe(
          catchError(() => {
            this.naoEncontrado.set(true);
            return of(null);
          }),
        ),
      ),
      tap(() => this.carregando.set(false)),
    ),
    { initialValue: null as PropertyDetail | null },
  );

  readonly semelhantes = toSignal(
    this.route.paramMap.pipe(
      switchMap((pm) =>
        this.api.porCodigoOuSlug(pm.get('slug') ?? '').pipe(
          switchMap((d) => this.api.semelhantes(d.resumo.id, 3)),
          catchError(() => of([] as PropertyCard[])),
        ),
      ),
    ),
    { initialValue: [] as PropertyCard[] },
  );

  readonly fichaTecnica = computed(() => Object.entries(this.imovel()?.fichaTecnica ?? {}));

  constructor() {
    effect(() => {
      const d = this.imovel();
      if (!d) {
        return;
      }
      const im = d.resumo;
      this.seo.aplicar({
        title: `${im.titulo} — ${im.localizacao} | Terra Nexo`,
        description: `${im.tipo} para ${im.finalidadeRotulo.toLowerCase()} em ${im.localizacao}: ${im.areaTexto}. ${im.chamada}`.slice(0, 158),
        canonicalPath: im.url,
        image: im.imagem.url,
        imageAlt: im.imagem.alt,
        type: 'article',
      });

      this.seo.jsonLd('imovel', {
        '@context': 'https://schema.org',
        '@type': ['Residence', 'Product'],
        name: im.titulo,
        description: d.descricao,
        sku: im.codigo,
        image: d.galeria.map((g) => g.url),
        url: `${this.seo.siteUrl}${im.url}`,
        numberOfRooms: im.ambientes.dormitorios || undefined,
        floorSize: { '@type': 'QuantitativeValue', value: im.areaM2, unitCode: 'MTK' },
        address: {
          '@type': 'PostalAddress',
          addressLocality: im.municipio,
          addressRegion: im.uf,
          addressCountry: 'BR',
        },
        offers: im.exibirPreco
          ? {
              '@type': 'Offer',
              price: im.preco,
              priceCurrency: 'BRL',
              availability: 'https://schema.org/InStock',
              businessFunction:
                im.finalidade === 'LOCACAO'
                  ? 'https://schema.org/LeaseOut'
                  : 'https://schema.org/Sell',
            }
          : undefined,
      });

      this.seo.jsonLd('breadcrumb-imovel', {
        '@context': 'https://schema.org',
        '@type': 'BreadcrumbList',
        itemListElement: [
          { '@type': 'ListItem', position: 1, name: 'Início', item: `${this.seo.siteUrl}/` },
          { '@type': 'ListItem', position: 2, name: 'Imóveis', item: `${this.seo.siteUrl}/imoveis` },
          { '@type': 'ListItem', position: 3, name: im.titulo },
        ],
      });
    });
  }
}
