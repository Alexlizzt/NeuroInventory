import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

let refreshing = false;

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !refreshing) {
        refreshing = true;
        // Intentar obtener un token fresco antes de redirigir
        authService.getToken().then(() => {
          refreshing = false;
        }).catch(() => {
          refreshing = false;
          // Si no se puede obtener token, redirigir al login
          authService.login();
        });
      } else if (error.status === 403) {
        console.error('Acceso denegado: No tienes permisos para este recurso.');
      }
      return throwError(() => error);
    })
  );
};
