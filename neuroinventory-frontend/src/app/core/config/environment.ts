import { AppConfig } from './app-config';

export const environment: AppConfig = {
  apiUrl: 'http://localhost:8080/api',
  aiApiUrl: 'http://localhost:8000/api',

  auth: {
    url: 'http://localhost:9090',
    realm: 'neuroinventory',
    clientId: 'frontend-client',
  },
};