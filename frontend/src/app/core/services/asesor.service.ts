// src/app/core/services/asesor.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Asesor, AsesorDTO } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AsesorService {
  private base = `${environment.apiUrl}/api`;
  constructor(private http: HttpClient) {}

  registrar(dto: AsesorDTO): Observable<Asesor> {
    return this.http.post<Asesor>(`${this.base}/plataforma/asesores`, dto);
  }
  buscarPorId(id: string): Observable<Asesor> {
    return this.http.get<Asesor>(`${this.base}/asesores/${id}`);
  }
  listar(ranking = false): Observable<Asesor[]> {
    let params = new HttpParams();
    if (ranking) params = params.set('ranking', true);
    return this.http.get<Asesor[]>(`${this.base}/asesores`, { params });
  }
  actualizar(id: string, dto: AsesorDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/asesores/${id}`, dto);
  }
  obtenerCarga(id: string): Observable<{ cargaTotal: number }> {
    return this.http.get<{ cargaTotal: number }>(`${this.base}/asesores/${id}/carga`);
  }
  rankingPorCierres(): Observable<Asesor[]> {
    return this.http.get<Asesor[]>(`${this.base}/plataforma/rankings/asesores`);
  }
}
