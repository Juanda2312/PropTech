// src/app/core/services/cliente.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cliente, ClienteDTO, Inmueble } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private base = `${environment.apiUrl}/api`;
  constructor(private http: HttpClient) {}

  registrar(dto: ClienteDTO): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.base}/plataforma/clientes`, dto);
  }
  buscarPorId(id: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.base}/clientes/${id}`);
  }
  listar(ordenarPorPresupuesto = false, presupuestoMax = 0): Observable<Cliente[]> {
    let params = new HttpParams();
    if (ordenarPorPresupuesto) params = params.set('ordenarPorPresupuesto', true);
    if (presupuestoMax > 0) params = params.set('presupuestoMax', presupuestoMax);
    return this.http.get<Cliente[]>(`${this.base}/clientes`, { params });
  }
  actualizar(id: string, dto: ClienteDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/clientes/${id}`, dto);
  }
  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/clientes/${id}`);
  }
  marcarFavorito(id: string, codigoInmueble: string): Observable<void> {
    return this.http.post<void>(`${this.base}/clientes/${id}/favoritos/${codigoInmueble}`, {});
  }
  descartarInmueble(id: string, codigoInmueble: string): Observable<void> {
    return this.http.post<void>(`${this.base}/clientes/${id}/descartados/${codigoInmueble}`, {});
  }
  obtenerFavoritos(id: string): Observable<Inmueble[]> {
    return this.http.get<Inmueble[]>(`${this.base}/clientes/${id}/favoritos`);
  }
  obtenerHistorial(id: string): Observable<Inmueble[]> {
    return this.http.get<Inmueble[]>(`${this.base}/clientes/${id}/historial`);
  }
}
