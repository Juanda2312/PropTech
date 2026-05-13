// src/app/app.component.ts
import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { VisitaService } from './core/services/visita.service';
import { AlertaService } from './core/services/alerta.service';
import { ToastService, Toast } from './core/services/toast.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  sidebarCollapsed = false;
  pendientesCount = 0;
  alertasCount = 0;
  toasts: Toast[] = [];

  constructor(
    private visitaService: VisitaService,
    private alertaService: AlertaService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.cargarContadores();
    this.toastService.toasts$.subscribe(t => this.toasts = t);
    setInterval(() => this.cargarContadores(), 30000);
  }

  cargarContadores() {
    this.visitaService.totalPendientes().subscribe({
      next: r => this.pendientesCount = r.totalPendientes,
      error: () => {}
    });
    this.alertaService.totalPendientes().subscribe({
      next: r => this.alertasCount = r.totalPendientes,
      error: () => {}
    });
  }

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}
