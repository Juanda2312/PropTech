# Despliegue de PropTech (Vercel + Render) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Dejar PropTech desplegable con el frontend Angular en Vercel y el backend Spring Boot en Render, hablando por HTTPS con CORS restringido y sin secretos en el repo.

**Architecture:** El frontend es un SPA estático (Vercel) que llama directo a `https://<api>.onrender.com/api/**` usando `environment.apiUrl`. El backend se empaqueta en Docker y lee puerto, orígenes CORS, carpeta de datos y clave de OpenRouter desde variables de entorno. PropBot (chatbot) deja de llamar a OpenRouter desde el navegador y pasa por un endpoint del backend, para que la clave no viaje en el bundle.

**Tech Stack:** Java 21, Spring Boot 3.5.13, Maven wrapper, Angular 17 (builder `application`), Docker, Render (Blueprint `render.yaml`), Vercel (`vercel.json`).

**Spec:** Petición de David del 2026-11-10 («front en Vercel, back en otro; ajustar el código para poder desplegarlo») + sección «Hallazgos verificados» de este documento. Repo: https://github.com/Juanda2312/PropTech (rama `master`).

## Global Constraints

- Paquete base del backend: `co.edu.uniquindio.poo.PropTech`. Java `21`, Spring Boot `3.5.13`.
- Salida del build de Angular: `frontend/dist/proptech-frontend/browser` (builder `application`).
- Los contratos de la API (`/api/**`) **no cambian**; solo se añade `POST /api/chatbot/completions`.
- Variables de entorno del backend y su propiedad Spring (nombres exactos):
    - `PORT` → `server.port` (por defecto `8080`)
    - `CORS_ALLOWED_ORIGINS` → `proptech.cors.allowed-origins` (por defecto `http://localhost:4200`; lista separada por comas)
    - `PROPTECH_DATA_DIR` → `proptech.data-dir` (por defecto `data/`)
    - `OPENROUTER_API_KEY` → `proptech.openrouter.api-key` (por defecto vacío)
- Ningún secreto se commitea: la clave de OpenRouter solo existe como variable de entorno en Render.
- Render plan gratuito: se duerme tras 15 min sin tráfico (≈1 min en despertar), filesystem efímero (se pierde al reiniciar, redeploy o dormir), sin discos persistentes, poca RAM (≈512 MB; confírmalo en tu panel).
- El frontend no tiene infraestructura de tests (`angular.json` no define target `test` ni hay `.spec.ts`). No se añade en este plan (YAGNI); las tareas de frontend se verifican con `ng build` + `grep`.

## Review Focus

1. **Primera petición tras dormirse el backend (≈1 min):** el usuario debe ver el toast de error del `ErrorInterceptor` (no una pantalla rota) y poder reintentar. → verificación manual en Task 8.
2. **Reinicio del backend pierde clientes registrados desde el portal, pero siguen en el `localStorage` del navegador** (`proptech_clientes_auth`): el login funciona y las acciones devuelven 404 «Cliente no encontrado». Limitación conocida del plan gratuito (Decisión D2). → Task 3 deja la carpeta montable en un disco persistente; Task 8 lo comprueba.
3. **Preflight `OPTIONS` de `PATCH`/`DELETE`** (confirmar/cancelar visitas, quitar favoritos) desde el origen de Vercel debe pasar; desde otro origen debe rechazarse. → tests en Task 2.
4. **Recarga directa en `/cliente` o `/dashboard`** debe devolver `index.html`, no 404 de Vercel. → verificación con `curl` en Task 7.
5. **Abuso del proxy del chatbot:** CORS no protege de `curl`; cualquiera podría gastar la cuota de la clave eligiendo otro modelo o `max_tokens` enorme. → test en Task 6 (el backend fuerza modelo y tope de tokens); la clave nunca aparece en el bundle (`grep`).

---

## Decisiones abiertas (el plan asume el valor por defecto)

