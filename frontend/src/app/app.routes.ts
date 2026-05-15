// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent)
  },
  // Portal del cliente — solo requiere estar autenticado, NO requiere ser admin
  {
    path: 'cliente',
    loadComponent: () => import('./features/cliente-portal/cliente-portal.component').then(m => m.ClientePortalComponent),
    canActivate: [authGuard]
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'inmuebles',
    loadComponent: () => import('./features/inmuebles/inmuebles.component').then(m => m.InmueblesComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'clientes',
    loadComponent: () => import('./features/clientes/clientes.component').then(m => m.ClientesComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'asesores',
    loadComponent: () => import('./features/asesores/asesores.component').then(m => m.AsesoresComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'visitas',
    loadComponent: () => import('./features/visitas/visitas.component').then(m => m.VisitasComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'operaciones',
    loadComponent: () => import('./features/operaciones/operaciones.component').then(m => m.OperacionesComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'alertas',
    loadComponent: () => import('./features/alertas/alertas.component').then(m => m.AlertasComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'eventos',
    loadComponent: () => import('./features/eventos/eventos.component').then(m => m.EventosComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'recomendaciones',
    loadComponent: () => import('./features/recomendaciones/recomendaciones.component').then(m => m.RecomendacionesComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'grafo',
    loadComponent: () => import('./features/grafo/grafo.component').then(m => m.GrafoComponent),
    canActivate: [authGuard, adminGuard]
  },
  { path: '**', redirectTo: 'login' }
];