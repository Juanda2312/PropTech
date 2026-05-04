import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-favoritos',
  imports: [RouterModule, CommonModule],
  templateUrl: './favoritos.html',
  styleUrl: './favoritos.css'
})
export class Favoritos {
  tabActivo = 'favoritos';

  favoritos = [
    { codigo: 'INM001', nombre: 'Apartamento Norte Armenia', ciudad: 'Armenia', tipo: 'Apartamento', precio: 680000000, imagen: 'img/casa-Franklin.jpg' },
    { codigo: 'INM002', nombre: 'Casa La Castellana', ciudad: 'Armenia', tipo: 'Casa', precio: 2300000, imagen: 'img/casa-mickey-mouse.jpg' },
  ];

  historial = [
    { codigo: 'INM003', nombre: 'Local Comercial Centro', ciudad: 'Armenia', tipo: 'Local comercial', precio: 3500000, imagen: 'img/casa-bob-esponja.jpg', estado: 'consultado', fecha: '10/04/2026' },
    { codigo: 'INM004', nombre: 'Oficina Sudamericana', ciudad: 'Armenia', tipo: 'Oficina', precio: 2800000, imagen: 'img/casa-goku.jpg', estado: 'visitado', fecha: '08/04/2026' },
    { codigo: 'INM005', nombre: 'Lote Sector Norte', ciudad: 'Armenia', tipo: 'Lote', precio: 450000000, imagen: 'img/casa-minecraft.jpg', estado: 'negociado', fecha: '05/04/2026' },
  ];

  getHistorial() {
    const map: any = { consultados: 'consultado', visitados: 'visitado', negociados: 'negociado' };
    return this.historial.filter(h => h.estado === map[this.tabActivo]);
  }

  quitar(item: any) {
    this.favoritos = this.favoritos.filter(f => f.codigo !== item.codigo);
  }
}