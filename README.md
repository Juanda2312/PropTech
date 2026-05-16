# PropTech — Plataforma Beta de Gestión Inmobiliaria Inteligente

> Sistema de gestión inmobiliaria digital que integra estructuras de datos avanzadas para administrar inmuebles, clientes, asesores, visitas, operaciones y análisis de comportamiento comercial.

---

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Arquitectura del sistema](#arquitectura-del-sistema)
- [Estructuras de datos utilizadas](#estructuras-de-datos-utilizadas)
- [Funcionalidades principales](#funcionalidades-principales)
- [Autenticación y roles](#autenticación-y-roles)
- [Portal del cliente](#portal-del-cliente)
- [Persistencia de datos](#persistencia-de-datos)
- [Tecnologías](#tecnologías)
- [Requisitos previos](#requisitos-previos)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Endpoints de la API REST](#endpoints-de-la-api-rest)
- [Diagrama de entidades](#diagrama-de-entidades)
- [Datos de prueba](#datos-de-prueba)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Manejo de errores](#manejo-de-errores)

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
│   Portal Cliente · Login             │
└────────────────┬─────────────────────┘
                 │ HTTP / REST (proxy /api → :8080)
┌────────────────▼─────────────────────┐
│         Backend (Spring Boot 3)      │
│  Controllers → Services → Repository │
│       Estructuras de datos propias   │
│       Persistencia JSON en /data/    │
└──────────────────────────────────────┘
```

### Backend

- **Controllers:** exponen la API REST bajo `/api/**` y `/api/plataforma/**`.
- **Services:** contienen la lógica de negocio. `PlataformaBeta` es el orquestador principal.
- **Repositories:** encapsulan las estructuras de datos (HashTable, AVLTree, Stack, Queue, PriorityQueue).
- **Structures:** implementaciones propias de cada estructura de datos.
- **Exceptions:** jerarquía de excepciones de dominio con manejo global vía `GlobalExceptionHandler`.
- **PersistenciaService:** guarda y restaura el estado completo en archivos JSON dentro de `/data/` al apagar y encender el servidor.

### Frontend

- Aplicación Angular 17 con componentes standalone.
- Módulos: Dashboard, Inmuebles, Clientes, Asesores, Visitas, Operaciones, Alertas, Eventos Inusuales, Recomendaciones, Análisis de Grafo.
- Portal del cliente con secciones propias: inmuebles disponibles, favoritos, historial, interacciones y recomendaciones.
- Sistema de autenticación con roles: ADMIN y CLIENTE.
- Comunicación con el backend mediante servicios HTTP con proxy de desarrollo.

---

## Estructuras de datos utilizadas

Todas las estructuras son **implementaciones propias** (sin usar `java.util` para las estructuras principales).

| Estructura | Clase | Uso en el sistema |
|---|---|---|
| **Lista enlazada simple** | `SimpleLinkedList<T>` | Historial de visitas, inmuebles consultados, favoritos, inmuebles asignados a asesores, contratos, operaciones, historial de interacciones del cliente |
| **Pila (Stack)** | `Stack<T>` | Deshacer cambios recientes en publicaciones de inmuebles; historial de snapshots para revertir modificaciones |
| **Cola FIFO (Queue)** | `Queue<T>` | Solicitudes de visitas pendientes por procesar; cola de alertas pendientes de revisión |
| **Cola de prioridad** | `PriorityQueue<T>` | Visitas urgentes o VIP; alertas con mayor nivel de atención (heap máximo, extracción en O(log n)) |
| **Tabla hash** | `HashTable<K,V>` | Búsqueda O(1) de clientes por ID, inmuebles por código, asesores, alertas y eventos; conteo de frecuencias |
| **Árbol AVL** | `AVLTree<T>` | Ordenamiento de inmuebles por precio; clasificación de clientes por presupuesto; ranking de asesores por cierres |
| **Grafo** | `Graph<T>` | Relaciones entre clientes e inmuebles visitados; análisis de conexiones mediante BFS y DFS |

### Justificación técnica destacada

- **HashTable:** acceso O(1) para las búsquedas más frecuentes del sistema (cliente por ID, inmueble por código).
- **AVLTree:** garantiza orden y balanceo automático para consultas por rango de precio y rankings; recorrido inOrder produce la lista ordenada sin costo adicional.
- **Stack:** permite deshacer el último cambio sobre cualquier inmueble restaurando el snapshot previo.
- **PriorityQueue (heap máximo):** las visitas VIP y las alertas críticas se extraen siempre primero, independientemente del orden de llegada.
- **Graph (no dirigido):** representa la relación bidireccional cliente↔inmueble generada con cada visita; el BFS permite descubrir todos los nodos conectados desde cualquier punto.
- **SimpleLinkedList:** permite inserción en O(1) al frente (`addFirst`) para mantener el historial de interacciones en orden cronológico inverso (más reciente primero).

---

## Funcionalidades principales

### Gestión de inmuebles
- Registro, edición y eliminación de inmuebles con más de 12 atributos.
- Filtrado combinado por tipo, finalidad, ciudad, precio y habitaciones.
- Ordenamiento por precio (recorrido inOrder del AVL).
- Sugerencia de inmuebles similares (mismo tipo, finalidad y precio ±20%).
- Deshacer el último cambio sobre un inmueble (Stack).

### Gestión de clientes
- CRUD completo con validación de duplicados y formato de cédula (10 dígitos).
- Historial de inmuebles consultados, visitados, favoritos, descartados y negociados.
- Historial unificado de interacciones (visitas agendadas, favoritos guardados, intenciones de compra/renta, compras/arriendos realizados, inmuebles consultados/descartados).
- Ordenamiento por presupuesto (AVL).
- Recomendaciones personalizadas por puntaje de coincidencia.

### Gestión de asesores
- Registro y actualización de asesores con zona de especialidad.
- Validación de ID como cédula colombiana (exactamente 10 dígitos).
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
- Contratos próximos a vencer (dentro de 30 días), con nivel según días restantes (≤7: CRÍTICO, ≤15: ALTO, ≤30: MEDIO).
- Inmuebles con alta demanda y sin cierre (> 10 visitas).
- Operaciones activas por más de 60 días sin cerrarse.
- Alertas gestionadas con cola FIFO y cola de prioridad (extrae primero la más crítica).

### Detección de comportamientos inusuales
- Clientes con exceso de visitas sin cierre (> 10).
- Asesores con sobrecarga de atención (carga > 15).
- Inmuebles con alta demanda sin cierre (> 20 visitas).
- Inmuebles con cambios de precio frecuentes (> 3 veces).
- Concentración inusual de visitas en una misma zona (> 15).

### Recomendaciones inteligentes
Puntaje calculado con seis criterios (máx. 100 pts):
- Precio dentro del presupuesto del cliente: **+40 pts**
- Tipo de inmueble coincide con el deseado: **+20 pts**
- Habitaciones ≥ mínimo requerido: **+15 pts**
- Zona de interés del cliente: **+15 pts**
- Barrio consultado anteriormente: **+5 pts**
- Visitó inmuebles similares en el mismo barrio: **+5 pts**

### Análisis de grafo
- Obtener clientes conectados a un inmueble (vecinos del grafo).
- Obtener inmuebles visitados por un cliente (vecinos del grafo).
- Recorrido BFS desde cualquier nodo (cliente o inmueble).
- Clientes con perfil similar (visitaron inmuebles en común — vecinos de vecinos).
- Ranking de zonas por actividad (conteo de visitas por barrio).

### Operaciones de negocio
- Registro de arriendo, venta, renovación y cancelación.
- Fecha de vencimiento de contrato para arriendos y renovaciones.
- Cierre y cancelación de operaciones con validación de estado.
- Cálculo de comisiones por asesor.

### Reportes avanzados
- Ranking de asesores por cierres (AVL descendente).
- Ranking de zonas por actividad comercial.
- Clientes con alta probabilidad de cierre (estado ACTIVO + favoritos + ≥2 visitas + inmueble acorde al presupuesto).
- Simulación de crecimiento de demanda por sector (últimos 30 días vs. 30 días anteriores, con tendencia CRECIENDO / ESTABLE / DECAYENDO).

---

## Autenticación y roles

El sistema cuenta con dos roles diferenciados:

### Administrador (ADMIN)
- Credenciales hardcodeadas en `AuthService`.
- Cuentas predefinidas:
  - `jose@gmail.com` / `1111100000`
  - `tapiero@gmail.com` / `0000011111`
- Acceso completo al panel de administración con todos los módulos.

### Cliente (CLIENTE)
- Puede registrarse desde la pantalla de login (crea cuenta en frontend y en backend simultáneamente).
- También puede iniciar sesión con las credenciales de los clientes cargados por el `DataLoader` (correo + ID de cédula).
- Al autenticarse, el sistema vincula automáticamente su cuenta frontend con su registro en el backend buscando por correo.
- Redirige al Portal del Cliente (`/cliente`).

El sistema usa `sessionStorage` para mantener la sesión activa y `localStorage` para cachear clientes registrados en el frontend.

---

## Portal del cliente

Interfaz dedicada accesible en `/cliente` para usuarios con rol CLIENTE. Cuenta con cinco secciones:

### Inmuebles disponibles
- Listado de todos los inmuebles con disponibilidad activa.
- Búsqueda en tiempo real por ciudad, barrio, tipo o dirección.
- Al pasar el cursor (hover) sobre una tarjeta se registra automáticamente la consulta en el historial del cliente (una sola vez por sesión para evitar duplicados).
- Acciones por tarjeta: guardar/quitar favorito, agendar visita, declarar intención de compra o renta.

### Favoritos
- Lista de inmuebles guardados por el cliente.
- Acciones directas: agendar visita, declarar intención.

### Consultados
- Historial de inmuebles con los que el cliente interactuó.

### Mis Interacciones
- Historial unificado de todas las acciones del cliente ordenadas de más reciente a más antigua.
- Filtrable por tipo: visitas agendadas, favoritos, intenciones de compra/renta, compras/arriendos realizados, inmuebles consultados/descartados.
- Formulario para agendar visita directamente desde esta sección.
- Formulario para declarar intención de compra o renta.

### Recomendaciones
- Inmuebles recomendados personalizados con puntaje de coincidencia (0-100).
- Indicador visual circular por cada inmueble con color según nivel de coincidencia.
- Razones explícitas del porqué se recomienda cada inmueble.
- Acciones directas: guardar favorito, agendar visita, declarar intención.

---

## Persistencia de datos

El sistema persiste automáticamente todos los datos en archivos JSON dentro del directorio `/data/` al apagar el servidor (`@PreDestroy`), y los restaura al volver a arrancar.

Archivos generados:
```
data/
├── asesores.json
├── clientes.json
├── inmuebles.json
├── visitas.json
├── operaciones.json
├── alertas.json
├── eventos.json
└── interacciones.json
```

El archivo `interacciones.json` usa un formato propio (`InteraccionPersistida`) que almacena el ID del cliente y el código del inmueble en lugar de los objetos completos, evitando referencias circulares y problemas con `@JsonIgnore`.

Al restaurar, el `DataLoader` reconstruye el grafo de relaciones cliente↔inmueble y las listas específicas (`inmueblesConsultados`, `propiedadesVisitadas`, `inmueblesGuardados`, etc.) a partir del tipo de cada interacción. Los favoritos también se restauran desde el campo `codigosFavoritos` del cliente.

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

**Primera ejecución:** el `DataLoader` carga automáticamente datos de prueba (5 asesores, 10 clientes, 20 inmuebles, 20 visitas y 8 operaciones).

**Ejecuciones siguientes:** el sistema detecta los archivos en `/data/` y restaura el estado guardado automáticamente.

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

### 4. Acceso al sistema

| Rol | URL | Credenciales de prueba |
|---|---|---|
| Administrador | `http://localhost:4200/dashboard` | `jose@gmail.com` / `1111100000` |
| Cliente | `http://localhost:4200/cliente` | Usar correo + ID de cualquier cliente del DataLoader, o registrarse |

**Clientes de prueba disponibles (DataLoader):**

| Correo | ID (cédula) |
|---|---|
| juan@gmail.com | 1094567890 |
| maria@gmail.com | 1006234567 |
| carlos@gmail.com | 1093123456 |
| ana@hotmail.com | 1005678901 |
| pedro@gmail.com | 1094890123 |
| sofia@gmail.com | 1007345678 |
| tomas@gmail.com | 1095012345 |
| vale@gmail.com | 1008901234 |
| ricardo@gmail.com | 1090123456 |
| dani@gmail.com | 1001234560 |

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
| `DELETE` | `/api/clientes/{id}/favoritos/{codigo}` | Quitar favorito |
| `POST` | `/api/clientes/{id}/descartados/{codigo}` | Registrar descarte |
| `GET` | `/api/clientes/{id}/favoritos` | Obtener favoritos |
| `GET` | `/api/clientes/{id}/historial` | Obtener historial de consultas (sin duplicados) |
| `GET` | `/api/clientes/{id}/interacciones` | Historial unificado (`?tipo=VISITA_AGENDADA`) |
| `POST` | `/api/clientes/{id}/intencion` | Registrar intención de compra o renta |
| `POST` | `/api/clientes/{id}/visitas` | Agendar visita desde el portal cliente |
| `POST` | `/api/clientes/{id}/consulta/{codigo}` | Registrar consulta de inmueble |

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
| `POST` | `/api/alertas/pendientes/procesar` | Procesar siguiente (Cola de prioridad — extrae la más crítica) |
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
| `GET` | `/api/plataforma/grafo/similares/{idCliente}` | Clientes con perfil similar (vecinos de vecinos) |
| `GET` | `/api/plataforma/rankings/asesores` | Ranking de asesores por cierres |
| `GET` | `/api/plataforma/rankings/zonas` | Actividad por zona/barrio |
| `GET` | `/api/plataforma/clientes/alta-probabilidad-cierre` | Clientes con alta prob. de cierre |
| `GET` | `/api/plataforma/rankings/demanda-sectores` | Simulación de crecimiento por sector |

---

## Diagrama de entidades

```
Persona (abstract)
 ├── Cliente
 │     ├── inmueblesConsultados      : SimpleLinkedList<Inmueble>
 │     ├── propiedadesVisitadas      : SimpleLinkedList<Inmueble>
 │     ├── inmueblesGuardados        : SimpleLinkedList<Inmueble>
 │     ├── inmueblesDescartados      : SimpleLinkedList<Inmueble>
 │     ├── inmueblesNegociados       : SimpleLinkedList<Inmueble>
 │     ├── listaRecomendaciones      : SimpleLinkedList<Recomendacion>
 │     └── historialInteracciones    : SimpleLinkedList<Interaccion>
 └── Asesor
       ├── inmueblesAsignados        : SimpleLinkedList<Inmueble>
       ├── visitasAgendadas          : SimpleLinkedList<Visita>
       └── cierresRealizados         : SimpleLinkedList<Operacion>

Inmueble  ──── Visita ──── Cliente
   │                          │
   └──── Operacion ───────────┘
              │
           Asesor

Alerta        EventoInusual        Recomendacion        Interaccion
```

---

## Datos de prueba

Al iniciar la aplicación por primera vez, el `DataLoader` inserta automáticamente:

| Entidad | Cantidad |
|---|---|
| Asesores | 5 |
| Clientes | 10 |
| Inmuebles | 20 (Medellín, Bogotá, Cali) |
| Visitas | 10 (pasadas y recientes) |
| Operaciones | 4 |

**Nota:** desde la segunda ejecución en adelante el sistema carga desde los archivos JSON en `/data/`, preservando todos los cambios realizados durante la sesión anterior.

---

## Estructura del proyecto

```
├── pom.xml                          # Dependencias Maven (Java 21 + Spring Boot 3)
├── mvnw / mvnw.cmd                  # Maven Wrapper
├── data/                            # Datos persistidos en JSON (se genera en runtime)
├── src/
│   └── main/java/.../PropTech/
│       ├── config/
│       │   ├── DataLoader.java      # Carga/restaura datos al iniciar
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
│       │   ├── PlataformaBeta.java  # Orquestador principal
│       │   └── PersistenciaService.java # Guardar/restaurar en JSON
│       ├── structures/              # Implementaciones propias (sin java.util)
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
    │   │   ├── guards/              # authGuard + adminGuard
    │   │   └── interceptors/        # Error interceptor global
    │   └── features/                # Componentes por módulo
    │       ├── login/               # Pantalla de login y registro
    │       ├── dashboard/
    │       ├── inmuebles/
    │       ├── clientes/
    │       ├── asesores/
    │       ├── visitas/
    │       ├── operaciones/
    │       ├── alertas/
    │       ├── eventos/
    │       ├── recomendaciones/
    │       ├── grafo/
    │       └── cliente-portal/      # Portal exclusivo para usuarios CLIENTE
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

El frontend captura todos los errores HTTP mediante un interceptor global (`ErrorInterceptor`) y los muestra como notificaciones toast en la esquina superior derecha.

---

*Universidad del Quindío — Programación Orientada a Objetos — Estructuras de Datos*
