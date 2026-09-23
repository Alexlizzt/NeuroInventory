import { Component } from '@angular/core';

import { NavigationItemComponent } from './navigation-item/navigation-item';
import { NavigationItem } from './navigation-item';

@Component({
  selector: 'app-sidebar',
  imports: [NavigationItemComponent,],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {

  protected readonly navigationItems: NavigationItem[] = [
    {
      label: 'Dashboard',
      route: '/dashboard',
    },
    {
      label: 'Products',
      route: '/products',
    },
    {
      label: 'Categories',
      route: '/categories',
    },
    {
      label: 'Inventory',
      route: '/inventory',
    },
    {
      label: 'AI Assistant',
      route: '/ai-assistant',
    },
    {
      label: 'Reports',
      route: '/reports',
    },
  ];

}