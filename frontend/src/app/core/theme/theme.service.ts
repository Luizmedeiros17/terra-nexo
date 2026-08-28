import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';

type Tema = 'light' | 'dark';

const CHAVE = 'terranexo:theme';

/**
 * Alternância de tema. No SSR o tema padrão é claro; no navegador a preferência
 * salva (ou a do sistema) é aplicada em `<html data-theme>` antes da pintura,
 * pelo script inline do index.html.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly doc = inject(DOCUMENT);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  readonly tema = signal<Tema>('light');

  constructor() {
    if (!this.isBrowser) {
      return;
    }
    const salvo = localStorage.getItem(CHAVE) as Tema | null;
    const sistemaEscuro = window.matchMedia('(prefers-color-scheme: dark)').matches;
    this.aplicar(salvo ?? (sistemaEscuro ? 'dark' : 'light'));
  }

  alternar(): void {
    this.aplicar(this.tema() === 'dark' ? 'light' : 'dark');
  }

  private aplicar(tema: Tema): void {
    this.tema.set(tema);
    if (!this.isBrowser) {
      return;
    }
    this.doc.documentElement.dataset['theme'] = tema;
    localStorage.setItem(CHAVE, tema);
    this.doc
      .querySelector('meta[name="theme-color"]')
      ?.setAttribute('content', tema === 'dark' ? '#0e1114' : '#0f3b3f');
  }
}
