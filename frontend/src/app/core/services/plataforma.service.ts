// src/app/core/services/plataforma.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Recomendacion, Asesor, Inmueble } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PlataformaService {
  private base = `${environment.apiUrl}/api/plataforma`;
  constructor(private http: HttpClient) {}

  generarRecomendaciones(idCliente: string): Observable<Recomendacion[]> {
    return this.http.get<Recomendacion[]>(`${this.base}/recomendaciones/${idCliente}`);
  }
  clientesConectadosAInmueble(codigo: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/grafo/clientes/${codigo}`);
  }
  inmueblesConectadosACliente(idCliente: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/grafo/inmuebles/${idCliente}`);
  }
  bfsDesdeNodo(nodo: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/grafo/bfs/${nodo}`);
  }
  rankingAsesores(): Observable<Asesor[]> {
    return this.http.get<Asesor[]>(`${this.base}/rankings/asesores`);
  }
  rankingZonas(): Observable<{ [zona: string]: number }> {
    return this.http.get<{ [zona: string]: number }>(`${this.base}/rankings/zonas`);
  }
}
