// src/app/core/models/index.ts

export type TipoInmueble = 'APARTAMENTO' | 'CASA' | 'LOCAL_COMERCIAL' | 'OFICINA' | 'LOTE' | 'BODEGA';
export type FinalidadInmueble = 'VENTA' | 'ARRIENDO';
export type EstadoVisita = 'PENDIENTE' | 'CONFIRMADA' | 'REALIZADA' | 'CANCELADA' | 'REPROGRAMADA';
export type TipoOperacion = 'ARRIENDO' | 'VENTA' | 'RENOVACION' | 'CANCELACION';
export type NivelAtencion = 'BAJO' | 'MEDIO' | 'ALTO' | 'CRITICO';
export type EstadoBusqueda = 'ACTIVO' | 'EN_PAUSA' | 'CERRADO';
export type EstadoEvento = 'ACTIVO' | 'EN_PAUSA' | 'CERRADO';
export type Zona = 'NORTE' | 'SUR' | 'ESTE' | 'OESTE';

export interface Persona {
  id: string;
  nombre: string;
}

export interface Asesor extends Persona {
  contacto: string;
  especialidadZona: string;
}

export interface AsesorDTO {
  id: string;
  nombre: string;
  contacto: string;
  especialidadZona: string;
}

export interface Cliente extends Persona {
  correo: string;
  telefono: string;
  tipoCliente: string;
  presupuesto: number;
  zonasInteres: Zona[];
  tipoInmuebleDeseado: TipoInmueble;
  habitacionesMinimas: number;
  estadoBusqueda: EstadoBusqueda;
}

export interface ClienteDTO {
  id: string;
  nombre: string;
  correo: string;
  telefono: string;
  tipoCliente: string;
  presupuesto: number;
  zonasInteres: Zona[];
  tipoInmuebleDeseado: TipoInmueble;
  habitacionesMinimas: number;
  estadoBusqueda: EstadoBusqueda;
}

export interface Inmueble {
  codigo: string;
  direccion: string;
  ciudad: string;
  barrio: string;
  tipoInmueble: TipoInmueble;
  finalidad: FinalidadInmueble;
  precio: number;
  area: number;
  habitaciones: number;
  banos: number;
  estado: string;
  disponibilidad: boolean;
  asesor: Asesor;
}

export interface InmuebleDTO {
  codigo: string;
  direccion: string;
  ciudad: string;
  barrio: string;
  tipoInmueble: TipoInmueble;
  finalidad: FinalidadInmueble;
  precio: number;
  area: number;
  habitaciones: number;
  banos: number;
  estado: string;
  disponibilidad: boolean;
  idAsesor: string;
}

export interface Visita {
  idVisita: string;
  cliente: Cliente;
  inmueble: Inmueble;
  fecha: string;
  hora: string;
  asesor: Asesor;
  estado: EstadoVisita;
  observaciones: string;
}

export interface VisitaDTO {
  idVisita: string;
  idCliente: string;
  codigoInmueble: string;
  fecha: string;
  hora: string;
  idAsesor: string;
  estado?: EstadoVisita;
  observaciones?: string;
}

export interface Operacion {
  idOperacion: string;
  inmueble: Inmueble;
  cliente: Cliente;
  asesor: Asesor;
  fecha: string;
  tipoOperacion: TipoOperacion;
  valorAcordado: number;
  comision: number;
  estadoProceso: string;
}

export interface OperacionDTO {
  idOperacion: string;
  codigoInmueble: string;
  idCliente: string;
  idAsesor: string;
  fecha: string;
  tipoOperacion: TipoOperacion;
  valorAcordado: number;
  comision: number;
  estadoProceso: string;
}

export interface Alerta {
  idAlerta: string;
  tipoAlerta: string;
  descripcion: string;
  fechaGeneracion: string;
  nivel: NivelAtencion;
  cerrada: boolean;
}

export interface EventoInusual {
  idEvento: string;
  tipoEvento: string;
  descripcion: string;
  fechaDeteccion: string;
  nivelAtencion: NivelAtencion;
  estadoEvento: EstadoEvento;
}

export interface EventoInusualDTO {
  idEvento: string;
  tipoEvento: string;
  descripcion: string;
  nivelAtencion: NivelAtencion;
}

export interface Recomendacion {
  idRecomendacion: string;
  inmueble: Inmueble;
  puntaje: number;
  criterio: string;
  fechaGeneracion: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
