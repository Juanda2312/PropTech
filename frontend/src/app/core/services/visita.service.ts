// src/app/core/services/visita.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Visita, VisitaDTO, EstadoVisita } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class VisitaService {
  private base = `${environment.apiUrl}/api`;
  constructor(private http: HttpClient) {}

  programar(dto: VisitaDTO): Observable<Visita> {
    return this.http.post<Visita>(`${this.base}/plataforma/visitas`, dto);
  }
  buscarPorId(id: string): Observable<Visita> {
    return this.http.get<Visita>(`${this.base}/visitas/${id}`);
  }
  listar(filtros?: { estado?: EstadoVisita; idCliente?: string; codigoInmueble?: string }): Observable<Visita[]> {
    let params = new HttpParams();
    if (filtros?.estado) params = params.set('estado', filtros.estado);
    if (filtros?.idCliente) params = params.set('idCliente', filtros.idCliente);
    if (filtros?.codigoInmueble) params = params.set('codigoInmueble', filtros.codigoInmueble);
    return this.http.get<Visita[]>(`${this.base}/visitas`, { params });
  }
  confirmar(id: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/visitas/${id}/confirmar`, {});
  }
  cancelar(id: string, observaciones: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/visitas/${id}/cancelar`, { observaciones });
  }
  reprogramar(id: string, dto: VisitaDTO): Observable<void> {
    return this.http.patch<void>(`${this.base}/visitas/${id}/reprogramar`, dto);
  }
  marcarRealizada(id: string, observaciones: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/visitas/${id}/realizar`, { observaciones });
  }
  procesarSiguiente(): Observable<Visita> {
    return this.http.post<Visita>(`${this.base}/visitas/pendientes/procesar`, {});
  }
  totalPendientes(): Observable<{ totalPendientes: number }> {
    return this.http.get<{ totalPendientes: number }>(`${this.base}/visitas/pendientes/total`);
  }
  procesarVIP(): Observable<Visita> {
    return this.http.post<Visita>(`${this.base}/plataforma/visitas/vip/procesar`, {});
  }
}
