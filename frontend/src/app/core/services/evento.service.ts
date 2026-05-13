// src/app/core/services/evento.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EventoInusual, EventoInusualDTO, NivelAtencion } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EventoService {
  private base = `${environment.apiUrl}/api`;
  constructor(private http: HttpClient) {}

  registrar(dto: EventoInusualDTO): Observable<EventoInusual> {
    return this.http.post<EventoInusual>(`${this.base}/eventos`, dto);
  }
  listar(filtros?: { nivel?: NivelAtencion; activos?: boolean }): Observable<EventoInusual[]> {
    let params = new HttpParams();
    if (filtros?.nivel) params = params.set('nivel', filtros.nivel);
    if (filtros?.activos !== undefined) params = params.set('activos', filtros.activos);
    return this.http.get<EventoInusual[]>(`${this.base}/eventos`, { params });
  }
  cerrar(id: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/eventos/${id}/cerrar`, {});
  }
  detectarComportamientos(): Observable<void> {
    return this.http.post<void>(`${this.base}/plataforma/eventos/detectar`, {});
  }
}
