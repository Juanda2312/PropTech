import { Component } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-registro',
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './registro.html',
  styleUrl: './registro.css'
})
export class Registro {
  nombre = '';
  correo = '';
  password = '';
  confirmar = '';
  registroExitoso = false;
  verPassword = false;
  verConfirm = false;
  errorNombre = false;
  errorCorreo = false;
  errorPassword = false;
  errorConfirmar = false;
  errorCorreoExiste = false;

  constructor(private router: Router) {}

  validarNombre() { this.errorNombre = this.nombre.trim().length < 3; }
  validarCorreo() { this.errorCorreo = !this.correo.includes('@') || !this.correo.includes('.'); }
  validarPassword() { this.errorPassword = this.password.length < 6; }
  validarConfirmar() { this.errorConfirmar = this.password !== this.confirmar; }

  registrar() {
    this.validarNombre();
    this.validarCorreo();
    this.validarPassword();
    this.validarConfirmar();
    this.errorCorreoExiste = false;

    if (this.errorNombre || this.errorCorreo || this.errorPassword || this.errorConfirmar) return;

    const usuarios: any[] = JSON.parse(localStorage.getItem('usuarios') || '[]');
    const existe = usuarios.find((u: any) => u.correo === this.correo);
    if (existe) { this.errorCorreoExiste = true; return; }

    usuarios.push({ nombre: this.nombre, correo: this.correo, password: this.password });
    localStorage.setItem('usuarios', JSON.stringify(usuarios));

    this.registroExitoso = true;
    setTimeout(() => this.router.navigate(['/login']), 2000);
  }
}