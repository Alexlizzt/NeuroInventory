import { AuthConfig } from '../auth/auth.config';

export interface AppConfig {
  apiUrl: string;
  aiApiUrl: string;
  auth: AuthConfig;
}