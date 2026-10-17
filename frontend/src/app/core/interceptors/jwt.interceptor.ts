import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from, switchMap, catchError } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { environment } from '../../../environments/environment';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const isApiUrl = req.url.startsWith(environment.apiUrl);

  // Solo adjuntar token a peticiones de la API
  if (!isApiUrl) {
    return next(req);
  }

  return from(authService.getToken()).pipe(
    switchMap(token => {
      if (token) {
        const authReq = req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        });
        return next(authReq);
      }
      // Sin token, enviar petición igual (el servidor devolverá 401 y el errorInterceptor la manejará)
      return next(req);
    }),
    catchError(error => {
      console.error('[JWT Interceptor] Error:', error);
      throw error;
    })
  );
};
