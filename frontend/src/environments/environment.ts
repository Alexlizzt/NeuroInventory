export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1/inventory-service',
  keycloak: {
    url: 'http://localhost:9090',
    realm: 'neuroinventory',
    clientId: 'frontend-client'
  }
};
