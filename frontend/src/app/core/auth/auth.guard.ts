import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);

  if (!authService.isLoggedIn()) {
    authService.login();
    return false;
  }

  // Validación opcional por Roles definidos en la ruta (data: { roles: ['ADMIN'] })
  const expectedRoles = route.data?.['roles'] as string[];
  if (expectedRoles && expectedRoles.length > 0) {
    const hasRole = expectedRoles.some(role => authService.hasRole(role));
    if (!hasRole) {
      console.warn('Usuario no posee el rol necesario para esta ruta.');
      return false;
    }
  }

  return true;
};