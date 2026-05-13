// src/app/features/operaciones/operaciones.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OperacionService } from '../../core/services/operacion.service';
import { ClienteService } from '../../core/services/cliente.service';
import { InmuebleService } from '../../core/services/inmueble.service';
import { AsesorService } from '../../core/services/asesor.service';
import { ToastService } from '../../core/services/toast.service';
import { Operacion, OperacionDTO, Cliente, Inmueble, Asesor, TipoOperacion } from '../../core/models';

@Component({
  selector: 'app-operaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './operaciones.component.html',
  styleUrls: ['./operaciones.component.scss']
})
export class OperacionesComponent implements OnInit {
  operaciones: Operacion[] = [];
  clientes: Cliente[] = [];
  inmuebles: Inmueble[] = [];
  asesores: Asesor[] = [];
  loading = true;
  showModal = false;
  submitting = false;
  filtroTipo: TipoOperacion | '' = '';
  tipos: TipoOperacion[] = ['ARRIENDO','VENTA','RENOVACION','CANCELACION'];

  form: OperacionDTO = this.emptyForm();

  constructor(
    private operacionService: OperacionService,
    private clienteService: ClienteService,
    private inmuebleService: InmuebleService,
    private asesorService: AsesorService,
    private toast: ToastService
  ) {}

  ngOnInit() {
    this.cargarOperaciones();
    this.clienteService.listar().subscribe({ next: d => this.clientes = d, error: () => {} });
    this.inmuebleService.listar().subscribe({ next: d => this.inmuebles = d, error: () => {} });
    this.asesorService.listar().subscribe({ next: d => this.asesores = d, error: () => {} });
  }

  cargarOperaciones() {
    this.loading = true;
    this.operacionService.listar(this.filtroTipo ? { tipo: this.filtroTipo as TipoOperacion } : {}).subscribe({
      next: d => { this.operaciones = d; this.loading = false; },
      error: (e: any) => { this.toast.error(e.message); this.loading = false; }
    });
  }

  abrirCrear() { this.form = this.emptyForm(); this.showModal = true; }

  guardar() {
    if (!this.form.idOperacion || !this.form.codigoInmueble || !this.form.idCliente) { this.toast.warn('Completa los campos requeridos'); return; }
    this.submitting = true;
    this.operacionService.registrar(this.form).subscribe({
      next: () => { this.toast.success('Operación registrada'); this.showModal = false; this.submitting = false; this.cargarOperaciones(); },
      error: (e: any) => { this.toast.error(e.message); this.submitting = false; }
    });
  }

  cancelar(op: Operacion) {
    if (!confirm('¿Cancelar esta operación?')) return;
    this.operacionService.cancelar(op.idOperacion).subscribe({
      next: () => { this.toast.success('Operación cancelada'); this.cargarOperaciones(); },
      error: (e: any) => this.toast.error(e.message)
    });
  }

  cerrar(op: Operacion) {
    if (!confirm('¿Cerrar esta operación?')) return;
    this.operacionService.cerrar(op.idOperacion).subscribe({
      next: () => { this.toast.success('Operación cerrada'); this.cargarOperaciones(); },
      error: (e: any) => this.toast.error(e.message)
    });
  }

  tipoBadge(t: string) { return { VENTA:'badge-teal', ARRIENDO:'badge-purple', RENOVACION:'badge-blue', CANCELACION:'badge-red' }[t] || 'badge-gray'; }
  estadoBadge(e: string) { return e === 'CERRADO' ? 'badge-green' : e === 'CANCELADO' ? 'badge-red' : 'badge-amber'; }
  formatPrice(p: number) { return new Intl.NumberFormat('es-CO', { style:'currency', currency:'COP', maximumFractionDigits:0 }).format(p); }

  private emptyForm(): OperacionDTO {
    const today = new Date().toISOString().split('T')[0];
    return { idOperacion:'', codigoInmueble:'', idCliente:'', idAsesor:'', fecha: today, tipoOperacion:'VENTA', valorAcordado:0, comision:3, estadoProceso:'EN_PROCESO' };
  }
}
