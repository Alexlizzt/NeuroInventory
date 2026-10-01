import { ApplicationConfig, provideAppInitializer, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

import { routes } from './app.routes';
import { AuthService } from './core/auth/auth.service';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(), // Requerido para Angular Material
    provideHttpClient(
      withInterceptors([jwtInterceptor, errorInterceptor])
    ),
    // Inicializa Keycloak antes de renderizar la App
    provideAppInitializer(() => {
      const authService = inject(AuthService);
      return authService.init();
    })
  ]
};