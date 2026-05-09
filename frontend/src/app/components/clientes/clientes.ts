import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-clientes',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './clientes.html',
  styleUrl: './clientes.css'
})
export class Clientes implements OnInit {
  mostrarForm = false;
  modoEdicion = false;
  busqueda = '';
  filtroEstado = '';
  filtroTipo = '';
  cargando = false;

  form: any = { id: '', nombre: '', correo: '', telefono: '', tipoCliente: 'comprador', presupuesto: 0, zonaInteres: '', tipoDeseado: 'Apartamento', habitacionesMin: 1, estado: 'activo' };

  clientes: any[] = [];
  clientesFiltrados: any[] = [];

  constructor(private api: ApiService) {}

  ngOnInit() { this.cargar(); }

  cargar() {
    this.cargando = true;
    this.api.getClientes().subscribe({
      next: (data) => {
        this.clientes = data.map(c => ({
          id: c.id,
          nombre: c.nombre,
          correo: c.correo || '',
          telefono: c.telefono || '',
          tipo: c.tipoCliente || 'comprador',
          presupuesto: c.presupuesto || 0,
          zonaInteres: c.zonasInteres?.[0] || '',
          tipoDeseado: c.tipoInmuebleDeseado || 'Apartamento',
          habitacionesMin: c.habitacionesMinimas || 1,
          estado: c.estadoBusqueda?.toLowerCase() || 'activo'
        }));
        this.filtrar();
        this.cargando = false;
      },
      error: () => { this.cargando = false; }
    });
  }

  filtrar() {
    this.clientesFiltrados = this.clientes.filter(c =>
      (!this.busqueda || c.nombre?.toLowerCase().includes(this.busqueda.toLowerCase()) || c.id?.includes(this.busqueda)) &&
      (!this.filtroEstado || c.estado === this.filtroEstado) &&
      (!this.filtroTipo || c.tipo === this.filtroTipo)
    );
  }

  editar(c: any) { this.form = { ...c }; this.modoEdicion = true; this.mostrarForm = true; }

  eliminar(c: any) {
    if (!confirm('¿Eliminar este cliente?')) return;
    this.api.eliminarCliente(c.id).subscribe({
      next: () => this.cargar(),
      error: () => { this.clientes = this.clientes.filter(x => x.id !== c.id); this.filtrar(); }
    });
  }

  guardar() {
    const payload = {
      id: this.form.id,
      nombre: this.form.nombre,
      correo: this.form.correo,
      telefono: this.form.telefono,
      tipoCliente: this.form.tipo,
      presupuesto: this.form.presupuesto,
      zonasInteres: [this.form.zonaInteres?.toUpperCase() || 'NORTE'],
      tipoInmuebleDeseado: (this.form.tipoDeseado || 'APARTAMENTO').toUpperCase().replace(' ', '_'),
      habitacionesMinimas: this.form.habitacionesMin,
      estadoBusqueda: (this.form.estado || 'ACTIVO').toUpperCase()
    };

    if (this.modoEdicion) {
      this.api.actualizarCliente(this.form.id, payload).subscribe({
        next: () => { this.cargar(); this.cerrarForm(); },
        error: () => { this.cerrarForm(); }
      });
    } else {
      this.api.crearCliente(payload).subscribe({
        next: () => { this.cargar(); this.cerrarForm(); },
        error: () => { this.cerrarForm(); }
      });
    }
  }

  cerrarForm() {
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.form = { id: '', nombre: '', correo: '', telefono: '', tipoCliente: 'comprador', presupuesto: 0, zonaInteres: '', tipoDeseado: 'Apartamento', habitacionesMin: 1, estado: 'activo' };
  }
}
