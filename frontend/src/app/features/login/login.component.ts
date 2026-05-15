// src/app/features/login/login.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent {
    modo: 'login' | 'registro' = 'login';

    correo = '';
    id = '';
    nombre = '';
    error = '';
    exito = '';
    cargando = false;

    constructor(private authService: AuthService, private router: Router) {}

    entrar() {
        this.error = ''; this.exito = '';
        this.cargando = true;
        setTimeout(() => {
            const res = this.authService.login(this.correo, this.id);
            this.cargando = false;
            if (!res.ok) { this.error = res.mensaje; return; }
            if (res.rol === 'ADMIN') this.router.navigate(['/dashboard']);
            else this.router.navigate(['/cliente']);
        }, 600);
    }

    registrarse() {
        this.error = ''; this.exito = '';
        this.cargando = true;
        setTimeout(() => {
            const res = this.authService.registrar(this.nombre, this.correo, this.id);
            this.cargando = false;
            if (!res.ok) { this.error = res.mensaje; return; }
            this.exito = res.mensaje;
            setTimeout(() => this.router.navigate(['/cliente']), 800);
        }, 600);
    }

    cambiarModo(m: 'login' | 'registro') {
        this.modo = m; this.error = ''; this.exito = '';
        this.correo = ''; this.id = ''; this.nombre = '';
    }
}