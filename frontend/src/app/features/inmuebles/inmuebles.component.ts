// src/app/features/inmuebles/inmuebles.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InmuebleService } from '../../core/services/inmueble.service';
import { AsesorService } from '../../core/services/asesor.service';
import { ToastService } from '../../core/services/toast.service';
import { Inmueble, InmuebleDTO, Asesor, TipoInmueble, FinalidadInmueble } from '../../core/models';

@Component({
  selector: 'app-inmuebles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inmuebles.component.html',
  styleUrls: ['./inmuebles.component.scss']
})
export class InmueblesComponent implements OnInit {
  inmuebles: Inmueble[] = [];
  asesores: Asesor[] = [];
  loading = true;
  showModal = false;
  showDetailModal = false;
  editMode = false;
  submitting = false;
  selectedInmueble: Inmueble | null = null;
  similares: Inmueble[] = [];

  tiposInmueble: TipoInmueble[] = ['APARTAMENTO','CASA','LOCAL_COMERCIAL','OFICINA','LOTE','BODEGA'];
  finalidades: FinalidadInmueble[] = ['VENTA','ARRIENDO'];

  filtros = { tipo: '' as any, finalidad: '' as any, ciudad: '', disponible: '' as any, precioMin: 0, precioMax: 0, habitacionesMin: 0 };

  form: InmuebleDTO = this.emptyForm();

  constructor(
    private inmuebleService: InmuebleService,
    private asesorService: AsesorService,
    private toast: ToastService
  ) {}

  ngOnInit() {
    this.cargarAsesores();
    this.cargarInmuebles();
  }

  cargarInmuebles() {
    this.loading = true;
    const f = this.filtros;
    this.inmuebleService.listar({
      tipo: f.tipo || undefined,
      finalidad: f.finalidad || undefined,
      ciudad: f.ciudad || undefined,
      disponible: f.disponible !== '' ? f.disponible === 'true' : undefined,
      precioMin: f.precioMin || undefined,
      precioMax: f.precioMax || undefined,
      habitacionesMin: f.habitacionesMin || undefined
    }).subscribe({ next: d => { this.inmuebles = d; this.loading = false; }, error: (e: any) => { this.toast.error(e.message); this.loading = false; } });
  }

  cargarAsesores() {
    this.asesorService.listar().subscribe({ next: d => this.asesores = d, error: () => {} });
  }

  limpiarFiltros() {
    this.filtros = { tipo: '', finalidad: '', ciudad: '', disponible: '', precioMin: 0, precioMax: 0, habitacionesMin: 0 };
    this.cargarInmuebles();
  }

  abrirCrear() { this.form = this.emptyForm(); this.editMode = false; this.showModal = true; }

  abrirEditar(inm: Inmueble) {
    this.form = {
      codigo: inm.codigo, direccion: inm.direccion, ciudad: inm.ciudad,
      barrio: inm.barrio, tipoInmueble: inm.tipoInmueble, finalidad: inm.finalidad,
      precio: inm.precio, area: inm.area, habitaciones: inm.habitaciones,
      banos: inm.banos, estado: inm.estado, disponibilidad: inm.disponibilidad,
      idAsesor: inm.asesor?.id || ''
    };
    this.editMode = true; this.showModal = true;
  }

  verDetalle(inm: Inmueble) {
    this.selectedInmueble = inm;
    this.showDetailModal = true;
    this.inmuebleService.sugerirSimilares(inm.codigo).subscribe({ next: d => this.similares = d, error: () => this.similares = [] });
  }

  guardar() {
    if (!this.form.codigo || !this.form.idAsesor) { this.toast.warn('Completa los campos requeridos'); return; }
    this.submitting = true;
    const onSuccess = () => {
      this.toast.success(this.editMode ? 'Inmueble actualizado' : 'Inmueble registrado');
      this.showModal = false; this.submitting = false; this.cargarInmuebles();
    };
    const onError = (e: any) => { this.toast.error(e.message); this.submitting = false; };
    if (this.editMode) {
      this.inmuebleService.actualizar(this.form.codigo, this.form).subscribe({ next: onSuccess, error: onError });
    } else {
      this.inmuebleService.registrar(this.form).subscribe({ next: () => onSuccess(), error: onError });
    }
  }

  eliminar(inm: Inmueble) {
    if (!confirm(`¿Eliminar inmueble ${inm.codigo}?`)) return;
    this.inmuebleService.eliminar(inm.codigo).subscribe({
      next: () => { this.toast.success('Inmueble eliminado'); this.cargarInmuebles(); },
      error: (e: any) => this.toast.error(e.message)
    });
  }

  deshacer() {
    this.inmuebleService.deshacerCambio().subscribe({
      next: () => { this.toast.success('Cambio deshecho'); this.cargarInmuebles(); },
      error: (e: any) => this.toast.error(e.message)
    });
  }

  ordenarPorPrecio() {
    this.loading = true;
    this.inmuebleService.ordenadosPorPrecio().subscribe({
      next: d => { this.inmuebles = d; this.loading = false; },
      error: (e: any) => { this.toast.error(e.message); this.loading = false; }
    });
  }

  formatPrice(p: number) { return new Intl.NumberFormat('es-CO', { style:'currency', currency:'COP', maximumFractionDigits:0 }).format(p); }

  private emptyForm(): InmuebleDTO {
    return { codigo:'', direccion:'', ciudad:'', barrio:'', tipoInmueble:'APARTAMENTO', finalidad:'VENTA', precio:0, area:0, habitaciones:0, banos:0, estado:'DISPONIBLE', disponibilidad:true, idAsesor:'' };
  }
}
