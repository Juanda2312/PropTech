// src/app/features/cliente-portal/cliente-portal.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { InmuebleService } from '../../core/services/inmueble.service';
import { ClienteService, Interaccion, IntencionDTO } from '../../core/services/cliente.service';
import { AsesorService } from '../../core/services/asesor.service';
import { ToastService } from '../../core/services/toast.service';
import { Inmueble, Visita, Asesor } from '../../core/models';

type Seccion = 'inmuebles' | 'visitas' | 'favoritos' | 'historial' | 'interacciones';

@Component({
    selector: 'app-cliente-portal',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './cliente-portal.component.html',
    styleUrls: ['./cliente-portal.component.css']
})
export class ClientePortalComponent implements OnInit {
    usuario = this.authService.usuario;
    inmuebles: Inmueble[] = [];
    favoritos: Inmueble[] = [];
    historial: Inmueble[] = [];
    asesores: Asesor[] = [];
    interacciones: Interaccion[] = [];
    loading = true;
    loadingInteracciones = false;
    seccion: Seccion = 'inmuebles';
    filtroTexto = '';
    clienteBackendId = '';
    vinculado = false;

    // Modal: agendar visita
    showVisitaModal = false;
    submittingVisita = false;
    visitaForm = { codigoInmueble: '', idAsesor: '', fecha: '', hora: '', observaciones: '' };

    // Modal: intención de compra/renta
    showIntencionModal = false;
    submittingIntencion = false;
    intencionForm: { codigoInmueble: string; tipo: 'INTENCION_COMPRA' | 'INTENCION_RENTA'; detalle: string } = {
        codigoInmueble: '', tipo: 'INTENCION_COMPRA', detalle: ''
    };

    // filtro historial interacciones
    filtroTipoInteraccion = '';

    constructor(
        private authService: AuthService,
        private inmuebleService: InmuebleService,
        private clienteService: ClienteService,
        private asesorService: AsesorService,
        private toast: ToastService,
        private router: Router
    ) {}

    ngOnInit() {
        this.resolverClienteBackend();
        this.asesorService.listar().subscribe({ next: d => this.asesores = d, error: () => {} });
    }

    // ── Resolución de identidad backend ──────────────────────────────

    resolverClienteBackend() {
        const backendIdSesion = this.usuario?.backendId;
        if (backendIdSesion) {
            this.clienteService.buscarPorId(backendIdSesion).subscribe({
                next: () => {
                    this.clienteBackendId = backendIdSesion;
                    this.vinculado = true;
                    this.cargarTodo();
                },
                error: () => this.buscarPorCorreo()
            });
        } else {
            this.buscarPorCorreo();
        }
    }

    buscarPorCorreo() {
        this.clienteService.listar().subscribe({
            next: clientes => {
                const encontrado = clientes.find(
                    c => c.correo?.toLowerCase() === this.usuario?.correo?.toLowerCase()
                );
                if (encontrado) {
                    this.clienteBackendId = encontrado.id;
                    this.vinculado = true;
                    const u = { ...this.usuario!, backendId: encontrado.id };
                    sessionStorage.setItem('proptech_usuario', JSON.stringify(u));
                }
                this.cargarInmuebles();
                if (this.vinculado) this.cargarHistorialYFavoritos();
            },
            error: () => this.cargarInmuebles()
        });
    }

    cargarTodo() {
        this.cargarInmuebles();
        this.cargarHistorialYFavoritos();
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
    }

    cargarInteracciones() {
        if (!this.clienteBackendId) return;
        this.loadingInteracciones = true;
        const tipo = this.filtroTipoInteraccion || undefined;
        this.clienteService.obtenerInteracciones(this.clienteBackendId, tipo).subscribe({
            next: d => { this.interacciones = d; this.loadingInteracciones = false; },
            error: () => this.loadingInteracciones = false
        });
    }

    cambiarSeccion(s: Seccion) {
        this.seccion = s;
        if (s === 'historial' || s === 'favoritos') this.cargarHistorialYFavoritos();
        if (s === 'interacciones') this.cargarInteracciones();
    }

    // ── Favoritos ─────────────────────────────────────────────────────

    esFavorito(inm: Inmueble): boolean {
        return this.favoritos.some(f => f.codigo === inm.codigo);
    }

    toggleFavorito(inm: Inmueble) {
        if (!this.clienteBackendId) {
            this.toast.warn('Tu cuenta no está registrada. Contacta a un asesor.');
            return;
        }
        if (this.esFavorito(inm)) {
            this.clienteService.eliminarFavorito(this.clienteBackendId, inm.codigo).subscribe({
                next: () => { this.toast.success('Eliminado de favoritos'); this.cargarHistorialYFavoritos(); },
                error: (e: any) => this.toast.error(e.message)
            });
        } else {
            this.clienteService.marcarFavorito(this.clienteBackendId, inm.codigo).subscribe({
                next: () => { this.toast.success('¡Agregado a favoritos!'); this.cargarHistorialYFavoritos(); },
                error: (e: any) => this.toast.error(e.message)
            });
        }
    }

