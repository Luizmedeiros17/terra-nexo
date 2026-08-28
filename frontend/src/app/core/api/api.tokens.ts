import { InjectionToken } from '@angular/core';

/**
 * Base da API pública.
 *
 * No navegador vale o caminho relativo `/api` — o servidor SSR encaminha para o
 * container do backend, o que evita CORS e expor a URL interna. No SSR o valor é
 * a URL absoluta do serviço (`API_URL`, ex.: http://backend:8080).
 */
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => '/api',
});
