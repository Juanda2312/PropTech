import { Component } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-login-admin',
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './login-admin.html',
  styleUrl: './login-admin.css'
})
export class LoginAdmin {
  correo = '';
  password = '';
  loginExitoso = false;
  verPassword = false;
  errorCorreo = false;
  errorPassword = false;
  errorGeneral = false;
  nombreUsuario = '';

  constructor(private router: Router, private http: HttpClient) {}

  validarCorreo() { this.errorCorreo = !this.correo.includes('@') || !this.correo.includes('.'); }
  validarPassword() { this.errorPassword = this.password.length === 0; }

  login() {
    this.validarCorreo();
    this.validarPassword();
    this.errorGeneral = false;
    if (this.errorCorreo || this.errorPassword) return;

    this.http.get<any[]>('usuarios-admin.json').subscribe(admins => {
      const admin = admins.find(a => a.correo === this.correo && a.password === this.password);
      if (admin) {
        this.loginExitoso = true;
        this.nombreUsuario = admin.nombre;
        localStorage.setItem('adminActivo', JSON.stringify(admin));
        setTimeout(() => this.router.navigate(['/admin/dashboard']), 2000);
      } else {
        this.errorGeneral = true;
      }
    });
  }
}