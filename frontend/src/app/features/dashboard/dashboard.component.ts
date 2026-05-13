// src/app/features/dashboard/dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { InmuebleService } from '../../core/services/inmueble.service';
import { ClienteService } from '../../core/services/cliente.service';
import { AsesorService } from '../../core/services/asesor.service';
import { VisitaService } from '../../core/services/visita.service';
import { AlertaService } from '../../core/services/alerta.service';
import { PlataformaService } from '../../core/services/plataforma.service';
import { EventoService } from '../../core/services/evento.service';
import { ToastService } from '../../core/services/toast.service';
import { Inmueble, Alerta, Asesor, Visita } from '../../core/models';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  loading = true;
  stats = { inmuebles: 0, clientes: 0, asesores: 0, visitasPendientes: 0, alertasAbiertas: 0 };
  inmueblesRecientes: Inmueble[] = [];
  alertasCriticas: Alerta[] = [];
  rankingAsesores: Asesor[] = [];
  visitasPendientes: Visita[] = [];
  rankingZonas: { zona: string; visitas: number }[] = [];
  generandoAlertas = false;
  detectando = false;

  constructor(
    private inmuebleService: InmuebleService,
    private clienteService: ClienteService,
    private asesorService: AsesorService,
    private visitaService: VisitaService,
    private alertaService: AlertaService,
    private plataformaService: PlataformaService,
    private eventoService: EventoService,
    private toast: ToastService
  ) {}

  ngOnInit() { this.cargarDatos(); }

  cargarDatos() {
    this.loading = true;
    forkJoin({
      inmuebles: this.inmuebleService.listar(),
      clientes: this.clienteService.listar(),
      asesores: this.asesorService.listar(),
      visitasPend: this.visitaService.totalPendientes(),
      alertas: this.alertaService.listar({ abiertas: true }),
      rankingAsesores: this.plataformaService.rankingAsesores(),
      visitas: this.visitaService.listar({ estado: 'PENDIENTE' }),
      zonas: this.plataformaService.rankingZonas()
    }).subscribe({
      next: data => {
        this.stats.inmuebles = data.inmuebles.length;
        this.stats.clientes = data.clientes.length;
        this.stats.asesores = data.asesores.length;
        this.stats.visitasPendientes = data.visitasPend.totalPendientes;
        this.stats.alertasAbiertas = data.alertas.length;
        this.inmueblesRecientes = data.inmuebles.slice(0, 5);
        this.alertasCriticas = data.alertas
          .filter(a => a.nivel === 'CRITICO' || a.nivel === 'ALTO')
          .slice(0, 5);
        this.rankingAsesores = data.rankingAsesores.slice(0, 5);
        this.visitasPendientes = data.visitas.slice(0, 5);
        this.rankingZonas = Object.entries(data.zonas)
          .map(([zona, visitas]) => ({ zona, visitas }))
          .sort((a, b) => b.visitas - a.visitas)
          .slice(0, 5);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  generarAlertas() {
    this.generandoAlertas = true;
    this.alertaService.generarAlertas().subscribe({
      next: alertas => {
        this.toast.success(`${alertas.length} alerta(s) generadas`);
        this.generandoAlertas = false;
        this.cargarDatos();
      },
      error: (e: any) => { this.toast.error(e.message); this.generandoAlertas = false; }
    });
  }

  detectarComportamientos() {
    this.detectando = true;
    this.eventoService.detectarComportamientos().subscribe({
      next: () => {
        this.toast.success('Detección completada');
        this.detectando = false;
        this.cargarDatos();
      },
      error: (e: any) => { this.toast.error(e.message); this.detectando = false; }
    });
  }

  nivelClass(nivel: string) {
    return { BAJO:'badge-green', MEDIO:'badge-amber', ALTO:'badge-orange', CRITICO:'badge-red' }[nivel] || 'badge-gray';
  }
  formatPrice(p: number) { return new Intl.NumberFormat('es-CO', { style:'currency', currency:'COP', maximumFractionDigits:0 }).format(p); }
}
