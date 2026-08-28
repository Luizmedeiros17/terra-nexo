import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';

export interface SeoTags {
  title: string;
  description: string;
  /** Caminho canônico, iniciando com "/". */
  canonicalPath: string;
  image?: string;
  imageAlt?: string;
  type?: 'website' | 'article';
  /** Combinações arbitrárias de filtros recebem noindex (seção 20). */
  index?: boolean;
}

/**
 * Metadados por rota: title, description, canonical, robots, Open Graph,
 * Twitter Cards e JSON-LD. Executa no SSR, de modo que o HTML entregue ao
 * rastreador já contém as tags — sem depender de JavaScript no cliente.
 */
@Injectable({ providedIn: 'root' })
export class SeoService {
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);
  private readonly doc = inject(DOCUMENT);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  /** Origem pública do portal; em produção vem de variável de ambiente do SSR. */
  readonly siteUrl = 'https://www.terranexo.com.br';

  aplicar(tags: SeoTags): void {
    const url = `${this.siteUrl}${tags.canonicalPath}`;
    const imagem = tags.image ?? `${this.siteUrl}/assets/og-default.jpg`;

    this.title.setTitle(tags.title);
    this.setName('description', tags.description);
    this.setName('robots', tags.index === false
      ? 'noindex, follow'
      : 'index, follow, max-image-preview:large, max-snippet:-1');

    this.setProperty('og:type', tags.type ?? 'website');
    this.setProperty('og:site_name', 'Terra Nexo');
    this.setProperty('og:locale', 'pt_BR');
    this.setProperty('og:title', tags.title);
    this.setProperty('og:description', tags.description);
    this.setProperty('og:url', url);
    this.setProperty('og:image', imagem);
    if (tags.imageAlt) {
      this.setProperty('og:image:alt', tags.imageAlt);
    }

    this.setName('twitter:card', 'summary_large_image');
    this.setName('twitter:title', tags.title);
    this.setName('twitter:description', tags.description);
    this.setName('twitter:image', imagem);

    this.setCanonical(url);
  }

  /** Injeta um bloco JSON-LD identificado, substituindo o anterior de mesma chave. */
  jsonLd(chave: string, dados: unknown): void {
    const id = `ld-${chave}`;
    const head = this.doc.head;
    const existente = this.doc.getElementById(id);
    if (existente) {
      existente.remove();
    }
    const script = this.doc.createElement('script');
    script.id = id;
    script.type = 'application/ld+json';
    script.textContent = JSON.stringify(dados);
    head.appendChild(script);
  }

  /** Links rel=prev/next da paginação, para o rastreador seguir a sequência (seção 13). */
  paginacao(anterior: string | null, proxima: string | null): void {
    this.setLink('prev', anterior ? `${this.siteUrl}${anterior}` : null);
    this.setLink('next', proxima ? `${this.siteUrl}${proxima}` : null);
  }

  private setName(name: string, content: string): void {
    this.meta.updateTag({ name, content });
  }

  private setProperty(property: string, content: string): void {
    this.meta.updateTag({ property, content });
  }

  private setCanonical(url: string): void {
    this.setLink('canonical', url);
  }

  private setLink(rel: string, href: string | null): void {
    const head = this.doc.head;
    let link = head.querySelector<HTMLLinkElement>(`link[rel="${rel}"]`);
    if (!href) {
      link?.remove();
      return;
    }
    if (!link) {
      link = this.doc.createElement('link');
      link.setAttribute('rel', rel);
      head.appendChild(link);
    }
    link.setAttribute('href', href);
  }

  /** Dados estruturados fixos do portal (seção 20). */
  organizacao(): void {
    this.jsonLd('organizacao', {
      '@context': 'https://schema.org',
      '@graph': [
        {
          '@type': 'RealEstateAgent',
          '@id': `${this.siteUrl}/#organizacao`,
          name: 'Terra Nexo',
          description:
            'Imobiliária especializada em imóveis urbanos e rurais no Noroeste de Minas e no Centro-Oeste goiano.',
          url: `${this.siteUrl}/`,
          logo: `${this.siteUrl}/brand/lockup.png`,
          telephone: '+55-61-99146-7471',
          email: 'contato@terranexo.com.br',
          priceRange: '$$$',
          address: {
            '@type': 'PostalAddress',
            streetAddress: 'Avenida Brasil, Centro',
            addressLocality: 'Unaí',
            addressRegion: 'MG',
            postalCode: '38610-000',
            addressCountry: 'BR',
          },
          areaServed: [
            { '@type': 'City', name: 'Unaí' },
            { '@type': 'City', name: 'Paracatu' },
            { '@type': 'City', name: 'João Pinheiro' },
            { '@type': 'City', name: 'Formosa' },
            { '@type': 'City', name: 'Luziânia' },
            { '@type': 'City', name: 'Cristalina' },
            { '@type': 'City', name: 'Catalão' },
          ],
        },
        {
          '@type': 'WebSite',
          '@id': `${this.siteUrl}/#site`,
          url: `${this.siteUrl}/`,
          name: 'Terra Nexo',
          inLanguage: 'pt-BR',
          publisher: { '@id': `${this.siteUrl}/#organizacao` },
          potentialAction: {
            '@type': 'SearchAction',
            target: {
              '@type': 'EntryPoint',
              urlTemplate: `${this.siteUrl}/imoveis?q={search_term_string}`,
            },
            'query-input': 'required name=search_term_string',
          },
        },
      ],
    });
  }

  get executandoNoNavegador(): boolean {
    return this.isBrowser;
  }
}
