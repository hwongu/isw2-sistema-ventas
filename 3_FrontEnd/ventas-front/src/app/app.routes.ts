import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent) },
  { path: 'productos', loadComponent: () => import('./features/productos/productos.component').then((m) => m.ProductosComponent) },
  { path: 'ventas', loadComponent: () => import('./features/ventas/ventas.component').then((m) => m.VentasComponent) },
  { path: 'pagos', loadComponent: () => import('./features/pagos/pagos.component').then((m) => m.PagosComponent) },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' },
];
