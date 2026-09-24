import { Injectable, signal } from '@angular/core';

import { AuthConfig } from './auth.config';
import { AuthUser } from './models/auth-user';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private keycloak: import('keycloak-js').default | null = null;

  private readonly authenticated = signal(false);
  private readonly currentUser = signal<AuthUser | null>(null);

  readonly isAuthenticated = this.authenticated.asReadonly();
  readonly user = this.currentUser.asReadonly();

  async initialize(config: AuthConfig): Promise<void> {
    if (this.keycloak) {
      return;
    }

    const { default: Keycloak } = await import('keycloak-js');

    this.keycloak = new Keycloak({
      url: config.url,
      realm: config.realm,
      clientId: config.clientId,
    });

    const authenticated = await this.keycloak.init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
    });

    this.authenticated.set(authenticated);

    if (authenticated) {
      await this.loadUser();
    }
  }

  async login(): Promise<void> {
    if (!this.keycloak) {
      throw new Error('Keycloak has not been initialized.');
    }

    await this.keycloak.login();
  }

  async logout(): Promise<void> {
    if (!this.keycloak) {
      return;
    }

    await this.keycloak.logout();
  }

  getToken(): string | null {
    return this.keycloak?.token ?? null;
  }

  private async loadUser(): Promise<void> {
    if (!this.keycloak?.authenticated) {
      return;
    }

    const profile = await this.keycloak.loadUserProfile();

    this.currentUser.set({
      id: this.keycloak.subject ?? '',
      username: profile.username ?? '',
      name: `${profile.firstName ?? ''} ${profile.lastName ?? ''}`.trim(),
      email: profile.email ?? '',
    });
  }
}