- **D1 · Host del backend:** Render plan gratuito con Docker. El mismo `Dockerfile` sirve para Railway o Fly.io si prefieres cambiar.
- **D2 · Persistencia:** por defecto efímera (el backend vuelve a cargar los datos de prueba del `DataLoader` tras cada reinicio). Para conservar datos hay que usar un plan de pago de Render con disco persistente y poner `PROPTECH_DATA_DIR=/var/data/` (Task 3 lo permite sin más cambios de código). Pasar a base de datos queda fuera de alcance.
- **D3 · Chatbot:** se pasa por el backend (Task 6). Si prefieres no usar PropBot en producción, omite Task 6 y quita el botón flotante; pero **no** despliegues con la clave dentro de `chatbot.service.ts`.

## Hallazgos verificados en el repo

| # | Hallazgo | Evidencia | Lo resuelve |
|---|----------|-----------|-------------|
| 1 | `ng build` (producción) **falla en un clon limpio**: 3 componentes superan el presupuesto `anyComponentStyle` (error a 4 kB): `cliente-portal` 7.52 kB, `chatbot` 6.11 kB, `dashboard` 4.28 kB | Build ejecutado | Task 5 |
| 2 | `angular.json` no tiene `fileReplacements`: `environment.prod.ts` (que apunta a `http://localhost:8080`) **nunca se usa** | `angular.json` | Task 5 |
| 3 | `auth.service.ts` llama a `'/api/clientes'` (l. 88) y `'/api/plataforma/clientes'` (l. 173) con ruta relativa, sin `environment.apiUrl`: en Vercel irían al propio Vercel | grep | Task 5 |
| 4 | `server.port=8080` fijo; Render inyecta `PORT` | `application.properties` | Task 2 |
| 5 | CORS abierto a `*` (`WebConfig`) | `WebConfig.java` | Task 2 |
| 6 | La persistencia escribe en `data/` relativo y solo en `@PreDestroy` | `PersistenciaService.java` | Task 3 |
| 7 | `BackendApplicationTests` está en el paquete `com.proptech.backend`, fuera del de la app (`co.edu.uniquindio.poo.PropTech`): Spring no encuentra `@SpringBootConfiguration` y `mvn test/package` fallaría. *(No pude ejecutar Maven en mi entorno; confírmalo en Task 1.)* | Lectura del código | Task 1 |
| 8 | La clave de OpenRouter está en el navegador (`chatbot.service.ts`, hoy con el placeholder `API_KEY_AQUI`) | `chatbot.service.ts` | Task 6 |
| 9 | No hay `package-lock.json` en `frontend/` ni `Dockerfile`, `vercel.json` o `render.yaml` | listado | Tasks 4, 7 |
| 10 | `.gitignore` ignora `.data/` pero la app escribe en `data/` | `.gitignore` | Task 1 |

---

### Task 1: Línea base verde del backend

**Files:**
- Delete: `src/test/java/com/proptech/backend/BackendApplicationTests.java`
- Modify: `.gitignore` (añadir `data/`)
- Test (ya existe): `src/test/java/co/edu/uniquindio/poo/PropTech/PropTechApplicationTests.java`

**Interfaces:**
- Produces: `./mvnw test` pasa en limpio; las tareas 2, 3 y 6 añaden tests sobre esta base.

- [ ] **Step 1: Comprobar que la suite actual falla**

Run: `./mvnw -q test`
Expected: FAIL con `Unable to find a @SpringBootConfiguration` en `BackendApplicationTests`.

- [ ] **Step 2: Borrar `BackendApplicationTests.java`**

`PropTechApplicationTests.contextLoads` ya cubre lo mismo en el paquete correcto.

- [ ] **Step 3: Añadir `data/` a `.gitignore`**

Los tests y la app crean `data/` en la raíz; no debe commitearse.

- [ ] **Step 4: Verificar**

