import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-solicitudes',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './solicitudes.html',
  styleUrl: './solicitudes.css'
})
export class Solicitudes {
  mostrarForm = false;
  busqueda = '';
  filtroEstado = '';
  filtroPrioridad = '';

  form: any = { cliente: '', tipo: 'información de inmueble', descripcion: '', prioridad: 'media', asesor: '', estado: 'pendiente', fecha: '' };

  solicitudes: any[] = [
    { id: 'SOL001', cliente: 'Carlos Martínez', tipo: 'agendar visita', descripcion: 'Quiere visitar el Apartamento Norte', fecha: '13/04/2026', prioridad: 'alta', estado: 'pendiente', asesor: 'Juan Tapiero' },
    { id: 'SOL002', cliente: 'Ana Gómez', tipo: 'información de inmueble', descripcion: 'Solicita más fotos de Casa La Castellana', fecha: '12/04/2026', prioridad: 'media', estado: 'en atención', asesor: 'Jose Bedoya' },
    { id: 'SOL003', cliente: 'Luis Herrera', tipo: 'negociación', descripcion: 'Interesado en negociar precio del Local Centro', fecha: '11/04/2026', prioridad: 'alta', estado: 'en atención', asesor: 'Juan Tapiero' },
    { id: 'SOL004', cliente: 'María López', tipo: 'queja o reclamo', descripcion: 'Inconformidad con el tiempo de respuesta', fecha: '10/04/2026', prioridad: 'alta', estado: 'resuelta', asesor: 'Laura Quintero' },
    { id: 'SOL005', cliente: 'Pedro Sánchez', tipo: 'otro', descripcion: 'Consulta sobre documentos para arriendo', fecha: '09/04/2026', prioridad: 'baja', estado: 'cancelada', asesor: 'Andrés Ríos' },
  ];

  solicitudesFiltradas = [...this.solicitudes];

  filtrar() {
    this.solicitudesFiltradas = this.solicitudes.filter(s =>
      (!this.busqueda || s.cliente.toLowerCase().includes(this.busqueda.toLowerCase())) &&
      (!this.filtroEstado || s.estado === this.filtroEstado) &&
      (!this.filtroPrioridad || s.prioridad === this.filtroPrioridad)
    );
  }

  atender(s: any) { s.estado = 'en atención'; this.filtrar(); }

  eliminar(s: any) {
    if (confirm('¿Eliminar esta solicitud?')) {
      this.solicitudes = this.solicitudes.filter(x => x.id !== s.id);
      this.filtrar();
    }
  }

  guardar() {
    this.form.id = 'SOL' + String(this.solicitudes.length + 1).padStart(3, '0');
    this.form.fecha = new Date().toLocaleDateString('es-CO');
    this.solicitudes.push({ ...this.form });
    this.filtrar();
    this.cerrarForm();
  }

  cerrarForm() {
    this.mostrarForm = false;
    this.form = { cliente: '', tipo: 'información de inmueble', descripcion: '', prioridad: 'media', asesor: '', estado: 'pendiente', fecha: '' };
  }
}