    // ── Agendar visita ────────────────────────────────────────────────

    abrirAgendarVisita(inm?: Inmueble) {
        if (!this.clienteBackendId) {
            this.toast.warn('Tu cuenta no está registrada. Contacta a un asesor.');
            return;
        }
        this.visitaForm = {
            codigoInmueble: inm?.codigo || '',
            idAsesor: inm?.asesor?.id || '',
            fecha: '',
            hora: '',
            observaciones: ''
        };
        this.showVisitaModal = true;
    }

    confirmarVisita() {
        if (!this.visitaForm.codigoInmueble || !this.visitaForm.fecha || !this.visitaForm.hora) {
            this.toast.warn('Completa inmueble, fecha y hora');
            return;
        }
        this.submittingVisita = true;
        const dto = {
            idVisita: 'VP-' + Date.now(),
            idCliente: this.clienteBackendId,
            codigoInmueble: this.visitaForm.codigoInmueble,
            idAsesor: this.visitaForm.idAsesor || this.asesores[0]?.id,
            fecha: this.visitaForm.fecha,
            hora: this.visitaForm.hora + ':00',
            observaciones: this.visitaForm.observaciones || 'Agendada desde el portal'
        };
        this.clienteService.agendarVisitaDesdePortal(this.clienteBackendId, dto).subscribe({
            next: () => {
                this.toast.success('¡Visita agendada!');
                this.showVisitaModal = false;
                this.submittingVisita = false;
                if (this.seccion === 'interacciones') this.cargarInteracciones();
            },
            error: (e: any) => { this.toast.error(e.message); this.submittingVisita = false; }
        });
    }

    // ── Intención de compra / renta ───────────────────────────────────

    abrirIntencion(inm?: Inmueble, tipo: 'INTENCION_COMPRA' | 'INTENCION_RENTA' = 'INTENCION_COMPRA') {
        if (!this.clienteBackendId) {
            this.toast.warn('Tu cuenta no está registrada. Contacta a un asesor.');
            return;
        }
        this.intencionForm = { codigoInmueble: inm?.codigo || '', tipo, detalle: '' };
        this.showIntencionModal = true;
    }

    confirmarIntencion() {
        if (!this.intencionForm.codigoInmueble) { this.toast.warn('Selecciona un inmueble'); return; }
        this.submittingIntencion = true;
        const dto: IntencionDTO = {
            codigoInmueble: this.intencionForm.codigoInmueble,
            tipo: this.intencionForm.tipo,
            detalle: this.intencionForm.detalle
        };
        this.clienteService.registrarIntencion(this.clienteBackendId, dto).subscribe({
            next: () => {
                const accion = dto.tipo === 'INTENCION_COMPRA' ? 'compra' : 'renta';
                this.toast.success(`¡Intención de ${accion} registrada! Un asesor se comunicará contigo.`);
                this.showIntencionModal = false;
                this.submittingIntencion = false;
                if (this.seccion === 'interacciones') this.cargarInteracciones();
            },
            error: (e: any) => { this.toast.error(e.message); this.submittingIntencion = false; }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────

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

    tipoInteraccionLabel(tipo: string): string {
        const labels: Record<string, string> = {
            VISITA_AGENDADA:     '📅 Visita agendada',
            COMPRA_REALIZADA:    '🏠 Compra realizada',
            ARRIENDO_REALIZADO:  '🔑 Arriendo realizado',
            FAVORITO_GUARDADO:   '⭐ Favorito guardado',
            INTENCION_COMPRA:    '💰 Intención de compra',
            INTENCION_RENTA:     '🔑 Intención de renta',
            INMUEBLE_DESCARTADO: '✕ Inmueble descartado',
            INMUEBLE_CONSULTADO: '👁 Inmueble consultado'
        };
        return labels[tipo] || tipo;
    }

    tipoInteraccionBadge(tipo: string): string {
        const map: Record<string, string> = {
            VISITA_AGENDADA:     'badge-blue',
            COMPRA_REALIZADA:    'badge-teal',
            ARRIENDO_REALIZADO:  'badge-purple',
            FAVORITO_GUARDADO:   'badge-gold',
            INTENCION_COMPRA:    'badge-amber',
            INTENCION_RENTA:     'badge-amber',
            INMUEBLE_DESCARTADO: 'badge-red',
            INMUEBLE_CONSULTADO: 'badge-gray'
        };
        return map[tipo] || 'badge-gray';
    }

    estadoBadge(e: string) {
        return { PENDIENTE:'badge-amber', CONFIRMADA:'badge-blue', REALIZADA:'badge-green',
            CANCELADA:'badge-red', REPROGRAMADA:'badge-purple' }[e] || 'badge-gray';
    }

    formatPrice(p: number) {
        return new Intl.NumberFormat('es-CO', { style:'currency', currency:'COP', maximumFractionDigits:0 }).format(p ?? 0);
    }

    cerrarSesion() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}