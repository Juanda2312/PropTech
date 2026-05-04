import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-dashboard',
  imports: [RouterModule, CommonModule, SidebarComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard {
  visitasHoy = [
    { hora: '9:00 AM', cliente: 'Carlos Martínez', inmueble: 'Casa La Castellana', estado: 'confirmada' },
    { hora: '11:30 AM', cliente: 'Ana Gómez', inmueble: 'Apto. Norte', estado: 'pendiente' },
    { hora: '3:00 PM', cliente: 'Luis Herrera', inmueble: 'Local Comercial', estado: 'confirmada' },
  ];

  alertas = [
    { titulo: 'Contrato por vencer', descripcion: 'Apto. Centenario · en 5 días', nivel: 'alto' },
    { titulo: 'Sin visitas hace 30 días', descripcion: 'Bodega Zona Industrial', nivel: 'medio' },
    { titulo: 'Alta demanda detectada', descripcion: 'Zona Norte · 12 consultas', nivel: 'bajo' },
  ];

  inmuebles = [
    { codigo: 'INM001', nombre: 'Apartamento Norte', tipo: 'Apartamento', finalidad: 'Venta', precio: 680000000, estado: 'disponible', asesor: 'Juan Tapiero' },
    { codigo: 'INM002', nombre: 'Casa La Castellana', tipo: 'Casa', finalidad: 'Arriendo', precio: 2300000, estado: 'reservado', asesor: 'Jose Bedoya' },
    { codigo: 'INM003', nombre: 'Local Centro', tipo: 'Local comercial', finalidad: 'Arriendo', precio: 3500000, estado: 'disponible', asesor: 'Juan Tapiero' },
    { codigo: 'INM004', nombre: 'Oficina Suramericana', tipo: 'Oficina', finalidad: 'Arriendo', precio: 2800000, estado: 'disponible', asesor: 'Jose Bedoya' },
    { codigo: 'INM005', nombre: 'Lote Sector Norte', tipo: 'Lote', finalidad: 'Venta', precio: 450000000, estado: 'vendido', asesor: 'Juan Tapiero' },
  ];
}