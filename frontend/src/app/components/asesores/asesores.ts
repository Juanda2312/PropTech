import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-asesores',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './asesores.html',
  styleUrl: './asesores.css'
})
export class Asesores implements OnInit {
  mostrarForm = false;
  modoEdicion = false;
  busqueda = '';
  filtroZona = '';
  cargando = false;

  form: any = { id: '', nombre: '', iniciales: '', correo: '', telefono: '', especialidad: '', zona: 'Norte', inmueblesAsignados: 0, visitasAgendadas: 0, cierres: 0 };

  asesores: any[] = [];
  asesoresFiltrados: any[] = [];

  constructor(private api: ApiService) {}

  ngOnInit() { this.cargar(); }

  cargar() {
    this.cargando = true;
    this.api.getAsesores().subscribe({
      next: (data) => {
        this.asesores = data.map(a => ({
          id: a.id,
          nombre: a.nombre,
          iniciales: a.nombre?.split(' ').map((n: string) => n[0]).join('').substring(0, 2).toUpperCase() || 'XX',
          correo: `${a.nombre?.split(' ')[0]?.toLowerCase() || 'asesor'}@beta.com`,
          telefono: a.contacto || '',
          especialidad: a.especialidadZona || '',
          zona: a.especialidadZona || 'Norte',
          inmueblesAsignados: a.inmueblesAsignados?.size || 0,
          visitasAgendadas: a.visitasAgendadas?.size || 0,
          cierres: a.cierresRealizados?.size || 0
        }));
        this.filtrar();
        this.cargando = false;
      },
      error: () => { this.cargando = false; }
    });
  }

  filtrar() {
    this.asesoresFiltrados = this.asesores.filter(a =>
      (!this.busqueda || a.nombre?.toLowerCase().includes(this.busqueda.toLowerCase()) || a.id?.includes(this.busqueda)) &&
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
    const payload = {
      id: this.form.id,
      nombre: this.form.nombre,
      contacto: this.form.telefono,
      especialidadZona: this.form.especialidad || this.form.zona
    };

    if (this.modoEdicion) {
      this.api.actualizarAsesor(this.form.id, payload).subscribe({
        next: () => { this.cargar(); this.cerrarForm(); },
        error: () => { this.cerrarForm(); }
      });
    } else {
      this.api.crearAsesor(payload).subscribe({
        next: () => { this.cargar(); this.cerrarForm(); },
        error: () => { this.cerrarForm(); }
      });
    }
  }

  cerrarForm() {
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.form = { id: '', nombre: '', iniciales: '', correo: '', telefono: '', especialidad: '', zona: 'Norte', inmueblesAsignados: 0, visitasAgendadas: 0, cierres: 0 };
  }
}
