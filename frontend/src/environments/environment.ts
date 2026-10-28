export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1/inventory-service',
  // Vacío = no exportar trazas. Compose lo activa desde config.json y
  // Kubernetes lo deja vacío porque no incluye el colector OTLP.
  tracingUrl: '',
  keycloak: {
    url: 'http://localhost:9090',
    realm: 'neuroinventory',
    clientId: 'frontend-client'
  }
};