Run: `./mvnw -q test`
Expected: PASS (1 test). `git status` no muestra `data/`.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "test: eliminar test duplicado fuera del paquete base e ignorar data/"
```

---

### Task 2: Puerto y CORS configurables por entorno

**Files:**
- Modify: `src/main/resources/application.properties`
- Modify: `src/main/java/co/edu/uniquindio/poo/PropTech/config/WebConfig.java`
- Test: `src/test/java/co/edu/uniquindio/poo/PropTech/config/WebConfigCorsTest.java`

**Interfaces:**
- Consumes: nada de tareas previas.
- Produces: propiedades `server.port=${PORT:8080}` y `proptech.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:4200}`. `WebConfig` aplica esa lista (admite comodines tipo `https://proptech-*.vercel.app`) al mapeo `/api/**`.

- [ ] **Step 1: Escribir el test que falla** — `WebConfigCorsTest`

```java
@SpringBootTest(properties = "proptech.cors.allowed-origins=https://proptech.vercel.app")
@AutoConfigureMockMvc
class WebConfigCorsTest {
    @Autowired MockMvc mvc;

    @Test
    void preflightPATCH_desdeOrigenPermitido_respondeConEseOrigen() throws Exception {
        mvc.perform(options("/api/visitas/V-1/confirmar")
                .header("Origin", "https://proptech.vercel.app")
                .header("Access-Control-Request-Method", "PATCH"))
           .andExpect(status().isOk())
           .andExpect(header().string("Access-Control-Allow-Origin", "https://proptech.vercel.app"));
    }

    @Test
    void preflight_desdeOrigenNoPermitido_seRechazaConForbidden() throws Exception {
        mvc.perform(options("/api/visitas/V-1/confirmar")
                .header("Origin", "https://otro-sitio.example")
                .header("Access-Control-Request-Method", "PATCH"))
           .andExpect(status().isForbidden());
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./mvnw -q test -Dtest=WebConfigCorsTest`
Expected: FAIL (hoy el origen ajeno recibe 200 y el header devuelve `*`).

- [ ] **Step 3: Implementar**

- `application.properties`: `server.port=${PORT:8080}` (reemplaza la línea fija) y añadir `proptech.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:4200}`.
- `WebConfig`: inyectar `@Value("${proptech.cors.allowed-origins}") String[] allowedOrigins` y usar `.allowedOriginPatterns(allowedOrigins)` en lugar de `.allowedOriginPatterns("*")`. Mantener métodos, headers, `allowCredentials(false)` y `maxAge(3600)`. Actualizar el Javadoc (ya no es «cualquier cliente»).

- [ ] **Step 4: Ejecutar y ver que pasa**

Run: `./mvnw -q test`
Expected: PASS (todos). El default `http://localhost:4200` mantiene funcionando el `ng serve` con proxy (el `Origin` sigue siendo `localhost:4200`).

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/application.properties src/main/java/co/edu/uniquindio/poo/PropTech/config/WebConfig.java src/test/java/co/edu/uniquindio/poo/PropTech/config/WebConfigCorsTest.java
git commit -m "feat: puerto y CORS configurables por variables de entorno"
```

---

### Task 3: Carpeta de datos configurable

**Files:**
- Modify: `src/main/java/co/edu/uniquindio/poo/PropTech/service/PersistenciaService.java`
- Modify: `src/main/resources/application.properties`
- Test: `src/test/java/co/edu/uniquindio/poo/PropTech/service/PersistenciaServiceDirTest.java`

**Interfaces:**
- Consumes: `application.properties` de Task 2.
- Produces: propiedad `proptech.data-dir=${PROPTECH_DATA_DIR:data/}`. El constructor de `PersistenciaService` recibe como **primer** parámetro `@Value("${proptech.data-dir}") String dataDir`; los 8 archivos JSON se resuelven contra ese directorio.

- [ ] **Step 1: Escribir el test que falla** — `PersistenciaServiceDirTest`

```java
@SpringBootTest(properties = "proptech.data-dir=${java.io.tmpdir}/proptech-test/sin-slash-final")
class PersistenciaServiceDirTest {
    static final Path DIR = Path.of(System.getProperty("java.io.tmpdir"), "proptech-test", "sin-slash-final");

