# PropTech — Plataforma Beta de Gestión Inmobiliaria Inteligente

> Sistema de gestión inmobiliaria digital que integra estructuras de datos avanzadas para administrar inmuebles, clientes, asesores, visitas, operaciones y análisis de comportamiento comercial.

---

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Arquitectura del sistema](#arquitectura-del-sistema)
- [Estructuras de datos utilizadas](#estructuras-de-datos-utilizadas)
- [Funcionalidades principales](#funcionalidades-principales)
- [Tecnologías](#tecnologías)
- [Requisitos previos](#requisitos-previos)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Endpoints de la API REST](#endpoints-de-la-api-rest)
- [Diagrama de entidades](#diagrama-de-entidades)
- [Datos de prueba](#datos-de-prueba)
- [Estructura del proyecto](#estructura-del-proyecto)

---

## Descripción general

PropTech Beta es una plataforma inmobiliaria que simula el funcionamiento de una inmobiliaria moderna. No se limita al almacenamiento de propiedades: gestiona procesos inteligentes como filtrado avanzado, historial de interés de clientes, programación de visitas, seguimiento de negociaciones, alertas automáticas, recomendaciones de inmuebles y análisis de relaciones mediante grafos.

El propósito del proyecto es aplicar de manera justificada diferentes **estructuras de datos** para resolver necesidades reales de organización, búsqueda, priorización, recorrido, clasificación y análisis dentro del contexto inmobiliario.

---

## Arquitectura del sistema

El proyecto sigue una arquitectura **cliente-servidor** en capas:

```
┌──────────────────────────────────────┐
│         Frontend (Angular 17)        │
│   Dashboard · Módulos · Reportes     │
└────────────────┬─────────────────────┘
                 │ HTTP / REST (proxy /api → :8080)
┌────────────────▼─────────────────────┐
│         Backend (Spring Boot 3)      │
│  Controllers → Services → Repository │
│       Estructuras de datos propias   │
└──────────────────────────────────────┘
```

### Backend

- **Controllers:** exponen la API REST bajo `/api/**` y `/api/plataforma/**`.
- **Services:** contienen la lógica de negocio. `PlataformaBeta` es el orquestador principal.
- **Repositories:** encapsulan las estructuras de datos (HashTable, AVLTree, Stack, Queue, PriorityQueue).
- **Structures:** implementaciones propias de cada estructura de datos.
- **Exceptions:** jerarquía de excepciones de dominio con manejo global vía `GlobalExceptionHandler`.

### Frontend

- Aplicación Angular 17 con componentes standalone.
- Módulos: Dashboard, Inmuebles, Clientes, Asesores, Visitas, Operaciones, Alertas, Eventos Inusuales, Recomendaciones, Análisis de Grafo.
- Comunicación con el backend mediante servicios HTTP con proxy de desarrollo.

---

## Estructuras de datos utilizadas

Todas las estructuras son **implementaciones propias** (sin usar `java.util` para las estructuras principales).

| Estructura | Clase | Uso en el sistema |
|---|---|---|
| **Lista enlazada simple** | `SimpleLinkedList<T>` | Historial de visitas, inmuebles consultados, favoritos, inmuebles asignados a asesores, contratos y operaciones |
| **Pila (Stack)** | `Stack<T>` | Deshacer cambios recientes en publicaciones de inmuebles; historial de snapshots para revertir modificaciones |
| **Cola FIFO (Queue)** | `Queue<T>` | Solicitudes de visitas pendientes por procesar; cola de alertas pendientes de revisión |
| **Cola de prioridad** | `PriorityQueue<T>` | Visitas urgentes o VIP; alertas con mayor nivel de atención (heap máximo, extracción en O(log n)) |
| **Tabla hash** | `HashTable<K,V>` | Búsqueda O(1) de clientes por ID, inmuebles por código, asesores, alertas y eventos; conteo de frecuencias |
| **Árbol AVL** | `AVLTree<T>` | Ordenamiento de inmuebles por precio; clasificación de clientes por presupuesto; ranking de asesores por cierres |
| **Grafo** | `Graph<T>` | Relaciones entre clientes e inmuebles visitados; análisis de conexiones mediante BFS |

### Justificación técnica destacada

- **HashTable:** acceso O(1) para las búsquedas más frecuentes del sistema (cliente por ID, inmueble por código).
- **AVLTree:** garantiza orden y balanceo automático para consultas por rango de precio y rankings; recorrido inOrder produce la lista ordenada sin costo adicional.
- **Stack:** permite deshacer el último cambio sobre cualquier inmueble restaurando el snapshot previo.
- **PriorityQueue (heap máximo):** las visitas VIP y las alertas críticas se extraen siempre primero, independientemente del orden de llegada.
- **Graph (no dirigido):** representa la relación bidireccional cliente↔inmueble generada con cada visita; el BFS permite descubrir todos los nodos conectados desde cualquier punto.

---

## Funcionalidades principales

### Gestión de inmuebles
- Registro, edición y eliminación de inmuebles con más de 12 atributos.
- Filtrado combinado por tipo, finalidad, ciudad, precio y habitaciones.
- Ordenamiento por precio (recorrido inOrder del AVL).
- Sugerencia de inmuebles similares (mismo tipo, finalidad y precio ±20%).
- Deshacer el último cambio sobre un inmueble (Stack).

### Gestión de clientes
- CRUD completo con validación de duplicados.
- Historial de inmuebles consultados, visitados, favoritos, descartados y negociados.
- Ordenamiento por presupuesto (AVL).
- Recomendaciones personalizadas por puntaje de coincidencia.

### Gestión de asesores
- Registro y actualización de asesores con zona de especialidad.
- Cálculo de carga actual (visitas + inmuebles asignados).
- Ranking por número de cierres realizados (AVL descendente).

### Programación de visitas
- Estados: `PENDIENTE → CONFIRMADA → REALIZADA` o `CANCELADA / REPROGRAMADA`.
- Cola FIFO de pendientes: procesa visitas en orden de llegada.
- Cola de prioridad VIP: procesa la visita más urgente primero.

### Alertas automáticas
- Inmuebles sin visitas (inactivos).
- Visitas pendientes por más de 3 días sin confirmar.
- Clientes activos sin ninguna interacción registrada.
- Alertas gestionadas con cola FIFO y cola de prioridad.

### Detección de comportamientos inusuales
- Clientes con exceso de visitas sin cierre (> 10).
- Asesores con sobrecarga de atención (carga > 15).
- Inmuebles con alta demanda sin cierre (> 20 visitas).
- Inmuebles con cambios de precio frecuentes (> 3 veces).
- Concentración inusual de visitas en una misma zona (> 15).

### Recomendaciones inteligentes
Puntaje calculado por tres criterios (máx. 100 pts):
- Precio dentro del presupuesto del cliente: **+40 pts**
- Tipo de inmueble coincide con el deseado: **+30 pts**
- Habitaciones ≥ mínimo requerido: **+30 pts**

### Análisis de grafo
- Obtener clientes conectados a un inmueble (vecinos del grafo).
- Obtener inmuebles visitados por un cliente (vecinos del grafo).
- Recorrido BFS desde cualquier nodo (cliente o inmueble).
- Ranking de zonas por actividad (conteo de visitas por barrio).

### Operaciones de negocio
- Registro de arriendo, venta, renovación y cancelación.
- Cierre y cancelación de operaciones con validación de estado.
- Cálculo de comisiones por asesor.

---

## Tecnologías

### Backend
| Tecnología | Versión | Rol |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.5.x | Framework web y DI |
| Spring Web MVC | — | API REST |
| Lombok | — | Reducción de boilerplate |
| SpringDoc OpenAPI | 2.5.0 | Documentación Swagger UI |
| Jackson JSR310 | — | Serialización de fechas ISO-8601 |
| Maven | 3.9.x | Gestión de dependencias |

### Frontend
| Tecnología | Versión | Rol |
|---|---|---|
| Angular | 17 | Framework SPA |
| TypeScript | ~5.2 | Lenguaje del frontend |
| Angular CDK | 17 | Componentes base |
| RxJS | ~7.8 | Programación reactiva |
| SCSS | — | Estilos |
| Node.js | ≥ 18 | Entorno de ejecución |

---

## Requisitos previos

- **Java 21** o superior
- **Maven 3.9** o superior (o usar el wrapper `./mvnw`)
- **Node.js 18** o superior
- **Angular CLI 17** (`npm install -g @angular/cli@17`)

---

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd <nombre-del-proyecto>
```

### 2. Ejecutar el backend

```bash
# Desde la raíz del proyecto
./mvnw spring-boot:run
```

El servidor arranca en `http://localhost:8080`.  
Al iniciar, el `DataLoader` carga automáticamente **5 asesores, 10 clientes, 20 inmuebles, 20 visitas y 8 operaciones** de prueba.

La documentación Swagger UI estará disponible en:
```
http://localhost:8080/swagger-ui/index.html
```

### 3. Ejecutar el frontend

```bash
cd frontend
npm install
npm start
```

La aplicación Angular quedará disponible en `http://localhost:4200`.  
El proxy redirige automáticamente `/api/**` → `http://localhost:8080`.

---

## Endpoints de la API REST

### Inmuebles — `/api/inmuebles` y `/api/plataforma/inmuebles`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/plataforma/inmuebles` | Registrar inmueble (agrega al grafo) |
| `GET` | `/api/inmuebles` | Listar con filtros opcionales |
| `GET` | `/api/inmuebles/{codigo}` | Buscar por código |
| `PUT` | `/api/inmuebles/{codigo}` | Actualizar |
| `DELETE` | `/api/inmuebles/{codigo}` | Eliminar |
| `POST` | `/api/inmuebles/deshacer` | Deshacer último cambio (Stack) |
| `GET` | `/api/plataforma/inmuebles/ordenados` | Ordenados por precio (AVL inOrder) |
| `GET` | `/api/plataforma/inmuebles/buscar` | Filtro combinado multi-criterio |
| `GET` | `/api/plataforma/similares/{codigo}` | Sugerir inmuebles similares |

**Parámetros de filtro disponibles:** `tipo`, `finalidad`, `ciudad`, `disponible`, `precioMin`, `precioMax`, `habitacionesMin`

### Clientes — `/api/clientes` y `/api/plataforma/clientes`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/plataforma/clientes` | Registrar cliente (agrega al grafo) |
| `GET` | `/api/clientes` | Listar (`?ordenarPorPresupuesto=true`) |
| `GET` | `/api/clientes/{id}` | Buscar por ID |
| `PUT` | `/api/clientes/{id}` | Actualizar |
| `DELETE` | `/api/clientes/{id}` | Eliminar |
| `POST` | `/api/clientes/{id}/favoritos/{codigo}` | Marcar favorito |
| `POST` | `/api/clientes/{id}/descartados/{codigo}` | Registrar descarte |
| `GET` | `/api/clientes/{id}/favoritos` | Obtener favoritos |
| `GET` | `/api/clientes/{id}/historial` | Obtener historial de consultas |

### Asesores — `/api/asesores`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/plataforma/asesores` | Registrar asesor |
| `GET` | `/api/asesores` | Listar (`?ranking=true` para ranking por cierres) |
| `GET` | `/api/asesores/{id}` | Buscar por ID |
| `PUT` | `/api/asesores/{id}` | Actualizar |
| `GET` | `/api/asesores/{id}/carga` | Carga total del asesor |

### Visitas — `/api/visitas` y `/api/plataforma/visitas`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/plataforma/visitas` | Programar visita (actualiza grafo) |
| `GET` | `/api/visitas` | Listar (`?estado=`, `?idCliente=`, `?codigoInmueble=`) |
| `PATCH` | `/api/visitas/{id}/confirmar` | Confirmar visita |
| `PATCH` | `/api/visitas/{id}/cancelar` | Cancelar visita |
| `PATCH` | `/api/visitas/{id}/reprogramar` | Reprogramar visita |
| `PATCH` | `/api/visitas/{id}/realizar` | Marcar como realizada |
| `POST` | `/api/visitas/pendientes/procesar` | Procesar siguiente (Cola FIFO) |
| `GET` | `/api/visitas/pendientes/total` | Total en cola pendiente |
| `POST` | `/api/plataforma/visitas/vip/procesar` | Procesar VIP (Cola de prioridad) |

### Operaciones — `/api/operaciones`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/plataforma/operaciones` | Registrar operación |
| `GET` | `/api/operaciones` | Listar (`?tipo=`, `?idCliente=`, `?idAsesor=`) |
| `GET` | `/api/operaciones/{id}` | Buscar por ID |
| `PATCH` | `/api/operaciones/{id}/cancelar` | Cancelar |
| `PATCH` | `/api/operaciones/{id}/cerrar` | Cerrar |
| `GET` | `/api/operaciones/comisiones/{idAsesor}` | Total comisiones de un asesor |

### Alertas — `/api/alertas`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/plataforma/alertas/generar` | Generar alertas automáticas |
| `GET` | `/api/alertas` | Listar (`?nivel=`, `?abiertas=true`) |
| `PATCH` | `/api/alertas/{id}/cerrar` | Cerrar alerta |
| `POST` | `/api/alertas/pendientes/procesar` | Procesar siguiente (Cola FIFO) |
| `GET` | `/api/alertas/pendientes/total` | Total pendientes |

### Eventos Inusuales — `/api/eventos`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/plataforma/eventos/detectar` | Ejecutar detección automática |
| `POST` | `/api/eventos` | Registrar evento manualmente |
| `GET` | `/api/eventos` | Listar (`?nivel=`, `?activos=true`) |
| `PATCH` | `/api/eventos/{id}/cerrar` | Cerrar evento |

### Análisis y Rankings — `/api/plataforma`

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/plataforma/recomendaciones/{idCliente}` | Recomendaciones por puntaje |
| `GET` | `/api/plataforma/grafo/clientes/{codigo}` | Clientes conectados a un inmueble |
| `GET` | `/api/plataforma/grafo/inmuebles/{idCliente}` | Inmuebles conectados a un cliente |
| `GET` | `/api/plataforma/grafo/bfs/{nodo}` | Recorrido BFS desde un nodo |
| `GET` | `/api/plataforma/rankings/asesores` | Ranking de asesores por cierres |
| `GET` | `/api/plataforma/rankings/zonas` | Actividad por zona/barrio |

---

## Diagrama de entidades

```
Persona (abstract)
 ├── Cliente
 │     ├── inmueblesConsultados  : SimpleLinkedList<Inmueble>
 │     ├── propiedadesVisitadas  : SimpleLinkedList<Inmueble>
 │     ├── inmueblesGuardados    : SimpleLinkedList<Inmueble>
 │     ├── inmueblesDescartados  : SimpleLinkedList<Inmueble>
 │     └── listaRecomendaciones  : SimpleLinkedList<Recomendacion>
 └── Asesor
       ├── inmueblesAsignados    : SimpleLinkedList<Inmueble>
       ├── visitasAgendadas      : SimpleLinkedList<Visita>
       └── cierresRealizados     : SimpleLinkedList<Operacion>

Inmueble  ──── Visita ──── Cliente
   │                          │
   └──── Operacion ───────────┘
              │
           Asesor

Alerta        EventoInusual        Recomendacion
```

---

## Datos de prueba

Al iniciar la aplicación, el `DataLoader` inserta automáticamente:

| Entidad | Cantidad |
|---|---|
| Asesores | 5 |
| Clientes | 10 |
| Inmuebles | 20 (Medellín, Bogotá, Cali) |
| Visitas | 20 (pasadas, presentes y futuras) |
| Operaciones | 8 |

Los IDs siguen el patrón `ASR-00X`, `CLI-00X`, `INM-00X`, `VIS-00X`, `OP-00X`.

---

## Estructura del proyecto

```
├── pom.xml                          # Dependencias Maven (Java 21 + Spring Boot 3)
├── mvnw / mvnw.cmd                  # Maven Wrapper
├── src/
│   └── main/java/.../PropTech/
│       ├── config/
│       │   ├── DataLoader.java      # Carga datos de prueba al iniciar
│       │   ├── JacksonConfig.java   # Fechas ISO-8601
│       │   ├── OpenApiConfig.java   # Swagger UI
│       │   └── WebConfig.java       # CORS
│       ├── controller/              # Endpoints REST
│       ├── exception/               # Jerarquía de excepciones + handler global
│       ├── model/
│       │   ├── dto/                 # DTOs de entrada
│       │   ├── entity/              # Entidades del dominio
│       │   └── enums/               # Tipos enumerados
│       ├── repository/              # Repositorios con estructuras de datos
│       ├── service/                 # Lógica de negocio
│       │   └── PlataformaBeta.java  # Orquestador principal
│       ├── structures/              # Implementaciones propias
│       │   ├── AVLTree.java
│       │   ├── Graph.java
│       │   ├── HashTable.java
│       │   ├── Node.java
│       │   ├── PriorityQueue.java
│       │   ├── Queue.java
│       │   ├── SimpleLinkedList.java
│       │   └── Stack.java
│       └── util/
│           └── ApiError.java        # Respuesta estándar de error
└── frontend/
    ├── src/app/
    │   ├── core/
    │   │   ├── models/              # Interfaces TypeScript
    │   │   ├── services/            # Servicios HTTP (uno por entidad)
    │   │   └── interceptors/        # Error interceptor global
    │   └── features/                # Componentes por módulo
    │       ├── dashboard/
    │       ├── inmuebles/
    │       ├── clientes/
    │       ├── asesores/
    │       ├── visitas/
    │       ├── operaciones/
    │       ├── alertas/
    │       ├── eventos/
    │       ├── recomendaciones/
    │       └── grafo/
    ├── proxy.conf.json              # Proxy /api → localhost:8080
    └── angular.json
```

---

## Manejo de errores

El `GlobalExceptionHandler` mapea las excepciones del dominio a códigos HTTP estándar:

| Excepción | HTTP |
|---|---|
| `EntidadNoEncontradaException` | 404 Not Found |
| `EntidadDuplicadaException` | 409 Conflict |
| `InmuebleNoDisponibleException` | 409 Conflict |
| `EstadoInvalidoException` | 422 Unprocessable Entity |
| `ReglaNegocioException` | 400 Bad Request |
| `Exception` (genérico) | 500 Internal Server Error |

Todas las respuestas de error siguen el formato:

```json
{
  "timestamp": "2025-05-06T14:32:00",
  "status": 404,
  "error": "Not Found",
  "message": "Inmueble no encontrado/a con id: INM-999",
  "path": "/api/inmuebles/INM-999"
}
```

---

*Universidad del Quindío — Programación Orientada a Objetos*
