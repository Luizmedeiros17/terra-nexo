import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { Observable, map, of, tap } from 'rxjs';
import { catchError } from 'rxjs/operators';

/** Resposta do ViaCEP quando o CEP existe. */
export interface EnderecoCep {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  localidade: string;
  uf: string;
  estado?: string;
  ibge?: string;
  ddd?: string;
}

export type ResultadoCep =
  | { situacao: 'encontrado'; endereco: EnderecoCep }
  | { situacao: 'nao-encontrado' }
  | { situacao: 'invalido' }
  | { situacao: 'indisponivel' };

/**
 * Consulta de endereço por CEP no ViaCEP (https://viacep.com.br).
 *
 * O serviço responde com CORS liberado, então a chamada sai direto do
 * navegador — sem intermediar pelo backend. Dois detalhes do contrato exigem
 * cuidado: um CEP inexistente volta com HTTP 200 e corpo `{"erro": "true"}`
 * (string, não booleano), e um CEP malformado volta 400.
 *
 * As respostas ficam em cache na sessão: reabrir o mesmo CEP não gera nova
 * chamada ao serviço externo.
 */
@Injectable({ providedIn: 'root' })
export class CepService {
  private readonly http = inject(HttpClient);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  private readonly cache = new Map<string, ResultadoCep>();

  /** Extrai os oito dígitos, ignorando pontuação. */
  static digitos(cep: string | null | undefined): string {
    return (cep ?? '').replace(/\D/g, '').slice(0, 8);
  }

  /** Formata para 00000-000 enquanto o usuário digita. */
  static formatar(cep: string | null | undefined): string {
    const d = CepService.digitos(cep);
    return d.length > 5 ? `${d.slice(0, 5)}-${d.slice(5)}` : d;
  }

  static valido(cep: string | null | undefined): boolean {
    return CepService.digitos(cep).length === 8;
  }

  buscar(cep: string): Observable<ResultadoCep> {
    const digitos = CepService.digitos(cep);

    if (digitos.length !== 8) {
      return of({ situacao: 'invalido' } as ResultadoCep);
    }
    // No SSR não faz sentido consultar: o cadastro roda apenas no navegador.
    if (!this.isBrowser) {
      return of({ situacao: 'indisponivel' } as ResultadoCep);
    }
    const emCache = this.cache.get(digitos);
    if (emCache) {
      return of(emCache);
    }

    return this.http
      .get<EnderecoCep & { erro?: boolean | string }>(`https://viacep.com.br/ws/${digitos}/json/`)
      .pipe(
        map((resposta): ResultadoCep => {
          // A API já devolveu `erro` como booleano e como string — trate os dois.
          const naoEncontrado = resposta?.erro === true || resposta?.erro === 'true';
          return naoEncontrado
            ? { situacao: 'nao-encontrado' }
            : { situacao: 'encontrado', endereco: resposta };
        }),
        catchError(() => of({ situacao: 'indisponivel' } as ResultadoCep)),
        tap((resultado) => this.cache.set(digitos, resultado)),
      );
  }
}