    @BeforeAll static void limpiar() throws IOException {
        if (Files.exists(DIR)) FileSystemUtils.deleteRecursively(DIR);
    }

    @Test
    void creaLaCarpetaConfigurada_aunqueNoTerminaEnSlash() {
        assertTrue(Files.isDirectory(DIR));
    }
}
```

- [ ] **Step 2: Ejecutar y ver que falla**

Run: `./mvnw -q test -Dtest=PersistenciaServiceDirTest`
Expected: FAIL (la propiedad se ignora; hoy siempre usa `data/`).

- [ ] **Step 3: Implementar**

En `PersistenciaService`: reemplazar `DIR` y las 8 constantes `*_FILE` (`static final`) por campos `private final String` calculados en el constructor a partir de `dataDir`. Si `dataDir` no termina en `/`, agregarlo. `new File(...).mkdirs()` pasa a usar ese directorio. Los usos existentes (`hayDatosGuardados`, cargar/guardar) no cambian de lógica. Añadir `proptech.data-dir=${PROPTECH_DATA_DIR:data/}` a `application.properties`.

- [ ] **Step 4: Ejecutar y ver que pasa**

Run: `./mvnw -q test`
Expected: PASS (todos, incluidos los de Task 2).

- [ ] **Step 5: Commit**

```bash
git add src/main src/test
git commit -m "feat: carpeta de persistencia configurable con PROPTECH_DATA_DIR"
```

---

### Task 4: Imagen Docker y Blueprint de Render

**Files:**
- Create: `Dockerfile`, `.dockerignore`, `render.yaml` (todos en la raíz)

**Interfaces:**
- Consumes: variables de Tasks 2 y 3 (`PORT`, `CORS_ALLOWED_ORIGINS`, `PROPTECH_DATA_DIR`); `OPENROUTER_API_KEY` se declara aquí y la usa Task 6.
- Produces: servicio web `proptech-api` en Render, health check en `GET /api/asesores`.

- [ ] **Step 1: Crear `Dockerfile`** (copia exacta; no hay firma que determine estos valores)

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY .mvn .mvn
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 2: Crear `.dockerignore`** con: `frontend/`, `target/`, `data/`, `.git/`, `.idea/`, `.vscode/`.

- [ ] **Step 3: Crear `render.yaml`**

```yaml
services:
  - type: web
    name: proptech-api
    runtime: docker
    plan: free
    healthCheckPath: /api/asesores
    envVars:
      - key: CORS_ALLOWED_ORIGINS
        sync: false
      - key: OPENROUTER_API_KEY
        sync: false
```

- [ ] **Step 4: Verificar la imagen en local** (requiere Docker; si no lo tienes, sáltalo: lo verificará el build de Render en Task 8)

Run:
```bash
docker build -t proptech-api .
docker run --rm -d --name pt -p 8081:8081 -e PORT=8081 -e CORS_ALLOWED_ORIGINS=https://proptech.vercel.app proptech-api
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8081/api/asesores
curl -si -X OPTIONS http://localhost:8081/api/visitas/V-1/confirmar -H "Origin: https://proptech.vercel.app" -H "Access-Control-Request-Method: PATCH" | grep -i access-control-allow-origin
docker stop pt
```
Expected: `200`; y la cabecera `Access-Control-Allow-Origin: https://proptech.vercel.app`.

- [ ] **Step 5: Commit**

```bash
git add Dockerfile .dockerignore render.yaml
git commit -m "build: Dockerfile y render.yaml para desplegar el backend"
```

