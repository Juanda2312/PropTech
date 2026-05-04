import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-alertas',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './alertas.html',
  styleUrl: './alertas.css'
})
export class Alertas {
  filtroNivel = '';
  filtroTipo = '';
  filtroEstado = '';

  alertas: any[] = [
    { titulo: 'Contrato próximo a vencer', descripcion: 'El contrato CON002 de Ana Gómez vence en 5 días. Inmueble: Local Comercial Centro.', tipo: 'contrato', nivel: 'alto', fecha: '13/04/2026', estado: 'pendiente' },
    { titulo: 'Inmueble sin visitas hace 45 días', descripcion: 'La Bodega Zona Industrial no ha recibido visitas en 45 días.', tipo: 'inmueble', nivel: 'medio', fecha: '12/04/2026', estado: 'pendiente' },
    { titulo: 'Propiedad con alta demanda', descripcion: 'El Apartamento Norte Armenia ha recibido 12 consultas esta semana.', tipo: 'inmueble', nivel: 'bajo', fecha: '11/04/2026', estado: 'pendiente' },
    { titulo: 'Visitas pendientes por confirmar', descripcion: 'Hay 3 visitas en estado pendiente que requieren confirmación del asesor.', tipo: 'visita', nivel: 'medio', fecha: '11/04/2026', estado: 'pendiente' },
    { titulo: 'Inmueble reservado sin cierre', descripcion: 'Casa La Castellana lleva 30 días reservada sin cierre de negocio.', tipo: 'inmueble', nivel: 'alto', fecha: '10/04/2026', estado: 'pendiente' },
    { titulo: 'Cliente sin seguimiento reciente', descripcion: 'Pedro Sánchez no ha sido contactado en más de 20 días.', tipo: 'cliente', nivel: 'bajo', fecha: '09/04/2026', estado: 'atendida' },
  ];

  alertasFiltradas = [...this.alertas];

  contarNivel(nivel: string) { return this.alertas.filter(a => a.nivel === nivel).length; }

  filtrar() {
    this.alertasFiltradas = this.alertas.filter(a =>
      (!this.filtroNivel || a.nivel === this.filtroNivel) &&
      (!this.filtroTipo || a.tipo === this.filtroTipo) &&
      (!this.filtroEstado || a.estado === this.filtroEstado)
    );
  }

  atender(a: any) { a.estado = 'atendida'; this.filtrar(); }
}