// src/app/core/services/auth.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { HttpClient } from '@angular/common/http';

export interface Usuario {
    id: string;
    nombre: string;
    correo: string;
    rol: 'ADMIN' | 'CLIENTE';
    backendId?: string; // ID tipo CLI-XXX en el backend
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

    login(correo: string, id: string): { ok: boolean; mensaje: string; rol?: string } {
        if (!correo || !id) return { ok: false, mensaje: 'Completa todos los campos.' };
        const correoRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!correoRegex.test(correo)) return { ok: false, mensaje: 'El correo no es válido.' };
        if (!/^\d{10}$/.test(id)) return { ok: false, mensaje: 'El número de identificación debe tener exactamente 10 dígitos.' };

        const admin = ADMINS.find(a => a.correo === correo.toLowerCase().trim() && a.id === id);
        if (admin) {
            const usuario: Usuario = { id: admin.id, nombre: admin.nombre, correo: admin.correo, rol: 'ADMIN' };
            this.guardarSesion(usuario);
            return { ok: true, mensaje: 'Bienvenido administrador.', rol: 'ADMIN' };
        }

        const clientesRaw = localStorage.getItem('proptech_clientes_auth');
        const clientes: Usuario[] = clientesRaw ? JSON.parse(clientesRaw) : [];
        const cliente = clientes.find(c => c.correo === correo.toLowerCase().trim() && c.id === id);
        if (cliente) {
            this.guardarSesion(cliente);
            return { ok: true, mensaje: 'Bienvenido.', rol: 'CLIENTE' };
        }

        return { ok: false, mensaje: 'Credenciales incorrectas. Verifica tu correo y número de identificación.' };
    }

    registrar(nombre: string, correo: string, id: string, telefono = '0000000000'): { ok: boolean; mensaje: string } {
        if (!nombre || !correo || !id) return { ok: false, mensaje: 'Completa todos los campos.' };
        const correoRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!correoRegex.test(correo)) return { ok: false, mensaje: 'El correo no es válido.' };
        if (!/^\d{10}$/.test(id)) return { ok: false, mensaje: 'El número de identificación debe tener exactamente 10 dígitos.' };

        const esAdmin = ADMINS.some(a => a.correo === correo.toLowerCase().trim());
        if (esAdmin) return { ok: false, mensaje: 'Este correo no puede registrarse como cliente.' };

        const clientesRaw = localStorage.getItem('proptech_clientes_auth');
        const clientes: Usuario[] = clientesRaw ? JSON.parse(clientesRaw) : [];
        if (clientes.some(c => c.correo === correo.toLowerCase().trim())) {
            return { ok: false, mensaje: 'Ya existe una cuenta con este correo. Inicia sesión.' };
        }

        const backendId = 'CLI-' + id;

        const nuevo: Usuario = {
            id,
            nombre,
            correo: correo.toLowerCase().trim(),
            rol: 'CLIENTE',
            backendId
        };

        const dto = {
            id: backendId,
            nombre,
            correo: correo.toLowerCase().trim(),
            telefono,
            tipoCliente: 'COMPRADOR',
            presupuesto: 0,
            zonasInteres: ['NORTE'],
            tipoInmuebleDeseado: 'APARTAMENTO',
            habitacionesMinimas: 1,
            estadoBusqueda: 'ACTIVO'
        };

        // LOG para depuración — ver en consola del navegador
        console.log('Intentando registrar en backend:', backendId, dto);

        this.http.post('/api/plataforma/clientes', dto).subscribe({
            next: (res) => console.log('✅ Cliente creado en backend:', res),
            error: (e) => console.error('❌ Error al crear en backend:', e?.error)
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