---

### Task 5: Build de producción del frontend y rutas a la API

**Files:**
- Modify: `frontend/angular.json` (configuración `production`)
- Modify: `frontend/src/environments/environment.prod.ts`
- Modify: `frontend/src/app/core/services/auth.service.ts:88` y `:173`

**Interfaces:**
- Consumes: `environment.apiUrl` (ya usado por los demás servicios, p. ej. `inmueble.service.ts`).
- Produces: `ng build` compila en producción sustituyendo `environment.ts` por `environment.prod.ts`; ninguna llamada HTTP del frontend usa ruta relativa a `/api` salvo vía `environment.apiUrl`.

- [ ] **Step 1: Comprobar que el build actual falla**

Run: `cd frontend && npm install && npx ng build`
Expected: FAIL con 3 `[ERROR] ... exceeded maximum budget` (`cliente-portal`, `chatbot`, `dashboard`).

- [ ] **Step 2: Ajustar `angular.json` → `configurations.production`**

- Añadir `"fileReplacements": [{ "replace": "src/environments/environment.ts", "with": "src/environments/environment.prod.ts" }]`.
- En `budgets`, el de tipo `anyComponentStyle`: `maximumWarning` `"10kb"`, `maximumError` `"20kb"`. Dejar intacto el de `initial`.

- [ ] **Step 3: Poner `environment.prod.ts` con la URL del backend**

`apiUrl: 'https://proptech-api.onrender.com'` como valor provisional; se corrige con la URL real en Task 8 (Render puede añadir un sufijo al nombre si ya existe).

- [ ] **Step 4: `auth.service.ts`**: importar `environment` igual que los otros servicios (`'../../../environments/environment'`) y cambiar `'/api/clientes'` → `` `${environment.apiUrl}/api/clientes` `` y `'/api/plataforma/clientes'` → `` `${environment.apiUrl}/api/plataforma/clientes` ``.

- [ ] **Step 5: Verificar**

Run:
```bash
cd frontend
npx ng build
grep -rnE "['\"\`]/api" src/app          # no debe imprimir nada
grep -rl "localhost:8080" dist           # no debe imprimir nada
grep -rl "proptech-api.onrender.com" dist
```
Expected: build OK; el primer y segundo `grep` sin salida; el tercero lista al menos un `.js`.
Nota: si compilas sin internet, falla la inlinación de Google Fonts (`styles.scss` importa la fuente); no es un error del repo.

- [ ] **Step 6: Commit**

```bash
git add frontend/angular.json frontend/src
git commit -m "fix(frontend): build de producción, environment.prod y rutas a la API"
```

---

### Task 6: PropBot a través del backend

**Files:**
- Create: `src/main/java/co/edu/uniquindio/poo/PropTech/service/ChatbotProxyService.java`
- Create: `src/main/java/co/edu/uniquindio/poo/PropTech/controller/ChatbotController.java`
- Modify: `src/main/resources/application.properties`
- Modify: `frontend/src/app/core/services/chatbot.service.ts`
- Test: `src/test/java/co/edu/uniquindio/poo/PropTech/service/ChatbotProxyServiceTest.java`, `src/test/java/co/edu/uniquindio/poo/PropTech/controller/ChatbotControllerTest.java`

**Interfaces:**
- Consumes: `ApiError` (`util/ApiError.java`) para los errores, como el resto de controladores.
- Produces:
    - `ChatbotProxyService.estaConfigurado(): boolean` (clave no vacía)
    - `ChatbotProxyService.reenviar(Map<String,Object> cuerpo): String` (POST a `https://openrouter.ai/api/v1/chat/completions` con `Authorization: Bearer <clave>`, devuelve el JSON del proveedor tal cual; usa `RestClient`)
    - `static Map<String,Object> sanear(Map<String,Object> cuerpo)`: copia con `model = "openrouter/free"` y `max_tokens = min(valor recibido, 2048)` (2048 si falta)
    - `POST /api/chatbot/completions` → `200` JSON del proveedor · `503` si `!estaConfigurado()` (mensaje «PropBot no está configurado») · `502` si `reenviar` lanza `RestClientException`
    - propiedad `proptech.openrouter.api-key=${OPENROUTER_API_KEY:}`

