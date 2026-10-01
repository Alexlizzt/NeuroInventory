import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { environment } from '../../../environments/environment';

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
    this.keycloakInstance = new Keycloak({
      url: environment.keycloak.url,
      realm: environment.keycloak.realm,
      clientId: environment.keycloak.clientId
    });

    try {
      const authenticated = await this.keycloakInstance.init({
        onLoad: 'login-required', // Exige autenticación al entrar
        checkLoginIframe: false,
        pkceMethod: 'S256'
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