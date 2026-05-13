// src/app/features/clientes/clientes.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../core/services/cliente.service';
import { ToastService } from '../../core/services/toast.service';
import { PlataformaService } from '../../core/services/plataforma.service';
import { Cliente, ClienteDTO, Inmueble, Recomendacion, TipoInmueble, EstadoBusqueda, Zona } from '../../core/models';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clientes.component.html',
  styleUrls: ['./clientes.component.scss']
})
export class ClientesComponent implements OnInit {
  clientes: Cliente[] = [];
  loading = true;
  showModal = false;
  showDetailModal = false;
  editMode = false;
  submitting = false;
  selectedCliente: Cliente | null = null;
  historial: Inmueble[] = [];
  favoritos: Inmueble[] = [];
  recomendaciones: Recomendacion[] = [];
  loadingRecs = false;
  activeTab: 'info' | 'historial' | 'favoritos' | 'recomendaciones' = 'info';
  ordenarPorPresupuesto = false;

  tiposInmueble: TipoInmueble[] = ['APARTAMENTO','CASA','LOCAL_COMERCIAL','OFICINA','LOTE','BODEGA'];
  estadosBusqueda: EstadoBusqueda[] = ['ACTIVO','EN_PAUSA','CERRADO'];
  zonas: Zona[] = ['NORTE','SUR','ESTE','OESTE'];

  form: ClienteDTO = this.emptyForm();

  constructor(
    private clienteService: ClienteService,
    private plataformaService: PlataformaService,
    private toast: ToastService
  ) {}

  ngOnInit() { this.cargarClientes(); }

  cargarClientes() {
    this.loading = true;
    this.clienteService.listar(this.ordenarPorPresupuesto).subscribe({
      next: d => { this.clientes = d; this.loading = false; },
      error: (e: any) => { this.toast.error(e.message); this.loading = false; }
    });
  }

  toggleOrden() { this.ordenarPorPresupuesto = !this.ordenarPorPresupuesto; this.cargarClientes(); }

  abrirCrear() { this.form = this.emptyForm(); this.editMode = false; this.showModal = true; }

  abrirEditar(c: Cliente) {
    this.form = {
      id: c.id, nombre: c.nombre, correo: c.correo, telefono: c.telefono,
      tipoCliente: c.tipoCliente, presupuesto: c.presupuesto,
      zonasInteres: c.zonasInteres || [], tipoInmuebleDeseado: c.tipoInmuebleDeseado,
      habitacionesMinimas: c.habitacionesMinimas, estadoBusqueda: c.estadoBusqueda
    };
    this.editMode = true; this.showModal = true;
  }

  verDetalle(c: Cliente) {
    this.selectedCliente = c;
    this.activeTab = 'info';
    this.historial = []; this.favoritos = []; this.recomendaciones = [];
    this.showDetailModal = true;
    this.clienteService.obtenerHistorial(c.id).subscribe({ next: d => this.historial = d, error: () => {} });
    this.clienteService.obtenerFavoritos(c.id).subscribe({ next: d => this.favoritos = d, error: () => {} });
  }

  cargarRecomendaciones() {
    if (!this.selectedCliente) return;
    this.loadingRecs = true;
    this.plataformaService.generarRecomendaciones(this.selectedCliente.id).subscribe({
      next: d => { this.recomendaciones = d; this.loadingRecs = false; },
      error: (e: any) => { this.toast.error(e.message); this.loadingRecs = false; }
    });
  }

  onTabChange(tab: typeof this.activeTab) {
    this.activeTab = tab;
    if (tab === 'recomendaciones' && this.recomendaciones.length === 0) this.cargarRecomendaciones();
  }

  guardar() {
    if (!this.form.id || !this.form.nombre) { this.toast.warn('ID y nombre son requeridos'); return; }
    this.submitting = true;
    const onSuccess = () => {
      this.toast.success(this.editMode ? 'Cliente actualizado' : 'Cliente registrado');
      this.showModal = false; this.submitting = false; this.cargarClientes();
    };
    const onError = (e: any) => { this.toast.error(e.message); this.submitting = false; };
    if (this.editMode) {
      this.clienteService.actualizar(this.form.id, this.form).subscribe({ next: onSuccess, error: onError });
    } else {
      this.clienteService.registrar(this.form).subscribe({ next: () => onSuccess(), error: onError });
    }
  }

  eliminar(c: Cliente) {
    if (!confirm(`¿Eliminar cliente ${c.nombre}?`)) return;
    this.clienteService.eliminar(c.id).subscribe({
      next: () => { this.toast.success('Cliente eliminado'); this.cargarClientes(); },
      error: (e: any) => this.toast.error(e.message)
    });
  }

  estadoBadge(e: string) { return { ACTIVO:'badge-green', EN_PAUSA:'badge-amber', CERRADO:'badge-gray' }[e] || 'badge-gray'; }
  formatPrice(p: number | undefined) {
    return new Intl.NumberFormat('es-CO', { style:'currency', currency:'COP', maximumFractionDigits:0 }).format(p ?? 0);
  }

  private emptyForm(): ClienteDTO {
    return { id:'', nombre:'', correo:'', telefono:'', tipoCliente:'COMPRADOR', presupuesto:0, zonasInteres:[], tipoInmuebleDeseado:'APARTAMENTO', habitacionesMinimas:1, estadoBusqueda:'ACTIVO' };
  }
}
