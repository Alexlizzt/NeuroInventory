import { environment } from '../../../environments/environment';

/**
 * Configuración que puede sobreescribirse en tiempo de ejecución mediante
 * `config.json`. Permite reutilizar la misma imagen de producción en Docker
 * Compose, Kubernetes o cualquier otro entorno sin recompilar el bundle.
 */
interface RuntimeConfig {
  apiUrl?: string;
  tracingUrl?: string;
  keycloakUrl?: string;
  keycloakRealm?: string;
  keycloakClientId?: string;
}

/**
 * Carga `config.json` (servido desde `public/`) antes de inicializar Keycloak.
 * Si el fichero no existe se mantienen los valores de `environment.ts`, de modo
 * que el flujo de desarrollo con `ng serve` sigue funcionando igual.
 */
export async function loadRuntimeConfig(): Promise<void> {
  try {
    const response = await fetch('config.json', { cache: 'no-store' });

    if (!response.ok) {
      return;
    }

    const config = (await response.json()) as RuntimeConfig;

    if (config.apiUrl) {
      environment.apiUrl = config.apiUrl;
    }
    environment.tracingUrl = config.tracingUrl || '';
    if (config.keycloakUrl) {
      environment.keycloak.url = config.keycloakUrl;
    }
    if (config.keycloakRealm) {
      environment.keycloak.realm = config.keycloakRealm;
    }
    if (config.keycloakClientId) {
      environment.keycloak.clientId = config.keycloakClientId;
    }
  } catch {
    // Sin configuración remota se usan los valores por defecto del entorno.
  }
}
