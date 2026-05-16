// src/app/core/services/cliente.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cliente, ClienteDTO, Inmueble } from '../models';
import { environment } from '../../../environments/environment';

export interface Interaccion {
  id: string;
  fecha: string;
  tipoInteraccion: string;
  detalle: string;
  inmueble?: Inmueble;
}

export interface IntencionDTO {
  codigoInmueble: string;
  tipo: 'INTENCION_COMPRA' | 'INTENCION_RENTA';
  detalle?: string;
}

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
  eliminarFavorito(id: string, codigoInmueble: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/clientes/${id}/favoritos/${codigoInmueble}`);
  }

  // ── Interacciones ─────────────────────────────────────────────────
  obtenerInteracciones(id: string, tipo?: string): Observable<Interaccion[]> {
    let params = new HttpParams();
    if (tipo) params = params.set('tipo', tipo);
    return this.http.get<Interaccion[]>(`${this.base}/clientes/${id}/interacciones`, { params });
  }

  registrarIntencion(id: string, dto: IntencionDTO): Observable<Interaccion> {
    return this.http.post<Interaccion>(`${this.base}/clientes/${id}/intencion`, dto);
  }

  agendarVisitaDesdePortal(id: string, dto: any): Observable<Interaccion> {
    return this.http.post<Interaccion>(`${this.base}/clientes/${id}/visitas`, dto);
  }

  /**
   * Registra que el cliente consultó (vio) un inmueble.
   * Usa el endpoint POST /api/clientes/{id}/consulta/{codigoInmueble}
   * que llama a clienteService.registrarInmuebleConsultado en el backend.
   */
  registrarConsultaInmueble(id: string, codigoInmueble: string): Observable<Interaccion> {
    return this.http.post<Interaccion>(
        `${this.base}/clientes/${id}/consulta/${codigoInmueble}`,
        {}
    );
  }
}