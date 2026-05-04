import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-operaciones',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './operaciones.html',
  styleUrl: './operaciones.css'
})
export class Operaciones {
  mostrarForm = false;
  modoEdicion = false;
  busqueda = '';
  filtroTipo = '';
  filtroEstado = '';

  form: any = { id: '', inmueble: '', cliente: '', asesor: '', fecha: '', tipo: 'arriendo', valor: 0, comision: 0, estado: 'en proceso' };

  operaciones: any[] = [
    { id: 'OP001', inmueble: 'Apartamento Norte', cliente: 'Carlos Martínez', asesor: 'Juan Tapiero', fecha: '2026-03-15', tipo: 'venta', valor: 680000000, comision: 20400000, estado: 'completado' },
    { id: 'OP002', inmueble: 'Casa La Castellana', cliente: 'Ana Gómez', asesor: 'Jose Bedoya', fecha: '2026-01-01', tipo: 'arriendo', valor: 2300000, comision: 69000, estado: 'completado' },
    { id: 'OP003', inmueble: 'Local Comercial Centro', cliente: 'Luis Herrera', asesor: 'Juan Tapiero', fecha: '2026-04-10', tipo: 'arriendo', valor: 3500000, comision: 105000, estado: 'en proceso' },
    { id: 'OP004', inmueble: 'Oficina Sudamericana', cliente: 'Pedro Sánchez', asesor: 'Laura Quintero', fecha: '2025-01-01', tipo: 'renovación de contrato', valor: 2800000, comision: 84000, estado: 'cancelado' },
    { id: 'OP005', inmueble: 'Bodega Zona Industrial', cliente: 'María López', asesor: 'Andrés Ríos', fecha: '2026-02-01', tipo: 'arriendo', valor: 5500000, comision: 165000, estado: 'completado' },
  ];

  operacionesFiltradas = [...this.operaciones];

  contar(tipo: string) { return this.operaciones.filter(o => o.tipo === tipo).length; }

  filtrar() {
    this.operacionesFiltradas = this.operaciones.filter(o =>
      (!this.busqueda || o.cliente.toLowerCase().includes(this.busqueda.toLowerCase()) || o.inmueble.toLowerCase().includes(this.busqueda.toLowerCase())) &&
      (!this.filtroTipo || o.tipo === this.filtroTipo) &&
      (!this.filtroEstado || o.estado === this.filtroEstado)
    );
  }

  calcularComision() { this.form.comision = Math.round(this.form.valor * 0.03); }

  editar(o: any) { this.form = { ...o }; this.modoEdicion = true; this.mostrarForm = true; }

  eliminar(o: any) {
    if (confirm('¿Eliminar esta operación?')) {
      this.operaciones = this.operaciones.filter(x => x.id !== o.id);
      this.filtrar();
    }
  }

  guardar() {
    if (!this.modoEdicion) {
      this.form.id = 'OP' + String(this.operaciones.length + 1).padStart(3, '0');
      this.operaciones.push({ ...this.form });
    } else {
      const idx = this.operaciones.findIndex(x => x.id === this.form.id);
      if (idx >= 0) this.operaciones[idx] = { ...this.form };
    }
    this.filtrar();
    this.cerrarForm();
  }

  cerrarForm() {
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.form = { id: '', inmueble: '', cliente: '', asesor: '', fecha: '', tipo: 'arriendo', valor: 0, comision: 0, estado: 'en proceso' };
  }
}