// src/app/core/services/operacion.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Operacion, OperacionDTO, TipoOperacion } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class OperacionService {
  private base = `${environment.apiUrl}/api`;
  constructor(private http: HttpClient) {}

  registrar(dto: OperacionDTO): Observable<Operacion> {
    return this.http.post<Operacion>(`${this.base}/plataforma/operaciones`, dto);
  }
  buscarPorId(id: string): Observable<Operacion> {
    return this.http.get<Operacion>(`${this.base}/operaciones/${id}`);
  }
  listar(filtros?: { tipo?: TipoOperacion; idCliente?: string; idAsesor?: string }): Observable<Operacion[]> {
    let params = new HttpParams();
    if (filtros?.tipo) params = params.set('tipo', filtros.tipo);
    if (filtros?.idCliente) params = params.set('idCliente', filtros.idCliente);
    if (filtros?.idAsesor) params = params.set('idAsesor', filtros.idAsesor);
    return this.http.get<Operacion[]>(`${this.base}/operaciones`, { params });
  }
  cancelar(id: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/operaciones/${id}/cancelar`, {});
  }
  cerrar(id: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/operaciones/${id}/cerrar`, {});
  }
  calcularComisiones(idAsesor: string): Observable<{ totalComisiones: number }> {
    return this.http.get<{ totalComisiones: number }>(`${this.base}/operaciones/comisiones/${idAsesor}`);
  }
}
