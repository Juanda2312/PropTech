// src/app/app.component.ts
import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { VisitaService } from './core/services/visita.service';
import { AlertaService } from './core/services/alerta.service';
import { ToastService, Toast } from './core/services/toast.service';
import { AuthService } from './core/services/auth.service';
import { filter } from 'rxjs/operators';
import { ChatbotComponent } from './shared/chatbot/chatbot.component';
import { ContextoChatbot } from './core/services/chatbot.service';
import { ClienteService } from './core/services/cliente.service';
import { InmuebleService } from './core/services/inmueble.service';
import { AsesorService } from './core/services/asesor.service';
import { PlataformaService } from './core/services/plataforma.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, ChatbotComponent],


  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  sidebarCollapsed = false;
  pendientesCount = 0;
  alertasCount = 0;
  toasts: Toast[] = [];
  mostrarShell = false;
  contextoChat: ContextoChatbot | null = null;


  // Rutas EXACTAS que no muestran el sidebar admin
  // Usar coincidencia exacta para evitar que /clientes haga match con /cliente
  private readonly rutasSinShell = ['/login', '/cliente'];

  constructor(
      private visitaService: VisitaService,
      private alertaService: AlertaService,
      private toastService: ToastService,
      private authService: AuthService,
      private clienteService: ClienteService,
      private inmuebleService: InmuebleService,
      private asesorService: AsesorService,
      private plataformaService: PlataformaService,
      private router: Router
  ) {}

  ngOnInit() {
    this.router.events.pipe(
        filter(e => e instanceof NavigationEnd)
    ).subscribe((e: any) => {
      const ruta: string = e.urlAfterRedirects || e.url || '';
      // Comparación exacta o con slash final para evitar falsos positivos:
      // /cliente   → sin shell  ✓
      // /clientes  → CON shell  ✓  (no confundir con /cliente)
      this.mostrarShell = !this.rutasSinShell.some(r =>
          ruta === r || ruta.startsWith(r + '/')  || ruta.startsWith(r + '?')
      );
    });

    this.cargarContadores();
    this.toastService.toasts$.subscribe(t => this.toasts = t);
    setInterval(() => this.cargarContadores(), 30000);
  }

  cargarContadores() {
    this.visitaService.totalPendientes().subscribe({ next: r => this.pendientesCount = r.totalPendientes, error: () => {} });
    this.alertaService.totalPendientes().subscribe({ next: r => this.alertasCount = r.totalPendientes, error: () => {} });
    this.construirContextoAdmin();
  }

  toggleSidebar() { this.sidebarCollapsed = !this.sidebarCollapsed; }

  cerrarSesion() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  construirContextoAdmin() {
    forkJoin({
      inmuebles: this.inmuebleService.listar(),
      clientes: this.clienteService.listar(),
      asesores: this.asesorService.listar(),
      visitasPend: this.visitaService.totalPendientes(),
      visitasPendLista: this.visitaService.listar({ estado: 'PENDIENTE' }), // ← agregar
      alertas: this.alertaService.listar({ abiertas: true }),
      rankingAsesores: this.plataformaService.rankingAsesores(),
      rankingZonas: this.plataformaService.rankingZonas()
    }).subscribe({
      next: data => {
        const inmueblesDisponibles = data.inmuebles.filter(i => i.disponibilidad).length;
        const inmueblesNoDisponibles = data.inmuebles.filter(i => !i.disponibilidad).length;

        this.contextoChat = {
          rol: 'ADMIN',
          stats: {
            inmuebles: data.inmuebles.length,
            clientes: data.clientes.length,
            asesores: data.asesores.length,
            visitasPendientes: data.visitasPendLista.length,
            alertasAbiertas: data.alertas.length,
            inmueblesDisponibles: data.inmuebles.filter(i => i.disponibilidad).length,
            inmueblesNoDisponibles: data.inmuebles.filter(i => !i.disponibilidad).length
          },
          alertasCriticas: data.alertas
              .sort((a, b) => {
                const orden: Record<string, number> = { CRITICO: 0, ALTO: 1, MEDIO: 2, BAJO: 3 };
                return (orden[a.nivel] ?? 4) - (orden[b.nivel] ?? 4);
              })
              .slice(0, 20)
              .map(a => ({ tipoAlerta: a.tipoAlerta, descripcion: a.descripcion, nivel: a.nivel })),
          rankingAsesores: data.rankingAsesores.slice(0, 5).map(a => ({
            nombre: a.nombre,
            especialidadZona: a.especialidadZona
          })),
          rankingZonas: Object.entries(data.rankingZonas)
              .map(([zona, visitas]) => ({ zona, visitas }))
              .sort((a, b) => b.visitas - a.visitas)
              .slice(0, 5),
          visitasPendientes: data.visitasPendLista.slice(0, 10).map(v => ({
            idVisita: v.idVisita,
            cliente: { nombre: v.cliente?.nombre },
            inmueble: { direccion: v.inmueble?.direccion },
            fecha: v.fecha
          })),
          inmueblesRecientes: data.inmuebles.slice(0, 8).map(i => ({
            codigo: i.codigo,
            direccion: i.direccion,
            ciudad: i.ciudad,
            precio: i.precio,
            disponibilidad: i.disponibilidad
          })),
          // Campos extra para el prompt — los pasamos en inmueblesRecientes no alcanza
          // así que los añadimos al stats como campos adicionales usando cast
          ...({
            inmueblesDisponibles,
            inmueblesNoDisponibles
          } as any)
        };
      },
      error: () => {}
    });
  }
}