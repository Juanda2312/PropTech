import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../../sidebar/sidebar';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-comportamiento',
  imports: [CommonModule, SidebarComponent],
  templateUrl: './comportamiento.html',
  styleUrl: './comportamiento.css'
})
export class Comportamiento implements OnInit {
  comportamientos: any[] = [];
  historial: any[] = [];
  cargando = false;

  constructor(private api: ApiService) {}

  ngOnInit() { this.cargar(); }

  cargar() {
    this.cargando = true;
    this.api.getEventos({ activos: true }).subscribe({
      next: (data) => {
        this.comportamientos = data.map(e => ({
          titulo: e.tipoEvento,
          descripcion: e.descripcion,
          entidad: e.idEvento,
          nivel: e.nivelAtencion?.toLowerCase() || 'bajo',
          fecha: e.fechaDeteccion,
          revisado: e.estadoEvento === 'CERRADO'
        }));
        this.cargando = false;
      },
      error: () => { this.cargando = false; }
    });

    this.api.getEventos().subscribe({
      next: (data) => {
        this.historial = data.map(e => ({
          fecha: e.fechaDeteccion,
          tipo: e.tipoEvento,
          entidad: e.idEvento,
          descripcion: e.descripcion,
          nivel: e.nivelAtencion?.toLowerCase() || 'bajo',
          estado: e.estadoEvento === 'CERRADO' ? 'atendido' : 'pendiente'
        }));
      },
      error: () => {}
    });
  }

  detectar() {
    this.api.detectarComportamientos().subscribe({
      next: () => this.cargar(),
      error: () => this.cargar()
    });
  }

  marcarRevisado(a: any) { a.revisado = !a.revisado; }
}
