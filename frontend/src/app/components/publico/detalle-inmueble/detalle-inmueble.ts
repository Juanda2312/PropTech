import { Component } from '@angular/core';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-detalle-inmueble',
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './detalle-inmueble.html',
  styleUrl: './detalle-inmueble.css'
})
export class DetalleInmueble {
  mostrarFormVisita = false;
  visitaAgendada = false;
  esFavorito = false;

  visita = { cliente: '', telefono: '', fecha: '', hora: '', observaciones: '' };

  inmueble = {
    codigo: 'INM001',
    nombre: 'Apartamento Norte Armenia',
    tipo: 'Apartamento',
    finalidad: 'Venta',
    precio: 680000000,
    direccion: 'Cra. 14 # 32-55',
    ciudad: 'Armenia, Quindío',
    zona: 'Norte',
    habitaciones: 3,
    banos: 2,
    area: 85,
    estrato: 4,
    imagen: 'img/casa-Franklin.jpg',
    galeria: ['img/casa-Franklin.jpg', 'img/casa-mickey-mouse.jpg', 'img/casa-bob-esponja.jpg'],
    descripcion: 'Hermoso apartamento ubicado en el sector norte de Armenia, con excelente vista, acabados de lujo y zona social completa. Ideal para familias que buscan comodidad y seguridad en una de las mejores zonas de la ciudad.',
    caracteristicas: ['Parqueadero', 'Depósito', 'Piscina', 'Gimnasio', 'Portería 24h', 'Balcón', 'Cocina integral', 'Cuarto de servicio'],
    asesor: { nombre: 'Juan David Tapiero', iniciales: 'JT', zona: 'Zona Norte Armenia', telefono: '+57 315 401 5585' }
  };

  constructor(private route: ActivatedRoute) {}

  toggleFav() { this.esFavorito = !this.esFavorito; }

  agendarVisita() {
    if (this.visita.cliente && this.visita.fecha && this.visita.hora) {
      this.visitaAgendada = true;
      setTimeout(() => {
        this.mostrarFormVisita = false;
        this.visitaAgendada = false;
      }, 3000);
    }
  }
}