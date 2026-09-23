import { inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';

import { APP_CONFIG } from '../config/app-config.token';
import { AuthService } from './auth.service';

export function initializeAuth(): Promise<void> {
  const platformId = inject(PLATFORM_ID);
  const authService = inject(AuthService);
  const config = inject(APP_CONFIG);

  if (!isPlatformBrowser(platformId)) {
    return Promise.resolve();
  }

  return authService.initialize(config.auth);
}