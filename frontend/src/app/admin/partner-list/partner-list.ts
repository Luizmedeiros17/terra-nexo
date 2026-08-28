import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';

import { PartnerApiService } from '../../core/api/partner-api.service';
import { PartnerCard, PartnerResumo } from '../../shared/models/partner.model';

/** Base de prospecção de clientes e parceiros — listagem com filtros por cidade, setor e status. */
@Component({
  selector: 'tn-admin-partner-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './partner-list.html',
  styleUrl: './partner-list.css',
})
export class PartnerList {
  private readonly api = inject(PartnerApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly carregando = signal(true);

  readonly CIDADES = [
    { valor: '', rotulo: 'Todas as cidades' },
    { valor: 'Unaí', rotulo: 'Unaí' },
    { valor: 'Paracatu', rotulo: 'Paracatu' },
    { valor: 'Formosa', rotulo: 'Formosa' },
    { valor: 'Luziânia', rotulo: 'Luziânia' },
    { valor: 'Cristalina', rotulo: 'Cristalina' },
    { valor: 'Catalão', rotulo: 'Catalão' },
    { valor: 'João Pinheiro', rotulo: 'João Pinheiro' },
  ];

  readonly SETORES = [
    { valor: '', rotulo: 'Todos os setores' },
    { valor: 'AGROPECUARIA', rotulo: 'Agropecuária' },
    { valor: 'IMOBILIARIA', rotulo: 'Imobiliária' },
    { valor: 'IRRIGACAO', rotulo: 'Irrigação' },
    { valor: 'SILOS_ARMAZENS', rotulo: 'Silos/Armazéns' },
    { valor: 'LEILAO_ANIMAIS', rotulo: 'Leilão de Animais' },
    { valor: 'LATICINIOS', rotulo: 'Laticínios' },
  ];

  readonly STATUS = [
    { valor: '', rotulo: 'Todos os status' },
    { valor: 'NAO_INICIADO', rotulo: 'Não iniciado' },
    { valor: 'PESQUISADO', rotulo: 'Pesquisado' },
    { valor: 'CONTATADO', rotulo: 'Contatado' },
    { valor: 'RESPONDEU', rotulo: 'Respondeu' },
    { valor: 'REUNIAO_MARCADA', rotulo: 'Reunião marcada' },
    { valor: 'PROPOSTA_ENVIADA', rotulo: 'Proposta enviada' },
    { valor: 'CONVERTIDO', rotulo: 'Convertido' },
    { valor: 'SEM_INTERESSE', rotulo: 'Sem interesse' },
    { valor: 'CONTATO_INVALIDO', rotulo: 'Contato inválido' },
  ];

  readonly filtros = toSignal(this.route.queryParamMap, { requireSync: true });

  readonly parceiros = toSignal(
    this.route.queryParamMap.pipe(
      tap(() => this.carregando.set(true)),
      switchMap((pm) =>
        this.api
          .listar({
            cidade: pm.get('cidade') ?? undefined,
            setor: pm.get('setor') ?? undefined,
            status: pm.get('status') ?? undefined,
            q: pm.get('q') ?? undefined,
          })
          .pipe(catchError(() => of([] as PartnerCard[]))),
      ),
      tap(() => this.carregando.set(false)),
    ),
    { initialValue: [] as PartnerCard[] },
  );

  readonly resumo = toSignal(
    this.api.resumo().pipe(catchError(() => of(null as PartnerResumo | null))),
    { initialValue: null as PartnerResumo | null },
  );

  valorDe(chave: string): string {
    return this.filtros().get(chave) ?? '';
  }

  aplicar(chave: string, valor: string): void {
    const params: Record<string, string> = {};
    for (const k of this.filtros().keys) {
      const v = this.filtros().get(k);
      if (v) {
        params[k] = v;
      }
    }
    if (valor) {
      params[chave] = valor;
    } else {
      delete params[chave];
    }
    this.router.navigate(['/admin/parceiros'], { queryParams: params });
  }

  aoDigitar(evento: Event): void {
    this.aplicar('q', (evento.target as HTMLInputElement).value.trim());
  }

  aoSelecionar(chave: string, evento: Event): void {
    this.aplicar(chave, (evento.target as HTMLSelectElement).value);
  }

  limpar(): void {
    this.router.navigate(['/admin/parceiros']);
  }

  contatoPrincipal(p: PartnerCard): string {
    return p.whatsapp ?? p.telefoneCelular ?? p.telefoneFixo ?? p.email ?? '—';
  }
}
