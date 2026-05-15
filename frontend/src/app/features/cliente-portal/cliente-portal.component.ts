// src/app/features/cliente-portal/cliente-portal.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { InmuebleService } from '../../core/services/inmueble.service';
import { ClienteService } from '../../core/services/cliente.service';
import { VisitaService } from '../../core/services/visita.service';
import { ToastService } from '../../core/services/toast.service';
import { Inmueble, Visita } from '../../core/models';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-cliente-portal',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './cliente-portal.component.html',
    styleUrls: ['./cliente-portal.component.scss']
})
export class ClientePortalComponent implements OnInit {
    usuario = this.authService.usuario;
    inmuebles: Inmueble[] = [];
    favoritos: Inmueble[] = [];
    historial: Inmueble[] = [];
    visitasCliente: Visita[] = [];
    loading = true;
    seccion: 'inmuebles' | 'visitas' | 'favoritos' | 'historial' = 'inmuebles';
    filtroTexto = '';
    clienteBackendId = '';
    vinculado = false;

    constructor(
        private authService: AuthService,
        private inmuebleService: InmuebleService,
        private clienteService: ClienteService,
        private visitaService: VisitaService,
        private toast: ToastService,
        private router: Router
    ) {}

    ngOnInit() {
        this.resolverClienteBackend();
    }

    resolverClienteBackend() {
        // Primero intentar con el backendId guardado en sesión
        const backendIdSesion = this.usuario?.backendId;
        if (backendIdSesion) {
            this.clienteService.buscarPorId(backendIdSesion).subscribe({
                next: () => {
                    this.clienteBackendId = backendIdSesion;
                    this.vinculado = true;
                    this.cargarInmuebles();
                    this.cargarHistorialYFavoritos();
                },
                error: () => this.buscarPorCorreo()
            });
        } else {
            this.buscarPorCorreo();
        }
    }

    buscarPorCorreo() {
        // Buscar en el backend por correo
        this.clienteService.listar().subscribe({
            next: clientes => {
                const encontrado = clientes.find(
                    c => c.correo?.toLowerCase() === this.usuario?.correo?.toLowerCase()
                );
                if (encontrado) {
                    this.clienteBackendId = encontrado.id;
                    this.vinculado = true;
                    // Actualizar sesión con backendId
                    const usuarioActualizado = { ...this.usuario!, backendId: encontrado.id };
                    sessionStorage.setItem('proptech_usuario', JSON.stringify(usuarioActualizado));
                    this.cargarHistorialYFavoritos();
                } else {
                    this.vinculado = false;
                }
                this.cargarInmuebles();
            },
            error: () => this.cargarInmuebles()
        });
    }

    cargarInmuebles() {
        this.inmuebleService.listar({ disponible: true }).subscribe({
            next: d => { this.inmuebles = d; this.loading = false; },
            error: () => this.loading = false
        });
    }

    cargarHistorialYFavoritos() {
        if (!this.clienteBackendId) return;
        this.clienteService.obtenerFavoritos(this.clienteBackendId).subscribe({
            next: d => this.favoritos = d, error: () => {}
        });
        this.clienteService.obtenerHistorial(this.clienteBackendId).subscribe({
            next: d => this.historial = d, error: () => {}
        });
        this.visitaService.listar({ idCliente: this.clienteBackendId }).subscribe({
            next: d => this.visitasCliente = d, error: () => {}
        });
    }

    marcarFavorito(inm: Inmueble) {
        if (!this.clienteBackendId) {
            this.toast.warn('Tu cuenta aún no está registrada en el sistema. Contacta a un asesor.');
            return;
        }
        this.clienteService.marcarFavorito(this.clienteBackendId, inm.codigo).subscribe({
            next: () => {
                this.toast.success('¡Agregado a favoritos!');
                this.cargarHistorialYFavoritos();
            },
            error: (e: any) => this.toast.error(e.message || 'Error al agregar favorito')
        });
    }

    registrarConsulta(inm: Inmueble) {
        if (!this.clienteBackendId) return;
        this.clienteService.buscarPorId(this.clienteBackendId).subscribe({
            next: () => {
                // registrar en historial usando el endpoint de consulta
                this.cargarHistorialYFavoritos();
            }
        });
    }

    get inmueblesFiltrados(): Inmueble[] {
        if (!this.filtroTexto.trim()) return this.inmuebles;
        const t = this.filtroTexto.toLowerCase();
        return this.inmuebles.filter(i =>
            i.ciudad?.toLowerCase().includes(t) ||
            i.barrio?.toLowerCase().includes(t) ||
            i.tipoInmueble?.toLowerCase().includes(t) ||
            i.direccion?.toLowerCase().includes(t)
        );
    }

    estadoBadge(e: string) {
        return { PENDIENTE:'badge-amber', CONFIRMADA:'badge-blue', REALIZADA:'badge-green', CANCELADA:'badge-red', REPROGRAMADA:'badge-purple' }[e] || 'badge-gray';
    }

    formatPrice(p: number) {
        return new Intl.NumberFormat('es-CO', { style:'currency', currency:'COP', maximumFractionDigits:0 }).format(p ?? 0);
    }

    cerrarSesion() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}