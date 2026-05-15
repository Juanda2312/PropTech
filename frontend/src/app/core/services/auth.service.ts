// src/app/core/services/auth.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { map, catchError } from 'rxjs/operators';

export interface Usuario {
    id: string;
    nombre: string;
    correo: string;
    rol: 'ADMIN' | 'CLIENTE';
    backendId?: string;
}

export interface LoginResult {
    ok: boolean;
    mensaje: string;
    rol?: string;
}

const ADMINS = [
    { correo: 'jose@gmail.com',    id: '1111100000', nombre: 'José Admin',    rol: 'ADMIN' as const },
    { correo: 'tapiero@gmail.com', id: '0000011111', nombre: 'Tapiero Admin', rol: 'ADMIN' as const },
];

@Injectable({ providedIn: 'root' })
export class AuthService {
    private usuarioSubject = new BehaviorSubject<Usuario | null>(this.cargarSesion());
    usuario$ = this.usuarioSubject.asObservable();

    constructor(private http: HttpClient) {}

    get usuario(): Usuario | null { return this.usuarioSubject.value; }
    get estaAutenticado(): boolean { return !!this.usuarioSubject.value; }
    get esAdmin(): boolean { return this.usuarioSubject.value?.rol === 'ADMIN'; }

    private cargarSesion(): Usuario | null {
        try {
            const raw = sessionStorage.getItem('proptech_usuario');
            return raw ? JSON.parse(raw) : null;
        } catch { return null; }
    }

    private guardarSesion(u: Usuario) {
        sessionStorage.setItem('proptech_usuario', JSON.stringify(u));
        this.usuarioSubject.next(u);
    }

    // ----------------------------------------------------------------
    // LOGIN
    // ----------------------------------------------------------------
    login(correo: string, id: string): Observable<LoginResult> {
        // Validaciones básicas
        if (!correo || !id) {
            return of({ ok: false, mensaje: 'Completa todos los campos.' });
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
            return of({ ok: false, mensaje: 'El correo no es válido.' });
        }
        if (!/^\d{10}$/.test(id)) {
            return of({ ok: false, mensaje: 'El número de identificación debe tener exactamente 10 dígitos.' });
        }

        const correoNorm = correo.toLowerCase().trim();

        // PASO 1: Admin hardcodeado
        const admin = ADMINS.find(a => a.correo === correoNorm && a.id === id);
        if (admin) {
            console.log('[Auth] Login como ADMIN:', admin.nombre);
            const usuario: Usuario = { id: admin.id, nombre: admin.nombre, correo: admin.correo, rol: 'ADMIN' };
            this.guardarSesion(usuario);
            return of({ ok: true, mensaje: 'Bienvenido administrador.', rol: 'ADMIN' });
        }

        // PASO 2: Cliente registrado desde el frontend (localStorage)
        const clientesRaw = localStorage.getItem('proptech_clientes_auth');
        const clientesLocales: Usuario[] = clientesRaw ? JSON.parse(clientesRaw) : [];
        const clienteLocal = clientesLocales.find(c => c.correo === correoNorm && c.id === id);
        if (clienteLocal) {
            console.log('[Auth] Login desde localStorage:', clienteLocal.nombre);
            this.guardarSesion(clienteLocal);
            return of({ ok: true, mensaje: 'Bienvenido.', rol: 'CLIENTE' });
        }

        // PASO 3: Consultar backend (clientes del DataLoader)
        console.log('[Auth] No encontrado localmente, consultando backend para:', correoNorm, id);

        return this.http.get<any[]>('/api/clientes').pipe(
            map(clientes => {
                console.log('[Auth] Clientes recibidos del backend:', clientes.length);

                const encontrado = clientes.find(c => {
                    const correoMatch = c.correo?.toLowerCase().trim() === correoNorm;
                    const idMatch = c.id === id;
                    console.log(`[Auth] id=${c.id} correo=${c.correo} | idMatch=${idMatch} correoMatch=${correoMatch}`);
                    return correoMatch && idMatch;
                });

                if (!encontrado) {
                    console.warn('[Auth] Cliente no encontrado en backend');
                    return { ok: false, mensaje: 'Credenciales incorrectas. Verifica tu correo y número de identificación.' };
                }

                console.log('[Auth] Cliente encontrado en backend:', encontrado.nombre);

                const usuario: Usuario = {
                    id: encontrado.id,
                    nombre: encontrado.nombre,
                    correo: encontrado.correo,
                    rol: 'CLIENTE',
                    backendId: encontrado.id,
                };
                this.guardarSesion(usuario);

                // Cachear en localStorage para futuras sesiones
                const actualizados = [
                    ...clientesLocales.filter(c => c.id !== usuario.id),
                    usuario
                ];
                localStorage.setItem('proptech_clientes_auth', JSON.stringify(actualizados));

                return { ok: true, mensaje: 'Bienvenido.', rol: 'CLIENTE' };
            }),
            catchError(err => {
                console.error('[Auth] Error al consultar backend:', err);
                return of({
                    ok: false,
                    mensaje: 'No se pudo conectar con el servidor. Verifica que el backend esté activo.'
                });
            })
        );
    }

    // ----------------------------------------------------------------
    // REGISTRO
    // ----------------------------------------------------------------
    registrar(nombre: string, correo: string, id: string, telefono = '0000000000'): { ok: boolean; mensaje: string } {
        if (!nombre || !correo || !id) return { ok: false, mensaje: 'Completa todos los campos.' };
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) return { ok: false, mensaje: 'El correo no es válido.' };
        if (!/^\d{10}$/.test(id)) return { ok: false, mensaje: 'El número de identificación debe tener exactamente 10 dígitos.' };

        const correoNorm = correo.toLowerCase().trim();
        const esAdmin = ADMINS.some(a => a.correo === correoNorm);
        if (esAdmin) return { ok: false, mensaje: 'Este correo no puede registrarse como cliente.' };

        const clientesRaw = localStorage.getItem('proptech_clientes_auth');
        const clientes: Usuario[] = clientesRaw ? JSON.parse(clientesRaw) : [];
        if (clientes.some(c => c.correo === correoNorm)) {
            return { ok: false, mensaje: 'Ya existe una cuenta con este correo. Inicia sesión.' };
        }

        const nuevo: Usuario = {
            id,
            nombre,
            correo: correoNorm,
            rol: 'CLIENTE',
            backendId: id,
        };

        const dto = {
            id,
            nombre,
            correo: correoNorm,
            telefono,
            tipoCliente: 'COMPRADOR',
            presupuesto: 0,
            zonasInteres: ['NORTE'],
            tipoInmuebleDeseado: 'APARTAMENTO',
            habitacionesMinimas: 1,
            estadoBusqueda: 'ACTIVO',
        };

        this.http.post('/api/plataforma/clientes', dto).subscribe({
            next: (res) => console.log('✅ Cliente creado en backend:', res),
            error: (e) => console.error('❌ Error al crear en backend:', e?.error),
        });

        clientes.push(nuevo);
        localStorage.setItem('proptech_clientes_auth', JSON.stringify(clientes));
        this.guardarSesion(nuevo);
        return { ok: true, mensaje: 'Cuenta creada correctamente.' };
    }

    logout() {
        sessionStorage.removeItem('proptech_usuario');
        this.usuarioSubject.next(null);
    }
}