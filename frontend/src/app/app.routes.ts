import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login').then((m) => m.LoginPage),
  },
  {
    path: 'movimentos',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/movimentos').then((m) => m.MovimentosPage),
  },
  { path: '', pathMatch: 'full', redirectTo: 'movimentos' },
  { path: '**', redirectTo: 'movimentos' },
];
