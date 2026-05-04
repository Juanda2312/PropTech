import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './inicio.html',
  styleUrl: './inicio.css'
})
export class Inicio {
  inmuebles = [
    {
      nombre: 'Apartamento Norte Armenia',
      ciudad: 'Armenia, Quindío',
      tipo: 'Apartamento',
      finalidad: 'Venta',
      precio: 680000000,
      habitaciones: 3,
      banos: 2,
      area: 85,
      imagen: 'img/casa-Franklin.jpg'
    },
    {
      nombre: 'Casa La Castellana',
      ciudad: 'Armenia, Quindío',
      tipo: 'Casa',
      finalidad: 'Arriendo',
      precio: 2300000,
      habitaciones: 4,
      banos: 3,
      area: 120,
      imagen: 'img/casa-mickey-mouse.jpg'
    },
    {
      nombre: 'Local Comercial Centro',
      ciudad: 'Armenia, Quindío',
      tipo: 'Local comercial',
      finalidad: 'Arriendo',
      precio: 3500000,
      habitaciones: 0,
      banos: 1,
      area: 45,
      imagen: 'img/casa-bob-esponja.jpg'
    },
    {
      nombre: 'Oficina Edificio Sudamericana',
      ciudad: 'Armenia, Quindío',
      tipo: 'Oficina',
      finalidad: 'Arriendo',
      precio: 2800000,
      habitaciones: 0,
      banos: 1,
      area: 60,
      imagen: 'img/casa-goku.jpg'
    },
    {
      nombre: 'Lote Sector Norte',
      ciudad: 'Armenia, Quindío',
      tipo: 'Lote',
      finalidad: 'Venta',
      precio: 450000000,
      habitaciones: 0,
      banos: 0,
      area: 300,
      imagen: 'img/casa-minecraft.jpg'
    },
    {
      nombre: 'Bodega Zona Industrial',
      ciudad: 'Armenia, Quindío',
      tipo: 'Bodega',
      finalidad: 'Arriendo',
      precio: 5500000,
      habitaciones: 0,
      banos: 2,
      area: 800,
      imagen: 'img/casa-india.jpg'
    },
    {
      nombre: 'Apartamento Centenario',
      ciudad: 'Armenia, Quindío',
      tipo: 'Apartamento',
      finalidad: 'Arriendo',
      precio: 1800000,
      habitaciones: 2,
      banos: 1,
      area: 65,
      imagen: 'img/centenario.jpg'
    },
    {
      nombre: 'Casa Campestre Calarcá',
      ciudad: 'Calarcá, Quindío',
      tipo: 'Casa',
      finalidad: 'Venta',
      precio: 850000000,
      habitaciones: 5,
      banos: 3,
      area: 280,
      imagen: 'img/casa-calarca.jpg'
    }
  ];
}
