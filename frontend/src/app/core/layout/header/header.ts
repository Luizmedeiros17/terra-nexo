import { ChangeDetectionStrategy, Component, HostListener, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { BrandMark } from '../brand-mark/brand-mark';
import { ThemeService } from '../../theme/theme.service';

@Component({
  selector: 'tn-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, BrandMark],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  private readonly theme = inject(ThemeService);

  readonly menuAberto = signal(false);
  readonly fixado = signal(false);
  readonly temaAtual = this.theme.tema;

  alternarMenu(): void {
    this.menuAberto.update((v) => !v);
  }

  fecharMenu(): void {
    this.menuAberto.set(false);
  }

  alternarTema(): void {
    this.theme.alternar();
  }

  @HostListener('window:scroll')
  aoRolar(): void {
    this.fixado.set(window.scrollY > 12);
  }

  @HostListener('document:keydown.escape')
  aoPressionarEsc(): void {
    this.fecharMenu();
  }
}
