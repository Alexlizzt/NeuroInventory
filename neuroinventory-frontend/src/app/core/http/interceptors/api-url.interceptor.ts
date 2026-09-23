import { HttpInterceptorFn } from '@angular/common/http';

import { inject } from '@angular/core';

import { APP_CONFIG } from '../../config/app-config.token';

export const apiUrlInterceptor: HttpInterceptorFn = (req, next) => {
  const config = inject(APP_CONFIG);

  if (!req.url.startsWith('/')) {
    return next(req);
  }

  const apiUrl = `${config.apiUrl}${req.url}`;

  const apiRequest = req.clone({
    url: apiUrl,
  });

  return next(apiRequest);
};
