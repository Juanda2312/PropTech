// src/app/features/grafo/grafo.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PlataformaService } from '../../core/services/plataforma.service';
import { ClienteService } from '../../core/services/cliente.service';
import { InmuebleService } from '../../core/services/inmueble.service';
import { ToastService } from '../../core/services/toast.service';
import { Cliente, Inmueble } from '../../core/models';

type QueryMode = 'clientes-de-inmueble' | 'inmuebles-de-cliente' | 'bfs';

@Component({
  selector: 'app-grafo',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './grafo.component.html',
  styleUrls: ['./grafo.component.scss']
})
export class GrafoComponent implements OnInit {
  clientes: Cliente[] = [];
  inmuebles: Inmueble[] = [];
  modoQuery: QueryMode = 'clientes-de-inmueble';
  selectedId = '';
  resultados: string[] = [];
  loading = false;
  rankingZonas: { zona: string; visitas: number }[] = [];
  loadingZonas = false;

  modos: { value: QueryMode; label: string; icon: string }[] = [
    { value: 'clientes-de-inmueble', label: 'Clientes → Inmueble', icon: '👥' },
    { value: 'inmuebles-de-cliente', label: 'Inmuebles → Cliente', icon: '🏠' },
    { value: 'bfs', label: 'BFS desde nodo', icon: '🕸️' }
  ];

  constructor(
    private plataformaService: PlataformaService,
    private clienteService: ClienteService,
    private inmuebleService: InmuebleService,
    private toast: ToastService
  ) {}

  ngOnInit() {
    this.clienteService.listar().subscribe({ next: d => this.clientes = d, error: () => {} });
    this.inmuebleService.listar().subscribe({ next: d => this.inmuebles = d, error: () => {} });
    this.cargarRankingZonas();
  }

  consultar() {
    if (!this.selectedId) { this.toast.warn('Ingresa un ID o código'); return; }
    this.loading = true;
    this.resultados = [];

    let obs$;
    switch (this.modoQuery) {
      case 'clientes-de-inmueble': obs$ = this.plataformaService.clientesConectadosAInmueble(this.selectedId); break;
      case 'inmuebles-de-cliente': obs$ = this.plataformaService.inmueblesConectadosACliente(this.selectedId); break;
      case 'bfs':                  obs$ = this.plataformaService.bfsDesdeNodo(this.selectedId); break;
    }

    obs$.subscribe({
      next: d => { this.resultados = d; this.loading = false; },
      error: (e: any) => { this.toast.error(e.message); this.loading = false; }
    });
  }

  cargarRankingZonas() {
    this.loadingZonas = true;
    this.plataformaService.rankingZonas().subscribe({
      next: data => {
        this.rankingZonas = Object.entries(data)
          .map(([zona, visitas]) => ({ zona, visitas }))
          .sort((a, b) => b.visitas - a.visitas);
        this.loadingZonas = false;
      },
      error: () => this.loadingZonas = false
    });
  }

  get modoActual() { return this.modos.find(m => m.value === this.modoQuery); }

  getResultadoIcon(id: string) {
    if (this.clientes.find(c => c.id === id)) return '👤';
    if (this.inmuebles.find(i => i.codigo === id)) return '🏠';
    return '⬡';
  }

  getResultadoLabel(id: string) {
    const c = this.clientes.find(c => c.id === id);
    if (c) return c.nombre;
    const i = this.inmuebles.find(i => i.codigo === id);
    if (i) return i.direccion;
    return id;
  }

  getResultadoTipo(id: string) {
    if (this.clientes.find(c => c.id === id)) return 'Cliente';
    if (this.inmuebles.find(i => i.codigo === id)) return 'Inmueble';
    return 'Nodo';
  }
}
