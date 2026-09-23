import { Component, inject, OnInit } from '@angular/core';

import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';

import { APP_CONFIG } from '../../../../core/config/app-config.token';
import { AuthService } from '../../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly config = inject(APP_CONFIG);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);

  protected loading = false;

  async ngOnInit(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    await this.authService.initialize(this.config.auth);

    if (this.authService.isAuthenticated()) {
      await this.router.navigate(['/dashboard']);
    }
  }

  protected async login(): Promise<void> {
    this.loading = true;

    await this.authService.initialize(this.config.auth);

    await this.authService.login();
  }
}
