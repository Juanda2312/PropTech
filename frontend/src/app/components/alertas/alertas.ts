import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-alertas',
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './alertas.html',
  styleUrl: './alertas.css'
})
export class Alertas implements OnInit {
  filtroNivel = '';
  filtroTipo = '';
  filtroEstado = '';
  cargando = false;

  alertas: any[] = [];
  alertasFiltradas: any[] = [];

  constructor(private api: ApiService) {}

  ngOnInit() { this.cargar(); }

  cargar() {
    this.cargando = true;
    this.api.getAlertas().subscribe({
      next: (data) => {
        this.alertas = data.map(a => ({
          id: a.idAlerta,
          titulo: a.tipoAlerta,
          descripcion: a.descripcion,
          tipo: a.tipoAlerta?.toLowerCase() || 'general',
          nivel: a.nivel?.toLowerCase() || 'bajo',
          fecha: a.fechaGeneracion,
          estado: a.cerrada ? 'atendida' : 'pendiente'
        }));
        this.filtrar();
        this.cargando = false;
      },
      error: () => { this.cargando = false; }
    });
  }

  generarAlertas() {
    this.api.generarAlertas().subscribe({
      next: () => this.cargar(),
      error: () => this.cargar()
    });
  }

  contarNivel(nivel: string) { return this.alertas.filter(a => a.nivel === nivel).length; }

  filtrar() {
    this.alertasFiltradas = this.alertas.filter(a =>
      (!this.filtroNivel || a.nivel === this.filtroNivel) &&
      (!this.filtroTipo || a.tipo === this.filtroTipo) &&
      (!this.filtroEstado || a.estado === this.filtroEstado)
    );
  }

  atender(a: any) {
    this.api.cerrarAlerta(a.id).subscribe({
      next: () => { a.estado = 'atendida'; this.filtrar(); },
      error: () => { a.estado = 'atendida'; this.filtrar(); }
    });
  }
}