- [ ] **Step 1: Escribir los tests que fallan**

`ChatbotProxyServiceTest`:
```java
@Test
void sanear_fuerzaModeloYLimitaMaxTokens() {
    Map<String,Object> in = Map.of("model", "gpt-4", "max_tokens", 99999, "messages", List.of());
    Map<String,Object> out = ChatbotProxyService.sanear(in);
    assertEquals("openrouter/free", out.get("model"));
    assertEquals(2048, out.get("max_tokens"));
    assertEquals(List.of(), out.get("messages"));
}
```
`ChatbotControllerTest` con `@WebMvcTest(ChatbotController.class)` y `@MockitoBean ChatbotProxyService proxy`:
- `sinClaveConfigurada_responde503`: `estaConfigurado()` → `false`; POST `{"messages":[]}` → `status().isServiceUnavailable()`.
- `conClave_devuelveLaRespuestaDelProveedor`: `estaConfigurado()` → `true`, `reenviar(any())` → `"{\"choices\":[]}"`; POST → `200` y cuerpo `{"choices":[]}`.
- `errorDelProveedor_responde502`: `reenviar(any())` lanza `new RestClientException("fallo")` → `status().isBadGateway()`.

- [ ] **Step 2: Ejecutar y ver que fallan**

Run: `./mvnw -q test -Dtest='Chatbot*Test'`
Expected: FAIL de compilación (clases no definidas).

- [ ] **Step 3: Implementar backend**

`ChatbotProxyService` (`@Service`, clave por `@Value("${proptech.openrouter.api-key:}")`) y `ChatbotController` (`@RestController`, `@RequestMapping("/api/chatbot")`, `@PostMapping("/completions")`, cuerpo `Map<String,Object>`, respuesta `ResponseEntity<String>` con `application/json`). El controlador llama a `reenviar(sanear(cuerpo))`. Cabecera adicional hacia OpenRouter: `X-Title: PropTech Chatbot`. Añadir la propiedad a `application.properties`.

- [ ] **Step 4: Ejecutar y ver que pasan**

Run: `./mvnw -q test`
Expected: PASS (todos).

- [ ] **Step 5: Frontend `chatbot.service.ts`**

Importar `environment`; `API_URL` pasa a `` `${environment.apiUrl}/api/chatbot/completions` ``; eliminar `API_KEY`, `MODEL` y las cabeceras `Authorization` y `HTTP-Referer` (dejar `Content-Type` y `X-Title`). El cuerpo (`model`, `messages`, `max_tokens`, `temperature`) y el parseo de la respuesta no cambian.

- [ ] **Step 6: Verificar el frontend**

Run:
```bash
cd frontend && npx ng build
grep -rnE "sk-or|API_KEY|openrouter\.ai" src dist     # no debe imprimir nada
```
Expected: build OK; `grep` sin salida.

- [ ] **Step 7: Commit**

```bash
git add src frontend/src
git commit -m "feat: PropBot consume el backend y la clave de OpenRouter sale del navegador"
```

---

### Task 7: Configuración de Vercel

**Files:**
- Create: `frontend/vercel.json`
- Create: `frontend/package-lock.json` (generado con `npm install`; se commitea)
- Modify: `frontend/package.json` (añadir `"engines": { "node": "20.x" }`)

**Interfaces:**
- Consumes: build de Task 5.
- Produces: proyecto Vercel con **Root Directory = `frontend`** que sirve `dist/proptech-frontend/browser` con fallback SPA.

- [ ] **Step 1: Crear `frontend/vercel.json`** (copia exacta)

