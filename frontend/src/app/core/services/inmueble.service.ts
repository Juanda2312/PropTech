// src/app/core/services/inmueble.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Inmueble, InmuebleDTO, TipoInmueble, FinalidadInmueble } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class InmuebleService {
  private base = `${environment.apiUrl}/api`;

  constructor(private http: HttpClient) {}

  // Vía PlataformaBeta (registra en grafo también)
  registrar(dto: InmuebleDTO): Observable<Inmueble> {
    return this.http.post<Inmueble>(`${this.base}/plataforma/inmuebles`, dto);
  }

  buscarPorCodigo(codigo: string): Observable<Inmueble> {
    return this.http.get<Inmueble>(`${this.base}/inmuebles/${codigo}`);
  }

  listar(filtros?: {
    tipo?: TipoInmueble;
    finalidad?: FinalidadInmueble;
    ciudad?: string;
    disponible?: boolean;
    precioMin?: number;
    precioMax?: number;
    habitacionesMin?: number;
  }): Observable<Inmueble[]> {
    let params = new HttpParams();
    if (filtros?.tipo) params = params.set('tipo', filtros.tipo);
    if (filtros?.finalidad) params = params.set('finalidad', filtros.finalidad);
    if (filtros?.ciudad) params = params.set('ciudad', filtros.ciudad);
    if (filtros?.disponible !== undefined) params = params.set('disponible', filtros.disponible);
    if (filtros?.precioMin) params = params.set('precioMin', filtros.precioMin);
    if (filtros?.precioMax) params = params.set('precioMax', filtros.precioMax);
    if (filtros?.habitacionesMin) params = params.set('habitacionesMin', filtros.habitacionesMin);
    return this.http.get<Inmueble[]>(`${this.base}/inmuebles`, { params });
  }

  actualizar(codigo: string, dto: InmuebleDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/inmuebles/${codigo}`, dto);
  }

  eliminar(codigo: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/inmuebles/${codigo}`);
  }

  deshacerCambio(): Observable<void> {
    return this.http.post<void>(`${this.base}/inmuebles/deshacer`, {});
  }

  ordenadosPorPrecio(): Observable<Inmueble[]> {
    return this.http.get<Inmueble[]>(`${this.base}/plataforma/inmuebles/ordenados`);
  }

  buscarConFiltros(filtros: {
    tipo?: TipoInmueble;
    finalidad?: FinalidadInmueble;
    ciudad?: string;
    precioMax?: number;
    habitacionesMin?: number;
  }): Observable<Inmueble[]> {
    let params = new HttpParams();
    if (filtros.tipo) params = params.set('tipo', filtros.tipo);
    if (filtros.finalidad) params = params.set('finalidad', filtros.finalidad);
    if (filtros.ciudad) params = params.set('ciudad', filtros.ciudad);
    if (filtros.precioMax) params = params.set('precioMax', filtros.precioMax);
    if (filtros.habitacionesMin) params = params.set('habitacionesMin', filtros.habitacionesMin);
    return this.http.get<Inmueble[]>(`${this.base}/plataforma/inmuebles/buscar`, { params });
  }

  sugerirSimilares(codigo: string): Observable<Inmueble[]> {
    return this.http.get<Inmueble[]>(`${this.base}/plataforma/similares/${codigo}`);
  }
}
