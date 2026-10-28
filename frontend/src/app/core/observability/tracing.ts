import {
  WebTracerProvider,
  BatchSpanProcessor,
} from '@opentelemetry/sdk-trace-web';
import {
  OTLPTraceExporter,
} from '@opentelemetry/exporter-trace-otlp-http';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { environment } from '../../../environments/environment';

export function initTracing(): void {
  // Sin colector configurado no se registra el exportador: evita errores de
  // red en Kubernetes, donde el stack de observabilidad no está desplegado.
  if (!environment.tracingUrl) {
    return;
  }

  const provider = new WebTracerProvider({
    resource: new Resource({
      [SemanticResourceAttributes.SERVICE_NAME]: 'neuroinventory-frontend',
      [SemanticResourceAttributes.SERVICE_VERSION]: '0.0.1',
    }),
  });

  // Exportador OTLP hacia el colector (por HTTP)
  const otlpExporter = new OTLPTraceExporter({
    url: `${environment.tracingUrl}/v1/traces`,
  });

  provider.addSpanProcessor(new BatchSpanProcessor(otlpExporter));

  provider.register();
}
