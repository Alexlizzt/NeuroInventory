import { ApplicationConfig, provideAppInitializer, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

import { routes } from './app.routes';
import { AuthService } from './core/auth/auth.service';
import { loadRuntimeConfig } from './core/config/runtime-config';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(), // Requerido para Angular Material
    provideHttpClient(
      withInterceptors([jwtInterceptor, errorInterceptor])
    ),
    // Carga config.json antes que cualquier servicio lea environment.
    provideAppInitializer(() => loadRuntimeConfig()),
    // Inicializa Keycloak antes de renderizar la App
    provideAppInitializer(() => {
      const authService = inject(AuthService);
      return authService.init();
    })
  ]
};