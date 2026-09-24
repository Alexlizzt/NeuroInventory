import { AppConfig } from './app-config';

export const environment: AppConfig = {
  apiUrl: 'http://localhost:8080/api/v1/inventory-service',
  aiApiUrl: 'http://localhost:8080/api/v1/inventory-service/ai',

  auth: {
    url: 'http://localhost:9090',
    realm: 'neuroinventory',
    clientId: 'frontend-client',
  },
};