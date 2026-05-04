import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-contratos',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './contratos.html',
  styleUrl: './contratos.css'
})
export class Contratos {
  mostrarForm = false;
  modoEdicion = false;
  busqueda = '';
  filtroEstado = '';
  filtroTipo = '';

  form: any = { id: '', cliente: '', inmueble: '', tipo: 'arriendo', valor: 0, fechaInicio: '', fechaFin: '', asesor: '', estado: 'activo' };

  contratos: any[] = [
    { id: 'CON001', cliente: 'Ana Gómez', inmueble: 'Casa La Castellana', tipo: 'arriendo', valor: 2300000, fechaInicio: '2026-01-01', fechaFin: '2026-12-31', asesor: 'Jose Bedoya', estado: 'activo' },
    { id: 'CON002', cliente: 'Luis Herrera', inmueble: 'Local Comercial Centro', tipo: 'arriendo', valor: 3500000, fechaInicio: '2025-05-01', fechaFin: '2026-04-18', asesor: 'Juan Tapiero', estado: 'por vencer' },
    { id: 'CON003', cliente: 'Carlos Martínez', inmueble: 'Apartamento Norte', tipo: 'venta', valor: 680000000, fechaInicio: '2026-03-15', fechaFin: '2026-03-15', asesor: 'Juan Tapiero', estado: 'activo' },
    { id: 'CON004', cliente: 'Pedro Sánchez', inmueble: 'Oficina Sudamericana', tipo: 'arriendo', valor: 2800000, fechaInicio: '2025-01-01', fechaFin: '2026-01-01', asesor: 'Laura Quintero', estado: 'vencido' },
    { id: 'CON005', cliente: 'María López', inmueble: 'Bodega Zona Industrial', tipo: 'arriendo', valor: 5500000, fechaInicio: '2026-02-01', fechaFin: '2027-01-31', asesor: 'Andrés Ríos', estado: 'activo' },
  ];

  contratosFiltrados = [...this.contratos];

  filtrar() {
    this.contratosFiltrados = this.contratos.filter(c =>
      (!this.busqueda || c.cliente.toLowerCase().includes(this.busqueda.toLowerCase()) || c.inmueble.toLowerCase().includes(this.busqueda.toLowerCase())) &&
      (!this.filtroEstado || c.estado === this.filtroEstado) &&
      (!this.filtroTipo || c.tipo === this.filtroTipo)
    );
  }

  editar(c: any) { this.form = { ...c }; this.modoEdicion = true; this.mostrarForm = true; }

  eliminar(c: any) {
    if (confirm('¿Eliminar este contrato?')) {
      this.contratos = this.contratos.filter(x => x.id !== c.id);
      this.filtrar();
    }
  }

  guardar() {
    if (!this.modoEdicion) {
      this.form.id = 'CON' + String(this.contratos.length + 1).padStart(3, '0');
      this.contratos.push({ ...this.form });
    } else {
      const idx = this.contratos.findIndex(x => x.id === this.form.id);
      if (idx >= 0) this.contratos[idx] = { ...this.form };
    }
    this.filtrar();
    this.cerrarForm();
  }

  cerrarForm() {
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.form = { id: '', cliente: '', inmueble: '', tipo: 'arriendo', valor: 0, fechaInicio: '', fechaFin: '', asesor: '', estado: 'activo' };
  }
}