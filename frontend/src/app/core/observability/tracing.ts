import {
  WebTracerProvider,
  BatchSpanProcessor,
} from '@opentelemetry/sdk-trace-web';
import {
  OTLPTraceExporter,
} from '@opentelemetry/exporter-trace-otlp-http';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';

const OTLP_ENDPOINT =
  typeof window !== 'undefined' && window.location
    ? `${window.location.protocol}//${window.location.hostname}:4318`
    : 'http://localhost:4318';

export function initTracing(): void {
  const provider = new WebTracerProvider({
    resource: new Resource({
      [SemanticResourceAttributes.SERVICE_NAME]: 'neuroinventory-frontend',
      [SemanticResourceAttributes.SERVICE_VERSION]: '0.0.1',
    }),
  });

  // Exportador OTLP hacia el colector (por HTTP)
  const otlpExporter = new OTLPTraceExporter({
    url: `${OTLP_ENDPOINT}/v1/traces`,
  });

  provider.addSpanProcessor(new BatchSpanProcessor(otlpExporter));

  provider.register();
}
