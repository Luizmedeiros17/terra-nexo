import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';

import { Footer } from './core/layout/footer/footer';
import { Header } from './core/layout/header/header';
import { SeoService } from './core/seo/seo.service';

@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, Header, Footer],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly seo = inject(SeoService);
  private readonly router = inject(Router);

  /** O ambiente administrativo tem casca própria — sem header, rodapé ou WhatsApp. */
  private readonly urlAtual = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => e.urlAfterRedirects),
      startWith(this.router.url),
    ),
    { initialValue: this.router.url },
  );

  readonly ehAdmin = computed(() => this.urlAtual().startsWith('/admin'));

  constructor() {
    // Dados estruturados do portal, presentes nas rotas públicas (seção 20).
    this.seo.organizacao();
  }
}
