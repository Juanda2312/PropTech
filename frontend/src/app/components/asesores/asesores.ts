import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-asesores',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './asesores.html',
  styleUrl: './asesores.css'
})
export class Asesores {
  mostrarForm = false;
  modoEdicion = false;
  busqueda = '';
  filtroZona = '';

  form: any = { id: '', nombre: '', iniciales: '', correo: '', telefono: '', especialidad: '', zona: 'Norte', inmueblesAsignados: 0, visitasAgendadas: 0, cierres: 0 };

  asesores: any[] = [
    { id: 'AS001', nombre: 'Juan David Tapiero', iniciales: 'JT', correo: 'juan@beta.com', telefono: '+57 315 401 5585', especialidad: 'Apartamentos y casas', zona: 'Norte', inmueblesAsignados: 12, visitasAgendadas: 24, cierres: 8 },
    { id: 'AS002', nombre: 'Jose Manuel Bedoya', iniciales: 'JB', correo: 'jose@beta.com', telefono: '+57 310 987 6543', especialidad: 'Locales y oficinas', zona: 'Centro', inmueblesAsignados: 8, visitasAgendadas: 15, cierres: 5 },
    { id: 'AS003', nombre: 'Laura Quintero', iniciales: 'LQ', correo: 'laura@beta.com', telefono: '+57 320 555 4321', especialidad: 'Fincas y lotes', zona: 'Rural', inmueblesAsignados: 6, visitasAgendadas: 10, cierres: 3 },
    { id: 'AS004', nombre: 'Andrés Ríos', iniciales: 'AR', correo: 'andres@beta.com', telefono: '+57 300 111 2233', especialidad: 'Bodegas y locales', zona: 'Sur', inmueblesAsignados: 9, visitasAgendadas: 18, cierres: 6 },
  ];

  asesoresFiltrados = [...this.asesores];

  filtrar() {
    this.asesoresFiltrados = this.asesores.filter(a =>
      (!this.busqueda || a.nombre.toLowerCase().includes(this.busqueda.toLowerCase()) || a.id.includes(this.busqueda)) &&
      (!this.filtroZona || a.zona === this.filtroZona)
    );
  }

  editar(a: any) { this.form = { ...a }; this.modoEdicion = true; this.mostrarForm = true; }

  eliminar(a: any) {
    if (confirm('¿Eliminar este asesor?')) {
      this.asesores = this.asesores.filter(x => x.id !== a.id);
      this.filtrar();
    }
  }

  guardar() {
    this.form.iniciales = this.form.nombre.split(' ').map((n: string) => n[0]).join('').substring(0, 2).toUpperCase();
    if (this.modoEdicion) {
      const idx = this.asesores.findIndex(x => x.id === this.form.id);
      if (idx >= 0) this.asesores[idx] = { ...this.form };
    } else {
      this.asesores.push({ ...this.form });
    }
    this.filtrar();
    this.cerrarForm();
  }

  cerrarForm() {
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.form = { id: '', nombre: '', iniciales: '', correo: '', telefono: '', especialidad: '', zona: 'Norte', inmueblesAsignados: 0, visitasAgendadas: 0, cierres: 0 };
  }
}