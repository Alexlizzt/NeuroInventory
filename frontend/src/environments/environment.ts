export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1/inventory-service', // URL de la API de Spring Boot
  keycloak: {
    url: 'http://localhost:9090', // URL de tu contenedor Keycloak
    realm: 'neuroinventory',     // Nombre del reino
    clientId: 'frontend-client'  // Client ID configurado en Keycloak
  }
};