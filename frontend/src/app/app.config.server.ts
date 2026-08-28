import { ApplicationConfig, mergeApplicationConfig } from '@angular/core';
import { provideServerRendering, withRoutes } from '@angular/ssr';

import { API_BASE_URL } from './core/api/api.tokens';
import { appConfig } from './app.config';
import { serverRoutes } from './app.routes.server';

/**
 * No SSR as chamadas saem do container do frontend para o do backend pela rede
 * interna do Docker. `API_URL` é definida no compose (ex.: http://backend:8080).
 */
const apiUrl = process.env['API_URL'] ?? 'http://localhost:8080';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(withRoutes(serverRoutes)),
    { provide: API_BASE_URL, useValue: `${apiUrl}/api` },
  ],
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
