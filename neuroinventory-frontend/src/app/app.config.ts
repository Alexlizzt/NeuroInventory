import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideAppInitializer,
} from '@angular/core';

import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { provideClientHydration } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';

import { APP_CONFIG } from './core/config/app-config.token';
import { environment } from './core/config/environment';

import { apiUrlInterceptor } from './core/http/interceptors/api-url.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(),

    provideHttpClient(withInterceptors([apiUrlInterceptor])),

    {
      provide: APP_CONFIG,
      useValue: environment,
    },
  ],
};
