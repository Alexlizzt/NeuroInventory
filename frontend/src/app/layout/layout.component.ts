import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

// Angular Material Imports
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AuthService } from '../core/auth/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
}

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatDividerModule,
    MatTooltipModule
  ],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss'
})
export class LayoutComponent implements OnInit {
  private authService = inject(AuthService);

  username = signal<string>('');
  userRoles = signal<string[]>([]);

  // Menú de navegación del dashboard
  navItems: NavItem[] = [
    { label: 'Productos', icon: 'inventory_2', route: '/products' },
    { label: 'Categorías', icon: 'category', route: '/categories' },
    { label: 'Movimientos', icon: 'swap_vert', route: '/inventory' },
    { label: 'Asistente IA (RAG)', icon: 'psychology', route: '/ai-assistant' }
  ];

  async ngOnInit(): Promise<void> {
    this.username.set(this.authService.getUsername());
    this.userRoles.set(this.authService.getUserRoles());
  }

  logout(): void {
    this.authService.logout();
  }
}