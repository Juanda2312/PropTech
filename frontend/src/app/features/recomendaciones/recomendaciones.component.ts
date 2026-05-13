// src/app/features/recomendaciones/recomendaciones.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PlataformaService } from '../../core/services/plataforma.service';
import { ClienteService } from '../../core/services/cliente.service';
import { ToastService } from '../../core/services/toast.service';
import { Recomendacion, Cliente } from '../../core/models';

@Component({
  selector: 'app-recomendaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './recomendaciones.component.html',
  styleUrls: ['./recomendaciones.component.scss']
})
export class RecomendacionesComponent implements OnInit {
  clientes: Cliente[] = [];
  selectedClienteId = '';
  selectedCliente: Cliente | null = null;
  recomendaciones: Recomendacion[] = [];
  loading = false;
  generando = false;

  constructor(
    private plataformaService: PlataformaService,
    private clienteService: ClienteService,
    private toast: ToastService
  ) {}

  ngOnInit() {
    this.clienteService.listar().subscribe({ next: d => this.clientes = d, error: () => {} });
  }

  onClienteChange() {
    this.selectedCliente = this.clientes.find(c => c.id === this.selectedClienteId) || null;
    this.recomendaciones = [];
  }

  generar() {
    if (!this.selectedClienteId) { this.toast.warn('Selecciona un cliente'); return; }
    this.generando = true;
    this.plataformaService.generarRecomendaciones(this.selectedClienteId).subscribe({
      next: d => { this.recomendaciones = d; this.generando = false; },
      error: (e: any) => { this.toast.error(e.message); this.generando = false; }
    });
  }

  formatPrice(p: number | undefined) {
    return new Intl.NumberFormat('es-CO', { style:'currency', currency:'COP', maximumFractionDigits:0 }).format(p ?? 0);
  }

  getPuntajeColor(p: number) {
    if (p >= 80) return 'var(--accent-green)';
    if (p >= 50) return 'var(--gold)';
    return 'var(--text-secondary)';
  }
}
