// src/app/core/services/alerta.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Alerta, NivelAtencion } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AlertaService {
  private base = `${environment.apiUrl}/api`;
  constructor(private http: HttpClient) {}

  listar(filtros?: { nivel?: NivelAtencion; abiertas?: boolean }): Observable<Alerta[]> {
    let params = new HttpParams();
    if (filtros?.nivel) params = params.set('nivel', filtros.nivel);
    if (filtros?.abiertas !== undefined) params = params.set('abiertas', filtros.abiertas);
    return this.http.get<Alerta[]>(`${this.base}/alertas`, { params });
  }
  cerrar(id: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/alertas/${id}/cerrar`, {});
  }
  procesarSiguiente(): Observable<Alerta> {
    return this.http.post<Alerta>(`${this.base}/alertas/pendientes/procesar`, {});
  }
  totalPendientes(): Observable<{ totalPendientes: number }> {
    return this.http.get<{ totalPendientes: number }>(`${this.base}/alertas/pendientes/total`);
  }
  generarAlertas(): Observable<Alerta[]> {
    return this.http.post<Alerta[]>(`${this.base}/plataforma/alertas/generar`, {});
  }
}
