import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { initTracing } from './app/core/observability/tracing';

// Inicializar OpenTelemetry antes de arrancar la aplicación Angular
initTracing();

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
