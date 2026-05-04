import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../../sidebar/sidebar';

@Component({
  selector: 'app-comportamiento',
  imports: [CommonModule, SidebarComponent],
  templateUrl: './comportamiento.html',
  styleUrl: './comportamiento.css'
})
export class Comportamiento {
  comportamientos = [
    { titulo: 'Alto número de visitas sin cierre', descripcion: 'El inmueble INM003 ha recibido 15 visitas en los últimos 30 días sin ningún cierre.', entidad: 'INM003 - Local Comercial Centro', nivel: 'alto', fecha: '13/04/2026', revisado: false },
    { titulo: 'Cliente con múltiples visitas sin continuidad', descripcion: 'El cliente Carlos Martínez agendó 8 visitas en 10 días sin registrar intención de compra.', entidad: 'Cliente: Carlos Martínez', nivel: 'medio', fecha: '12/04/2026', revisado: false },
    { titulo: 'Asesor con sobrecarga de atención', descripcion: 'Juan Tapiero tiene 24 visitas asignadas esta semana, superando el límite recomendado de 15.', entidad: 'Asesor: Juan David Tapiero', nivel: 'alto', fecha: '11/04/2026', revisado: false },
    { titulo: 'Precio cambia con frecuencia', descripcion: 'El inmueble INM005 ha tenido 5 cambios de precio en los últimos 15 días.', entidad: 'INM005 - Lote Sector Norte', nivel: 'medio', fecha: '10/04/2026', revisado: true },
    { titulo: 'Concentración de interés en zona Norte', descripcion: 'Se detectaron 45 consultas en la zona Norte en las últimas 48 horas, 3 veces el promedio normal.', entidad: 'Zona: Norte Armenia', nivel: 'bajo', fecha: '09/04/2026', revisado: false },
  ];

  historial = [
    { fecha: '13/04/2026', tipo: 'Visitas sin cierre', entidad: 'INM003', descripcion: '15 visitas sin cierre en 30 días', nivel: 'alto', estado: 'pendiente' },
    { fecha: '12/04/2026', tipo: 'Cliente atípico', entidad: 'Carlos Martínez', descripcion: '8 visitas en 10 días', nivel: 'medio', estado: 'pendiente' },
    { fecha: '11/04/2026', tipo: 'Sobrecarga asesor', entidad: 'Juan Tapiero', descripcion: '24 visitas asignadas', nivel: 'alto', estado: 'pendiente' },
    { fecha: '10/04/2026', tipo: 'Cambio de precio', entidad: 'INM005', descripcion: '5 cambios en 15 días', nivel: 'medio', estado: 'atendido' },
    { fecha: '09/04/2026', tipo: 'Demanda inusual', entidad: 'Zona Norte', descripcion: '45 consultas en 48 horas', nivel: 'bajo', estado: 'pendiente' },
  ];

  marcarRevisado(a: any) { a.revisado = !a.revisado; }
}