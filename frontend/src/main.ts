import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { loadRuntimeConfig } from './app/core/config/runtime-config';
import { initTracing } from './app/core/observability/tracing';

// config.json debe cargarse antes de configurar Keycloak y OpenTelemetry.
loadRuntimeConfig()
  .then(() => {
    initTracing();
    return bootstrapApplication(App, appConfig);
  })
  .catch((err) => console.error(err));
