// src/app/features/visitas/visitas.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VisitaService } from '../../core/services/visita.service';
import { ClienteService } from '../../core/services/cliente.service';
import { InmuebleService } from '../../core/services/inmueble.service';
import { AsesorService } from '../../core/services/asesor.service';
import { ToastService } from '../../core/services/toast.service';
import { Visita, VisitaDTO, Cliente, Inmueble, Asesor, EstadoVisita } from '../../core/models';

@Component({
  selector: 'app-visitas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './visitas.component.html',
  styleUrls: ['./visitas.component.scss']
})
export class VisitasComponent implements OnInit {
  visitas: Visita[] = [];
  clientes: Cliente[] = [];
  inmuebles: Inmueble[] = [];
  asesores: Asesor[] = [];
  loading = true;
  showModal = false;
  submitting = false;
  filtroEstado: EstadoVisita | '' = '';
  estados: EstadoVisita[] = ['PENDIENTE','CONFIRMADA','REALIZADA','CANCELADA','REPROGRAMADA'];
  showCancelModal = false;
  showRealizarModal = false;
  selectedVisita: Visita | null = null;
  observacionInput = '';

  form: VisitaDTO = this.emptyForm();

  constructor(
    private visitaService: VisitaService,
    private clienteService: ClienteService,
    private inmuebleService: InmuebleService,
    private asesorService: AsesorService,
    private toast: ToastService
  ) {}

  ngOnInit() {
    this.cargarVisitas();
    this.clienteService.listar().subscribe({ next: d => this.clientes = d, error: () => {} });
    this.inmuebleService.listar({ disponible: true }).subscribe({ next: d => this.inmuebles = d, error: () => {} });
    this.asesorService.listar().subscribe({ next: d => this.asesores = d, error: () => {} });
  }

  cargarVisitas() {
    this.loading = true;
    this.visitaService.listar(this.filtroEstado ? { estado: this.filtroEstado as EstadoVisita } : {}).subscribe({
      next: d => { this.visitas = d; this.loading = false; },
      error: (e: any) => { this.toast.error(e.message); this.loading = false; }
    });
  }

  abrirCrear() { this.form = this.emptyForm(); this.showModal = true; }

  guardar() {
    if (!this.form.idVisita || !this.form.idCliente || !this.form.codigoInmueble) { this.toast.warn('Completa los campos requeridos'); return; }
    this.submitting = true;
    this.visitaService.programar(this.form).subscribe({
      next: () => { this.toast.success('Visita programada'); this.showModal = false; this.submitting = false; this.cargarVisitas(); },
      error: (e: any) => { this.toast.error(e.message); this.submitting = false; }
    });
  }

  confirmar(v: Visita) {
    this.visitaService.confirmar(v.idVisita).subscribe({
      next: () => { this.toast.success('Visita confirmada'); this.cargarVisitas(); },
      error: (e: any) => this.toast.error(e.message)
    });
  }

  abrirCancelar(v: Visita) { this.selectedVisita = v; this.observacionInput = ''; this.showCancelModal = true; }

  confirmarCancelacion() {
    if (!this.selectedVisita) return;
    this.visitaService.cancelar(this.selectedVisita.idVisita, this.observacionInput).subscribe({
      next: () => { this.toast.success('Visita cancelada'); this.showCancelModal = false; this.cargarVisitas(); },
      error: (e: any) => this.toast.error(e.message)
    });
  }

  abrirRealizar(v: Visita) { this.selectedVisita = v; this.observacionInput = ''; this.showRealizarModal = true; }

  confirmarRealizada() {
    if (!this.selectedVisita) return;
    this.visitaService.marcarRealizada(this.selectedVisita.idVisita, this.observacionInput).subscribe({
      next: () => { this.toast.success('Visita marcada como realizada'); this.showRealizarModal = false; this.cargarVisitas(); },
      error: (e: any) => this.toast.error(e.message)
    });
  }

  procesarSiguiente() {
    this.visitaService.procesarSiguiente().subscribe({
      next: v => { this.toast.success(`Visita procesada: ${v.idVisita}`); this.cargarVisitas(); },
      error: (e: any) => this.toast.error(e.message)
    });
  }

  estadoBadge(e: string) {
    return { PENDIENTE:'badge-amber', CONFIRMADA:'badge-blue', REALIZADA:'badge-green', CANCELADA:'badge-red', REPROGRAMADA:'badge-purple' }[e] || 'badge-gray';
  }

  private emptyForm(): VisitaDTO { return { idVisita:'', idCliente:'', codigoInmueble:'', fecha:'', hora:'', idAsesor:'', observaciones:'' }; }
}
