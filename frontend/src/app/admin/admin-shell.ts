import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { BrandMark } from '../core/layout/brand-mark/brand-mark';
import { SeoService } from '../core/seo/seo.service';
import { ThemeService } from '../core/theme/theme.service';

/**
 * Casca do ambiente administrativo (seção 18): navegação lateral própria,
 * separada do portal público. Telas autenticadas não são indexáveis.
 */
@Component({
  selector: 'tn-admin-shell',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, BrandMark],
  templateUrl: './admin-shell.html',
  styleUrl: './admin-shell.css',
})
export class AdminShell {
  private readonly theme = inject(ThemeService);

  readonly menuAberto = signal(false);
  readonly temaAtual = this.theme.tema;

  /** Usuário fictício enquanto o módulo de autenticação não existe. */
  readonly usuario = { nome: 'Luiz Medeiros', perfil: 'Corretor Remax Prime' };

  constructor() {
    inject(SeoService).aplicar({
      title: 'Administração | Terra Nexo',
      description: 'Ambiente administrativo da plataforma Terra Nexo.',
      canonicalPath: '/admin',
      index: false,
    });
  }

  alternarMenu(): void {
    this.menuAberto.update((v) => !v);
  }

  alternarTema(): void {
    this.theme.alternar();
  }
}
