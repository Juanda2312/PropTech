import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-clientes',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './clientes.html',
  styleUrl: './clientes.css'
})
export class Clientes {
  mostrarForm = false;
  modoEdicion = false;
  busqueda = '';
  filtroEstado = '';
  filtroTipo = '';

  form: any = { id: '', nombre: '', correo: '', telefono: '', tipo: 'comprador', presupuesto: 0, zonaInteres: '', tipoDeseado: 'Apartamento', habitacionesMin: 1, estado: 'activo' };

  clientes: any[] = [
    { id: '1001', nombre: 'Carlos Martínez', correo: 'carlos@email.com', telefono: '3001234567', tipo: 'comprador', presupuesto: 700000000, zonaInteres: 'Norte', tipoDeseado: 'Apartamento', habitacionesMin: 3, estado: 'activo' },
    { id: '1002', nombre: 'Ana Gómez', correo: 'ana@email.com', telefono: '3109876543', tipo: 'arrendatario', presupuesto: 3000000, zonaInteres: 'Centro', tipoDeseado: 'Apartamento', habitacionesMin: 2, estado: 'activo' },
    { id: '1003', nombre: 'Luis Herrera', correo: 'luis@email.com', telefono: '3205556789', tipo: 'inversor', presupuesto: 2000000000, zonaInteres: 'Sur', tipoDeseado: 'Local comercial', habitacionesMin: 0, estado: 'activo' },
    { id: '1004', nombre: 'María López', correo: 'maria@email.com', telefono: '3154015585', tipo: 'comprador', presupuesto: 500000000, zonaInteres: 'Norte', tipoDeseado: 'Casa', habitacionesMin: 4, estado: 'inactivo' },
    { id: '1005', nombre: 'Pedro Sánchez', correo: 'pedro@email.com', telefono: '3007778899', tipo: 'arrendatario', presupuesto: 2500000, zonaInteres: 'Norte', tipoDeseado: 'Apartamento', habitacionesMin: 2, estado: 'cerrado' },
  ];

  clientesFiltrados = [...this.clientes];

  filtrar() {
    this.clientesFiltrados = this.clientes.filter(c =>
      (!this.busqueda || c.nombre.toLowerCase().includes(this.busqueda.toLowerCase()) || c.id.includes(this.busqueda)) &&
      (!this.filtroEstado || c.estado === this.filtroEstado) &&
      (!this.filtroTipo || c.tipo === this.filtroTipo)
    );
  }

  editar(c: any) { this.form = { ...c }; this.modoEdicion = true; this.mostrarForm = true; }

  eliminar(c: any) {
    if (confirm('¿Eliminar este cliente?')) {
      this.clientes = this.clientes.filter(x => x.id !== c.id);
      this.filtrar();
    }
  }

  guardar() {
    if (this.modoEdicion) {
      const idx = this.clientes.findIndex(x => x.id === this.form.id);
      if (idx >= 0) this.clientes[idx] = { ...this.form };
    } else {
      this.clientes.push({ ...this.form });
    }
    this.filtrar();
    this.cerrarForm();
  }

  cerrarForm() {
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.form = { id: '', nombre: '', correo: '', telefono: '', tipo: 'comprador', presupuesto: 0, zonaInteres: '', tipoDeseado: 'Apartamento', habitacionesMin: 1, estado: 'activo' };
  }
}