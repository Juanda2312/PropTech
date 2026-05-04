import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-catalogo',
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css'
})
export class Catalogo {
  filtroTipo = '';
  filtroFinalidad = '';
  filtroCiudad = '';
  filtroPrecio = 0;
  filtroHabitaciones = 0;
  orden = 'precio-asc';

  inmuebles = [
    { codigo: 'INM001', nombre: 'Apartamento Norte Armenia', ciudad: 'Armenia', zona: 'Norte', tipo: 'Apartamento', finalidad: 'Venta', precio: 680000000, habitaciones: 3, banos: 2, area: 85, imagen: 'img/casa-Franklin.jpg', favorito: false },
    { codigo: 'INM002', nombre: 'Casa La Castellana', ciudad: 'Armenia', zona: 'Norte', tipo: 'Casa', finalidad: 'Arriendo', precio: 2300000, habitaciones: 4, banos: 3, area: 120, imagen: 'img/casa-mickey-mouse.jpg', favorito: false },
    { codigo: 'INM003', nombre: 'Local Comercial Centro', ciudad: 'Armenia', zona: 'Centro', tipo: 'Local comercial', finalidad: 'Arriendo', precio: 3500000, habitaciones: 0, banos: 1, area: 45, imagen: 'img/casa-bob-esponja.jpg', favorito: false },
    { codigo: 'INM004', nombre: 'Oficina Edificio Sudamericana', ciudad: 'Armenia', zona: 'Centro', tipo: 'Oficina', finalidad: 'Arriendo', precio: 2800000, habitaciones: 0, banos: 1, area: 60, imagen: 'img/casa-goku.jpg', favorito: false },
    { codigo: 'INM005', nombre: 'Lote Sector Norte', ciudad: 'Armenia', zona: 'Norte', tipo: 'Lote', finalidad: 'Venta', precio: 450000000, habitaciones: 0, banos: 0, area: 300, imagen: 'img/casa-minecraft.jpg', favorito: false },
    { codigo: 'INM006', nombre: 'Bodega Zona Industrial', ciudad: 'Armenia', zona: 'Sur', tipo: 'Bodega', finalidad: 'Arriendo', precio: 5500000, habitaciones: 0, banos: 2, area: 800, imagen: 'img/casa-india.jpg', favorito: false },
    { codigo: 'INM007', nombre: 'Apartamento Centenario', ciudad: 'Armenia', zona: 'Centro', tipo: 'Apartamento', finalidad: 'Arriendo', precio: 1800000, habitaciones: 2, banos: 1, area: 65, imagen: 'img/centenario.jpg', favorito: false },
    { codigo: 'INM008', nombre: 'Casa Campestre Calarcá', ciudad: 'Calarcá', zona: 'Rural', tipo: 'Casa', finalidad: 'Venta', precio: 850000000, habitaciones: 5, banos: 3, area: 280, imagen: 'img/casa-calarca.jpg', favorito: false },
  ];

  inmueblesFiltrados = [...this.inmuebles];

  filtrar() {
    this.inmueblesFiltrados = this.inmuebles.filter(i => {
      return (!this.filtroTipo || i.tipo === this.filtroTipo) &&
             (!this.filtroFinalidad || i.finalidad === this.filtroFinalidad) &&
             (!this.filtroCiudad || i.ciudad === this.filtroCiudad) &&
             (!this.filtroPrecio || i.precio <= this.filtroPrecio) &&
             (i.habitaciones >= this.filtroHabitaciones);
    });
    this.ordenar();
  }

  ordenar() {
    if (this.orden === 'precio-asc') this.inmueblesFiltrados.sort((a, b) => a.precio - b.precio);
    if (this.orden === 'precio-desc') this.inmueblesFiltrados.sort((a, b) => b.precio - a.precio);
    if (this.orden === 'area-desc') this.inmueblesFiltrados.sort((a, b) => b.area - a.area);
  }

  limpiar() {
    this.filtroTipo = '';
    this.filtroFinalidad = '';
    this.filtroCiudad = '';
    this.filtroPrecio = 0;
    this.filtroHabitaciones = 0;
    this.inmueblesFiltrados = [...this.inmuebles];
  }

  toggleFav(item: any) {
    item.favorito = !item.favorito;
  }
}