// src/app/features/eventos/eventos.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EventoService } from '../../core/services/evento.service';
import { ToastService } from '../../core/services/toast.service';
import { EventoInusual, EventoInusualDTO, NivelAtencion } from '../../core/models';

@Component({
  selector: 'app-eventos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './eventos.component.html',
  styleUrls: ['./eventos.component.scss']
})
export class EventosComponent implements OnInit {
  eventos: EventoInusual[] = [];
  loading = true;
  detectando = false;
  showModal = false;
  submitting = false;
  filtroNivel: NivelAtencion | '' = '';
  filtroActivos = false;
  niveles: NivelAtencion[] = ['BAJO', 'MEDIO', 'ALTO', 'CRITICO'];

  form: EventoInusualDTO = this.emptyForm();

  constructor(private eventoService: EventoService, private toast: ToastService) {}

  ngOnInit() { this.cargarEventos(); }

  cargarEventos() {
    this.loading = true;
    this.eventoService.listar({
      nivel: this.filtroNivel as NivelAtencion || undefined,
      activos: this.filtroActivos || undefined
    }).subscribe({
      next: d => { this.eventos = d; this.loading = false; },
      error: (e: any) => { this.toast.error(e.message); this.loading = false; }
    });
  }

  detectar() {
    this.detectando = true;
    this.eventoService.detectarComportamientos().subscribe({
      next: () => { this.toast.success('Detección completada'); this.detectando = false; this.cargarEventos(); },
      error: (e: any) => { this.toast.error(e.message); this.detectando = false; }
    });
  }

  abrirCrear() { this.form = this.emptyForm(); this.showModal = true; }

  guardar() {
    if (!this.form.idEvento || !this.form.tipoEvento) { this.toast.warn('ID y tipo son requeridos'); return; }
    this.submitting = true;
    this.eventoService.registrar(this.form).subscribe({
      next: () => { this.toast.success('Evento registrado'); this.showModal = false; this.submitting = false; this.cargarEventos(); },
      error: (e: any) => { this.toast.error(e.message); this.submitting = false; }
    });
  }

  cerrar(ev: EventoInusual) {
    this.eventoService.cerrar(ev.idEvento).subscribe({
      next: () => { this.toast.success('Evento cerrado'); this.cargarEventos(); },
      error: (e: any) => this.toast.error(e.message)
    });
  }

  nivelClass(n: string) { return { BAJO:'badge-green', MEDIO:'badge-amber', ALTO:'badge-orange', CRITICO:'badge-red' }[n] || 'badge-gray'; }
  estadoClass(e: string) { return { ACTIVO:'badge-blue', EN_PAUSA:'badge-amber', CERRADO:'badge-gray' }[e] || 'badge-gray'; }
  nivelIcon(n: string) { return { BAJO:'🟢', MEDIO:'🟡', ALTO:'🟠', CRITICO:'🔴' }[n] || '⚪'; }

  private emptyForm(): EventoInusualDTO { return { idEvento:'', tipoEvento:'', descripcion:'', nivelAtencion:'MEDIO' }; }
}
