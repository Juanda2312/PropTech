import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-visitas',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './visitas.html',
  styleUrl: './visitas.css'
})
export class Visitas implements OnInit {
  mostrarForm = false;
  modoEdicion = false;
  busqueda = '';
  filtroEstado = '';
  cargando = false;

  form: any = { id: '', cliente: '', inmueble: '', fecha: '', hora: '', asesor: '', estado: 'pendiente', observaciones: '', idCliente: '', codigoInmueble: '', idAsesor: '' };

  visitas: any[] = [];
  visitasFiltradas: any[] = [];

  constructor(private api: ApiService) {}

  ngOnInit() { this.cargar(); }

  cargar() {
    this.cargando = true;
    this.api.getVisitas().subscribe({
      next: (data) => {
        this.visitas = data.map(v => ({
          id: v.idVisita,
          cliente: v.cliente?.nombre || v.idCliente || '',
          inmueble: v.inmueble?.nombre || v.codigoInmueble || '',
          fecha: v.fecha,
          hora: v.hora,
          asesor: v.asesor?.nombre || v.idAsesor || '',
          estado: v.estado?.toLowerCase() || 'pendiente',
          observaciones: v.observaciones || '',
          idCliente: v.cliente?.id || '',
          codigoInmueble: v.inmueble?.codigo || '',
          idAsesor: v.asesor?.id || ''
        }));
        this.filtrar();
        this.cargando = false;
      },
      error: () => { this.cargando = false; }
    });
  }

  filtrar() {
    this.visitasFiltradas = this.visitas.filter(v =>
      (!this.busqueda || v.cliente?.toLowerCase().includes(this.busqueda.toLowerCase()) ||
        v.inmueble?.toLowerCase().includes(this.busqueda.toLowerCase())) &&
      (!this.filtroEstado || v.estado === this.filtroEstado)
    );
  }

  editar(v: any) { this.form = { ...v }; this.modoEdicion = true; this.mostrarForm = true; }

  cancelar(v: any) {
    if (!confirm('¿Cancelar esta visita?')) return;
    this.api.cancelarVisita(v.id, 'Cancelada desde panel admin').subscribe({
      next: () => this.cargar(),
      error: () => { v.estado = 'cancelada'; this.filtrar(); }
    });
  }

  guardar() {
    const payload = {
      idVisita: this.form.id || 'VIS-' + Date.now(),
      idCliente: this.form.idCliente || '1001',
      codigoInmueble: this.form.codigoInmueble || this.form.inmueble,
      fecha: this.form.fecha,
      hora: this.form.hora,
      idAsesor: this.form.idAsesor || 'AS001',
      estado: 'PENDIENTE',
      observaciones: this.form.observaciones
    };

    if (this.modoEdicion) {
      this.api.reprogramarVisita(this.form.id, payload).subscribe({
        next: () => { this.cargar(); this.cerrarForm(); },
        error: () => { this.cerrarForm(); }
      });
    } else {
      this.api.crearVisita(payload).subscribe({
        next: () => { this.cargar(); this.cerrarForm(); },
        error: () => { this.cerrarForm(); }
      });
    }
  }

  cerrarForm() {
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.form = { id: '', cliente: '', inmueble: '', fecha: '', hora: '', asesor: '', estado: 'pendiente', observaciones: '', idCliente: '', codigoInmueble: '', idAsesor: '' };
  }
}
