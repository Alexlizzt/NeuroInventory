import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { environment } from '../../../environments/environment';

/**
 * keycloak-js 26 usa crypto.randomUUID() incluso sin PKCE, pero los
 * navegadores solo la exponen en contextos seguros (HTTPS o localhost).
 * En HTTP con un host propio (neuroinventory.local) se polyfilla usando
 * crypto.getRandomValues(), que sí está disponible.
 */
function ensureCryptoRandomUUID(): void {
  if (typeof crypto === 'undefined' || typeof crypto.randomUUID === 'function') {
    return;
  }

  Object.defineProperty(crypto, 'randomUUID', {
    value: () => {
      const bytes = crypto.getRandomValues(new Uint8Array(16));
      bytes[6] = (bytes[6] & 0x0f) | 0x40;
      bytes[8] = (bytes[8] & 0x3f) | 0x80;
      const hex = Array.from(bytes, byte => byte.toString(16).padStart(2, '0')).join('');
      return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
    },
    configurable: true,
  });
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private keycloakInstance!: Keycloak;

  /**
   * Inicializa el cliente de Keycloak con flujo PKCE.
   * Se ejecuta antes de que arranque la aplicación Angular.
   */
  async init(): Promise<boolean> {
    ensureCryptoRandomUUID();
    this.keycloakInstance = new Keycloak({
      url: environment.keycloak.url,
      realm: environment.keycloak.realm,
      clientId: environment.keycloak.clientId
    });

    // S256 usa Web Crypto, que el navegador no expone en orígenes HTTP que no
    // sean de confianza (por ejemplo neuroinventory.local). En ese caso se
    // desactiva PKCE; el realm acepta peticiones con y sin PKCE.
    const pkceMethod = window.isSecureContext ? 'S256' : false;

    try {
      const authenticated = await this.keycloakInstance.init({
        onLoad: 'login-required', // Exige autenticación al entrar
        checkLoginIframe: false,
        pkceMethod
      });
      return authenticated;
    } catch (error) {
      console.error('Error al inicializar Keycloak:', error);
      return false;
    }
  }

  /**
   * Retorna el token JWT actual. Renueva el token automáticamente si está por expirar.
   */
  async getToken(): Promise<string | undefined> {
    if (!this.keycloakInstance) return undefined;
    try {
      // Si el token expira en menos de 30 segundos, lo renueva
      await this.keycloakInstance.updateToken(30);
      return this.keycloakInstance.token;
    } catch (error) {
      console.error('Error actualizando el token de Keycloak:', error);
      this.login();
      return undefined;
    }
  }

  isLoggedIn(): boolean {
    return !!this.keycloakInstance?.authenticated;
  }

  getUserProfile() {
    return this.keycloakInstance?.loadUserProfile();
  }

  getUsername(): string {
    return this.keycloakInstance?.tokenParsed?.['preferred_username'] || '';
  }

  getUserRoles(): string[] {
    return this.keycloakInstance?.realmAccess?.roles || [];
  }

  hasRole(role: string): boolean {
    return this.getUserRoles().includes(role);
  }

  login(): void {
    this.keycloakInstance.login();
  }

  logout(): void {
    this.keycloakInstance.logout({
      redirectUri: window.location.origin
    });
  }
}