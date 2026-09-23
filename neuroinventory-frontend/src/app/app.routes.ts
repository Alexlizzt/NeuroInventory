import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layout/shell/shell').then((m) => m.Shell),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/pages/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'products',
        loadChildren: () => import('./features/products/routes').then((m) => m.PRODUCT_ROUTES),
      },
      {
        path: 'categories',
        loadChildren: () => import('./features/categories/routes').then((m) => m.CATEGORY_ROUTES),
      },
      {
        path: 'inventory',
        loadChildren: () => import('./features/inventory/routes').then((m) => m.INVENTORY_ROUTES),
      },
    ],
  },

  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard',
  },
];
