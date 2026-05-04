import { Component } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  correo = '';
  password = '';
  loginExitoso = false;
  errorCorreo = false;
  errorPassword = false;
  errorGeneral = false;
  verPassword = false;
  nombreUsuario = '';

  usuarios: any[] = JSON.parse(localStorage.getItem('usuarios') || '[]');

  constructor(private router: Router) {}

  validarCorreo() {
    this.errorCorreo = !this.correo.includes('@') || !this.correo.includes('.');
  }

  validarPassword() {
    this.errorPassword = this.password.length < 6;
  }

  login() {
    this.validarCorreo();
    this.validarPassword();
    this.errorGeneral = false;
    if (this.errorCorreo || this.errorPassword) return;

    const usuario = this.usuarios.find((u: any) => u.correo === this.correo && u.password === this.password);
    if (usuario) {
      this.loginExitoso = true;
      this.nombreUsuario = usuario.nombre;
      localStorage.setItem('usuarioActivo', JSON.stringify(usuario));
      setTimeout(() => this.router.navigate(['/']), 2000);
    } else {
      this.errorGeneral = true;
    }
  }
}