import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';

import { LeadApiService } from '../../core/api/lead-api.service';
import { LeadCard } from '../../shared/models/lead.model';

/** Funil de leads — listagem com filtros por etapa, origem e busca livre. */
@Component({
  selector: 'tn-admin-lead-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './lead-list.html',
  styleUrl: './lead-list.css',
})
export class LeadList {
  private readonly api = inject(LeadApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly carregando = signal(true);

  readonly ETAPAS = [
    { valor: '', rotulo: 'Todas as etapas' },
    { valor: 'NOVO', rotulo: 'Novo' },
    { valor: 'AGUARDANDO_ATENDIMENTO', rotulo: 'Aguardando atendimento' },
    { valor: 'CONTATO_INICIADO', rotulo: 'Contato iniciado' },
    { valor: 'SEM_RESPOSTA', rotulo: 'Sem resposta' },
    { valor: 'QUALIFICADO', rotulo: 'Qualificado' },
    { valor: 'VISITA_AGENDADA', rotulo: 'Visita agendada' },
    { valor: 'VISITA_REALIZADA', rotulo: 'Visita realizada' },
    { valor: 'PROPOSTA', rotulo: 'Proposta' },
    { valor: 'NEGOCIACAO', rotulo: 'Negociação' },
    { valor: 'CONVERTIDO', rotulo: 'Convertido' },
    { valor: 'PERDIDO', rotulo: 'Perdido' },
    { valor: 'DUPLICADO', rotulo: 'Duplicado' },
    { valor: 'DESCARTADO', rotulo: 'Descartado' },
  ];

  readonly ORIGENS = [
    { valor: '', rotulo: 'Todas as origens' },
    { valor: 'FORMULARIO_IMOVEL', rotulo: 'Formulário do imóvel' },
    { valor: 'CONTATO_GERAL', rotulo: 'Contato geral' },
    { valor: 'WHATSAPP', rotulo: 'WhatsApp' },
    { valor: 'TELEFONE', rotulo: 'Telefone' },
    { valor: 'EMAIL', rotulo: 'E-mail' },
    { valor: 'CADASTRO_MANUAL', rotulo: 'Cadastro manual' },
    { valor: 'REDES_SOCIAIS', rotulo: 'Redes sociais' },
    { valor: 'INDICACAO', rotulo: 'Indicação' },
    { valor: 'CAMPANHA', rotulo: 'Campanha' },
    { valor: 'OUTRA', rotulo: 'Outra' },
  ];

  readonly filtros = toSignal(this.route.queryParamMap, { requireSync: true });

  readonly leads = toSignal(
    this.route.queryParamMap.pipe(
      tap(() => this.carregando.set(true)),
      switchMap((pm) =>
        this.api
          .listar({
            etapa: pm.get('etapa') ?? undefined,
            origem: pm.get('origem') ?? undefined,
            q: pm.get('q') ?? undefined,
          })
          .pipe(catchError(() => of([] as LeadCard[]))),
      ),
      tap(() => this.carregando.set(false)),
    ),
    { initialValue: [] as LeadCard[] },
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
    this.router.navigate(['/admin/leads'], { queryParams: params });
  }

  aoDigitar(evento: Event): void {
    this.aplicar('q', (evento.target as HTMLInputElement).value.trim());
  }

  aoSelecionar(chave: string, evento: Event): void {
    this.aplicar(chave, (evento.target as HTMLSelectElement).value);
  }

  limpar(): void {
    this.router.navigate(['/admin/leads']);
  }
}
