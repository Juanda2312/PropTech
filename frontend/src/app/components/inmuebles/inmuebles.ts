import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-inmuebles',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './inmuebles.html',
  styleUrl: './inmuebles.css'
})
export class Inmuebles {
  mostrarForm = false;
  modoEdicion = false;
  busqueda = '';
  filtroTipo = '';
  filtroFinalidad = '';
  filtroEstado = '';

  form: any = { codigo: '', nombre: '', tipo: 'Apartamento', finalidad: 'Venta', ciudad: '', direccion: '', zona: '', precio: 0, area: 0, habitaciones: 0, banos: 0, estado: 'disponible', asesor: '', disponibilidad: 'Disponible' };

  inmuebles: any[] = [
    { codigo: 'INM001', nombre: 'Apartamento Norte Armenia', tipo: 'Apartamento', finalidad: 'Venta', ciudad: 'Armenia', direccion: 'Cra. 14 # 32-55', zona: 'Norte', precio: 680000000, area: 85, habitaciones: 3, banos: 2, estado: 'disponible', asesor: 'Juan Tapiero', disponibilidad: 'Disponible' },
    { codigo: 'INM002', nombre: 'Casa La Castellana', tipo: 'Casa', finalidad: 'Arriendo', ciudad: 'Armenia', direccion: 'Cl. 20 # 15-30', zona: 'Norte', precio: 2300000, area: 120, habitaciones: 4, banos: 3, estado: 'reservado', asesor: 'Jose Bedoya', disponibilidad: 'No disponible' },
    { codigo: 'INM003', nombre: 'Local Comercial Centro', tipo: 'Local comercial', finalidad: 'Arriendo', ciudad: 'Armenia', direccion: 'Av. Bolívar # 10-20', zona: 'Centro', precio: 3500000, area: 45, habitaciones: 0, banos: 1, estado: 'disponible', asesor: 'Juan Tapiero', disponibilidad: 'Disponible' },
    { codigo: 'INM004', nombre: 'Oficina Edificio Sudamericana', tipo: 'Oficina', finalidad: 'Arriendo', ciudad: 'Armenia', direccion: 'Cl. 19 # 14-17', zona: 'Centro', precio: 2800000, area: 60, habitaciones: 0, banos: 1, estado: 'disponible', asesor: 'Jose Bedoya', disponibilidad: 'Disponible' },
    { codigo: 'INM005', nombre: 'Lote Sector Norte', tipo: 'Lote', finalidad: 'Venta', ciudad: 'Armenia', direccion: 'Vía Norte Km 3', zona: 'Norte', precio: 450000000, area: 300, habitaciones: 0, banos: 0, estado: 'vendido', asesor: 'Juan Tapiero', disponibilidad: 'No disponible' },
    { codigo: 'INM006', nombre: 'Bodega Zona Industrial', tipo: 'Bodega', finalidad: 'Arriendo', ciudad: 'Armenia', direccion: 'Zona Industrial Km 5', zona: 'Sur', precio: 5500000, area: 800, habitaciones: 0, banos: 2, estado: 'disponible', asesor: 'Jose Bedoya', disponibilidad: 'Disponible' },
    { codigo: 'INM007', nombre: 'Apartamento Centenario', tipo: 'Apartamento', finalidad: 'Arriendo', ciudad: 'Armenia', direccion: 'Cra. 19 # 20-40', zona: 'Centro', precio: 1800000, area: 65, habitaciones: 2, banos: 1, estado: 'disponible', asesor: 'Juan Tapiero', disponibilidad: 'Disponible' },
    { codigo: 'INM008', nombre: 'Casa Campestre Calarcá', tipo: 'Casa', finalidad: 'Venta', ciudad: 'Calarcá', direccion: 'Vía Calarcá Km 2', zona: 'Rural', precio: 850000000, area: 280, habitaciones: 5, banos: 3, estado: 'disponible', asesor: 'Jose Bedoya', disponibilidad: 'Disponible' },
  ];

  inmueblesFiltrados = [...this.inmuebles];

  filtrar() {
    this.inmueblesFiltrados = this.inmuebles.filter(i =>
      (!this.busqueda || i.nombre.toLowerCase().includes(this.busqueda.toLowerCase()) || i.codigo.includes(this.busqueda) || i.zona.toLowerCase().includes(this.busqueda.toLowerCase())) &&
      (!this.filtroTipo || i.tipo === this.filtroTipo) &&
      (!this.filtroFinalidad || i.finalidad === this.filtroFinalidad) &&
      (!this.filtroEstado || i.estado === this.filtroEstado)
    );
  }

  editar(i: any) { this.form = { ...i }; this.modoEdicion = true; this.mostrarForm = true; }

  eliminar(i: any) {
    if (confirm('¿Eliminar este inmueble?')) {
      this.inmuebles = this.inmuebles.filter(x => x.codigo !== i.codigo);
      this.filtrar();
    }
  }

  guardar() {
    if (this.modoEdicion) {
      const idx = this.inmuebles.findIndex(x => x.codigo === this.form.codigo);
      if (idx >= 0) this.inmuebles[idx] = { ...this.form };
    } else {
      this.inmuebles.push({ ...this.form });
    }
    this.filtrar();
    this.cerrarForm();
  }

  cerrarForm() {
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.form = { codigo: '', nombre: '', tipo: 'Apartamento', finalidad: 'Venta', ciudad: '', direccion: '', zona: '', precio: 0, area: 0, habitaciones: 0, banos: 0, estado: 'disponible', asesor: '', disponibilidad: 'Disponible' };
  }
}