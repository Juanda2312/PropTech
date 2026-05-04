import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-visitas',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './visitas.html',
  styleUrl: './visitas.css'
})
export class Visitas {
  mostrarForm = false;
  modoEdicion = false;
  busqueda = '';
  filtroEstado = '';

  form: any = { id: '', cliente: '', inmueble: '', fecha: '', hora: '', asesor: '', estado: 'pendiente', observaciones: '' };

  visitas: any[] = [
    { id: 'VIS001', cliente: 'Carlos Martínez', inmueble: 'Apartamento Norte Armenia', fecha: '2026-04-13', hora: '09:00', asesor: 'Juan Tapiero', estado: 'confirmada', observaciones: 'Cliente muy interesado' },
    { id: 'VIS002', cliente: 'Ana Gómez', inmueble: 'Casa La Castellana', fecha: '2026-04-13', hora: '11:30', asesor: 'Jose Bedoya', estado: 'pendiente', observaciones: '' },
    { id: 'VIS003', cliente: 'Luis Herrera', inmueble: 'Local Comercial Centro', fecha: '2026-04-13', hora: '15:00', asesor: 'Juan Tapiero', estado: 'confirmada', observaciones: 'Interesado en arrendar' },
    { id: 'VIS004', cliente: 'María López', inmueble: 'Oficina Sudamericana', fecha: '2026-04-10', hora: '10:00', asesor: 'Laura Quintero', estado: 'realizada', observaciones: 'Le gustó pero quiere otra opción' },
    { id: 'VIS005', cliente: 'Pedro Sánchez', inmueble: 'Bodega Zona Industrial', fecha: '2026-04-08', hora: '14:00', asesor: 'Andrés Ríos', estado: 'cancelada', observaciones: 'Cliente canceló sin aviso' },
    { id: 'VIS006', cliente: 'Carlos Martínez', inmueble: 'Lote Sector Norte', fecha: '2026-04-15', hora: '09:00', asesor: 'Juan Tapiero', estado: 'reprogramada', observaciones: 'Reprogramada para siguiente semana' },
  ];

  visitasFiltradas = [...this.visitas];

  filtrar() {
    this.visitasFiltradas = this.visitas.filter(v =>
      (!this.busqueda || v.cliente.toLowerCase().includes(this.busqueda.toLowerCase()) || v.inmueble.toLowerCase().includes(this.busqueda.toLowerCase())) &&
      (!this.filtroEstado || v.estado === this.filtroEstado)
    );
  }

  editar(v: any) { this.form = { ...v }; this.modoEdicion = true; this.mostrarForm = true; }

  cancelar(v: any) {
    if (confirm('¿Cancelar esta visita?')) {
      v.estado = 'cancelada';
      this.filtrar();
    }
  }

  guardar() {
    if (!this.modoEdicion) {
      this.form.id = 'VIS' + String(this.visitas.length + 1).padStart(3, '0');
      this.visitas.push({ ...this.form });
    } else {
      const idx = this.visitas.findIndex(x => x.id === this.form.id);
      if (idx >= 0) this.visitas[idx] = { ...this.form };
    }
    this.filtrar();
    this.cerrarForm();
  }

  cerrarForm() {
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.form = { id: '', cliente: '', inmueble: '', fecha: '', hora: '', asesor: '', estado: 'pendiente', observaciones: '' };
  }
}