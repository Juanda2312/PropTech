// src/app/app.routes.ts
import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'inmuebles',
    loadComponent: () => import('./features/inmuebles/inmuebles.component').then(m => m.InmueblesComponent)
  },
  {
    path: 'clientes',
    loadComponent: () => import('./features/clientes/clientes.component').then(m => m.ClientesComponent)
  },
  {
    path: 'asesores',
    loadComponent: () => import('./features/asesores/asesores.component').then(m => m.AsesoresComponent)
  },
  {
    path: 'visitas',
    loadComponent: () => import('./features/visitas/visitas.component').then(m => m.VisitasComponent)
  },
  {
    path: 'operaciones',
    loadComponent: () => import('./features/operaciones/operaciones.component').then(m => m.OperacionesComponent)
  },
  {
    path: 'alertas',
    loadComponent: () => import('./features/alertas/alertas.component').then(m => m.AlertasComponent)
  },
  {
    path: 'eventos',
    loadComponent: () => import('./features/eventos/eventos.component').then(m => m.EventosComponent)
  },
  {
    path: 'recomendaciones',
    loadComponent: () => import('./features/recomendaciones/recomendaciones.component').then(m => m.RecomendacionesComponent)
  },
  {
    path: 'grafo',
    loadComponent: () => import('./features/grafo/grafo.component').then(m => m.GrafoComponent)
  },
  { path: '**', redirectTo: 'dashboard' }
];
