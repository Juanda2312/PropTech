import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-recomendaciones',
  imports: [RouterModule, CommonModule],
  templateUrl: './recomendaciones.html',
  styleUrl: './recomendaciones.css'
})
export class Recomendaciones {
  recomendaciones = [
    {
      codigo: 'INM001', nombre: 'Apartamento Norte Armenia', ciudad: 'Armenia', zona: 'Norte',
      tipo: 'Apartamento', finalidad: 'Venta', precio: 680000000, habitaciones: 3, banos: 2, area: 85,
      imagen: 'img/casa-Franklin.jpg', match: 98,
      razones: ['Dentro de tu presupuesto', 'Zona de interés', 'Habitaciones suficientes']
    },
    {
      codigo: 'INM007', nombre: 'Apartamento Centenario', ciudad: 'Armenia', zona: 'Centro',
      tipo: 'Apartamento', finalidad: 'Arriendo', precio: 1800000, habitaciones: 2, banos: 1, area: 65,
      imagen: 'img/centenario.jpg', match: 85,
      razones: ['Tipo de inmueble deseado', 'Precio competitivo']
    },
    {
      codigo: 'INM002', nombre: 'Casa La Castellana', ciudad: 'Armenia', zona: 'Norte',
      tipo: 'Casa', finalidad: 'Arriendo', precio: 2300000, habitaciones: 4, banos: 3, area: 120,
      imagen: 'img/casa-mickey-mouse.jpg', match: 72,
      razones: ['Zona de interés', 'Visitado por clientes similares']
    },
  ];
} 