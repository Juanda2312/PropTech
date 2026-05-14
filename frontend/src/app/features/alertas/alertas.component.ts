// src/app/features/alertas/alertas.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AlertaService } from '../../core/services/alerta.service';
import { ToastService } from '../../core/services/toast.service';
import { Alerta, NivelAtencion } from '../../core/models';

@Component({
  selector: 'app-alertas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './alertas.component.html',
  styleUrls: ['./alertas.component.scss']
})
export class AlertasComponent implements OnInit {
  alertas: Alerta[] = [];
  loading = true;
  generando = false;
  procesando = false;
  filtroNivel: NivelAtencion | '' = '';
  filtroAbiertas = false;
  niveles: NivelAtencion[] = ['BAJO', 'MEDIO', 'ALTO', 'CRITICO'];
  totalPendientes = 0;

  constructor(private alertaService: AlertaService, private toast: ToastService) {}

  ngOnInit() { this.cargarAlertas(); this.cargarTotal(); }

  cargarAlertas() {
    this.loading = true;
    this.alertaService.listar({
      nivel: this.filtroNivel as NivelAtencion || undefined,
      abiertas: this.filtroAbiertas || undefined
    }).subscribe({
      next: d => { this.alertas = d; this.loading = false; },
      error: (e: any) => { this.toast.error(e.message); this.loading = false; }
    });
  }

  cargarTotal() {
    this.alertaService.totalPendientes().subscribe({
      next: r => this.totalPendientes = r.totalPendientes,
      error: () => {}
    });
  }

  generar() {
    this.generando = true;
    this.alertaService.generarAlertas().subscribe({
      next: alertas => {
        this.toast.success(`${alertas.length} alerta(s) generadas`);
        this.generando = false;
        this.cargarAlertas();
        this.cargarTotal();
      },
      error: (e: any) => { this.toast.error(e.message); this.generando = false; }
    });
  }

  procesarSiguiente() {
    this.procesando = true;
    this.alertaService.procesarSiguiente().subscribe({
      next: a => {
        // Cerrar la alerta procesada
        this.alertaService.cerrar(a.idAlerta).subscribe({
          next: () => {
            this.toast.info(`Procesada y cerrada: ${a.tipoAlerta}`);
            this.procesando = false;
            this.cargarAlertas();
            this.cargarTotal();
          },
          error: () => {
            // Ya estaba cerrada u otro error, igual refrescar
            this.toast.info(`Procesada: ${a.tipoAlerta}`);
            this.procesando = false;
            this.cargarAlertas();
            this.cargarTotal();
          }
        });
      },
      error: (e: any) => { this.toast.error(e.message); this.procesando = false; }
    });
  }
  cerrar(alerta: Alerta) {
    this.alertaService.cerrar(alerta.idAlerta).subscribe({
      next: () => { this.toast.success('Alerta cerrada'); this.cargarAlertas(); this.cargarTotal(); },
      error: (e: any) => this.toast.error(e.message)
    });
  }

  limpiarFiltros() { this.filtroNivel = ''; this.filtroAbiertas = false; this.cargarAlertas(); }

  nivelClass(nivel: string) {
    return { BAJO:'badge-green', MEDIO:'badge-amber', ALTO:'badge-orange', CRITICO:'badge-red' }[nivel] || 'badge-gray';
  }

  nivelDotClass(nivel: string) {
    return { BAJO:'dot-bajo', MEDIO:'dot-medio', ALTO:'dot-alto', CRITICO:'dot-critico' }[nivel] || '';
  }
}
