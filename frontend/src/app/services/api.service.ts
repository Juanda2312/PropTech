import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = 'http://localhost:4200/api';

  constructor(private http: HttpClient) {}

  // Inmuebles
  getInmuebles(params?: any): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/inmuebles`, { params });
  }
  crearInmueble(data: any): Observable<any> {
    return this.http.post<any>(`${this.base}/plataforma/inmuebles`, data);
  }
  actualizarInmueble(codigo: string, data: any): Observable<any> {
    return this.http.put<any>(`${this.base}/inmuebles/${codigo}`, data);
  }
  eliminarInmueble(codigo: string): Observable<any> {
    return this.http.delete<any>(`${this.base}/inmuebles/${codigo}`);
  }

  // Clientes
  getClientes(params?: any): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/clientes`, { params });
  }
  crearCliente(data: any): Observable<any> {
    return this.http.post<any>(`${this.base}/plataforma/clientes`, data);
  }
  actualizarCliente(id: string, data: any): Observable<any> {
    return this.http.put<any>(`${this.base}/clientes/${id}`, data);
  }
  eliminarCliente(id: string): Observable<any> {
    return this.http.delete<any>(`${this.base}/clientes/${id}`);
  }

  // Asesores
  getAsesores(params?: any): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/asesores`, { params });
  }
  crearAsesor(data: any): Observable<any> {
    return this.http.post<any>(`${this.base}/plataforma/asesores`, data);
  }
  actualizarAsesor(id: string, data: any): Observable<any> {
    return this.http.put<any>(`${this.base}/asesores/${id}`, data);
  }

  // Visitas
  getVisitas(params?: any): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/visitas`, { params });
  }
  crearVisita(data: any): Observable<any> {
    return this.http.post<any>(`${this.base}/plataforma/visitas`, data);
  }
  confirmarVisita(id: string): Observable<any> {
    return this.http.patch<any>(`${this.base}/visitas/${id}/confirmar`, {});
  }
  cancelarVisita(id: string, obs: string): Observable<any> {
    return this.http.patch<any>(`${this.base}/visitas/${id}/cancelar`, { observaciones: obs });
  }
  reprogramarVisita(id: string, data: any): Observable<any> {
    return this.http.patch<any>(`${this.base}/visitas/${id}/reprogramar`, data);
  }

  // Operaciones
  getOperaciones(params?: any): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/operaciones`, { params });
  }
  crearOperacion(data: any): Observable<any> {
    return this.http.post<any>(`${this.base}/plataforma/operaciones`, data);
  }
  cancelarOperacion(id: string): Observable<any> {
    return this.http.patch<any>(`${this.base}/operaciones/${id}/cancelar`, {});
  }

  // Alertas
  getAlertas(params?: any): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/alertas`, { params });
  }
  cerrarAlerta(id: string): Observable<any> {
    return this.http.patch<any>(`${this.base}/alertas/${id}/cerrar`, {});
  }
  generarAlertas(): Observable<any[]> {
    return this.http.post<any[]>(`${this.base}/plataforma/alertas/generar`, {});
  }

  // Eventos inusuales
  getEventos(params?: any): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/eventos`, { params });
  }
  detectarComportamientos(): Observable<any> {
    return this.http.post<any>(`${this.base}/plataforma/eventos/detectar`, {});
  }

  // Recomendaciones y rankings
  getRecomendaciones(idCliente: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/plataforma/recomendaciones/${idCliente}`);
  }
  getRankingAsesores(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/plataforma/rankings/asesores`);
  }
  getRankingZonas(): Observable<any> {
    return this.http.get<any>(`${this.base}/plataforma/rankings/zonas`);
  }
}