```json
{
  "$schema": "https://openapi.vercel.sh/vercel.json",
  "buildCommand": "npm run build",
  "outputDirectory": "dist/proptech-frontend/browser",
  "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }]
}
```

- [ ] **Step 2: `engines.node`** en `package.json` (Angular 17 exige Node 18.13+/20.9+; se fija 20 para que Vercel no use una versión no soportada).

- [ ] **Step 3: Verificar un build reproducible**

Run: `cd frontend && npm ci && npm run build && test -f dist/proptech-frontend/browser/index.html && echo OK`
Expected: `OK`.

- [ ] **Step 4: Commit**

```bash
git add frontend/vercel.json frontend/package.json frontend/package-lock.json
git commit -m "build(frontend): vercel.json, Node 20 y package-lock"
```

---

### Task 8: Despliegue y verificación de extremo a extremo

**Files:**
- Modify: `frontend/src/environments/environment.prod.ts` (URL real del backend)

**Interfaces:**
- Consumes: todo lo anterior, ya en `master` en GitHub.

- [ ] **Step 1: Desplegar el backend en Render**

Render → New → Blueprint → elegir el repo → rama `master`. Rellenar `CORS_ALLOWED_ORIGINS` (provisional: `http://localhost:4200`) y `OPENROUTER_API_KEY`. Esperar el build.
Run: `curl -s -o /dev/null -w "%{http_code}\n" https://<api>.onrender.com/api/asesores`
Expected: `200` (la primera llamada puede tardar ≈1 min si el servicio estaba dormido).

- [ ] **Step 2: Poner la URL real** en `environment.prod.ts` (`apiUrl: 'https://<api>.onrender.com'`, sin barra final), commit y push.

```bash
git add frontend/src/environments/environment.prod.ts
git commit -m "chore(frontend): apuntar apiUrl al backend desplegado"
git push
```

- [ ] **Step 3: Desplegar el frontend en Vercel**

Vercel → Add New → Project → importar el repo → **Root Directory: `frontend`** → Deploy. Anotar la URL (`https://<proyecto>.vercel.app`).

- [ ] **Step 4: Cerrar el CORS**

En Render → Environment: `CORS_ALLOWED_ORIGINS=https://<proyecto>.vercel.app` (opcional, para previews: añadir `,https://<proyecto>-*.vercel.app`). Guardar y esperar el reinicio.

- [ ] **Step 5: Verificación manual**

1. `curl -si -X OPTIONS https://<api>.onrender.com/api/visitas/V-1/confirmar -H "Origin: https://<proyecto>.vercel.app" -H "Access-Control-Request-Method: PATCH" | grep -i access-control-allow-origin` → devuelve tu URL de Vercel.
2. `curl -sI https://<proyecto>.vercel.app/cliente` → `200` y `content-type: text/html` (recarga directa, Review Focus 4).
3. En el navegador: iniciar sesión como administrador, ver Inmuebles (20 de prueba), confirmar una visita (PATCH), abrir PropBot y hacerle una pregunta.
4. Dejar el backend 15 min sin tráfico y recargar: debe aparecer el toast de error y, tras ≈1 min, funcionar al reintentar (Review Focus 1).
5. Reiniciar el servicio en Render, registrar un cliente nuevo en el portal antes y comprobar después que desaparece del backend (Review Focus 2); es el comportamiento esperado en el plan gratuito.

---

## Fuera de alcance (y por qué importa)

- **Autenticación real:** los administradores están hardcodeados en `AuthService` (y sus credenciales publicadas en el README) y la API no tiene autenticación. Con una URL pública, cualquiera puede entrar al panel y llamar a los endpoints. Para una demo universitaria es aceptable; para algo más serio necesita un plan aparte (Spring Security + JWT).
- Base de datos real, dominio propio, CI/CD, límite de tasa en el proxy del chatbot.