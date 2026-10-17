import {
  WebTracerProvider,
  ConsoleSpanExporter,
  BatchSpanProcessor,
} from '@opentelemetry/sdk-trace-web';
import {
  OTLPTraceExporter,
} from '@opentelemetry/exporter-trace-otlp-http';
import {
  XMLHttpRequestInstrumentation,
} from '@opentelemetry/instrumentation-xml-http-request';
import {
  DocumentLoadInstrumentation,
} from '@opentelemetry/instrumentation-document-load';
import { registerInstrumentations } from '@opentelemetry/instrumentation';
import { resourceFromAttributes } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';

const OTLP_ENDPOINT =
  typeof window !== 'undefined' && window.location
    ? `${window.location.protocol}//${window.location.hostname}:4318`
    : 'http://localhost:4318';

export function initTracing(): void {
  const provider = new WebTracerProvider({
    resource: resourceFromAttributes({
      [SemanticResourceAttributes.SERVICE_NAME]: 'neuroinventory-frontend',
      [SemanticResourceAttributes.SERVICE_VERSION]: '0.0.1',
    }),
  });

  // Exportador OTLP hacia el colector (por HTTP)
  const otlpExporter = new OTLPTraceExporter({
    url: `${OTLP_ENDPOINT}/v1/traces`,
  });

  provider.addSpanProcessor(new BatchSpanProcessor(otlpExporter));

  // Exportador de consola para depuración local (opcional)
  // provider.addSpanProcessor(new BatchSpanProcessor(new ConsoleSpanExporter()));

  provider.register();

  // Instrumentar peticiones XHR (HttpClient de Angular usa XHR por defecto)
  registerInstrumentations({
    instrumentations: [
      new XMLHttpRequestInstrumentation({
        ignoreUrls: [
          /^http:\/\/localhost:4200\/assets/,
        ],
      }),
      new DocumentLoadInstrumentation(),
    ],
  });
}
