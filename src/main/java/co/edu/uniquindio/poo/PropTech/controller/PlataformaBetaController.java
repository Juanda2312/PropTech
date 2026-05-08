package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.model.dto.ClienteDTO;
import co.edu.uniquindio.poo.PropTech.model.dto.AsesorDTO;
import co.edu.uniquindio.poo.PropTech.model.dto.InmuebleDTO;
import co.edu.uniquindio.poo.PropTech.model.dto.OperacionDTO;
import co.edu.uniquindio.poo.PropTech.model.dto.VisitaDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.*;
import co.edu.uniquindio.poo.PropTech.model.enums.FinalidadInmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInmueble;
import co.edu.uniquindio.poo.PropTech.service.PlataformaBeta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador que expone las operaciones orquestadas de PlataformaBeta:
 * registro combinado de entidades, visitas con detección de comportamientos,
 * alertas automáticas, recomendaciones y análisis con grafos.
 *
 * Ruta base: /api/plataforma
 */
@RestController
@RequestMapping("/api/plataforma")
public class PlataformaBetaController {

    private final PlataformaBeta plataformaBeta;

    public PlataformaBetaController(PlataformaBeta plataformaBeta) {
        this.plataformaBeta = plataformaBeta;
    }

    // ================================================================
    // REGISTRO ORQUESTADO DE ENTIDADES
    // (usan PlataformaBeta porque añaden lógica extra:
    //  agregan el vértice al grafo, asignan inmueble al asesor, etc.)
    // ================================================================

    // ----------------------------------------------------------------
    // POST /api/plataforma/inmuebles
    // Registra el inmueble, lo asigna al asesor y añade el vértice al grafo.
    // ----------------------------------------------------------------
    @PostMapping("/inmuebles")
    public ResponseEntity<Inmueble> registrarInmueble(@RequestBody InmuebleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plataformaBeta.registrarInmueble(dto));
    }

    // ----------------------------------------------------------------
    // POST /api/plataforma/clientes
    // Registra el cliente y añade el vértice al grafo.
    // ----------------------------------------------------------------
    @PostMapping("/clientes")
    public ResponseEntity<Cliente> registrarCliente(@RequestBody ClienteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plataformaBeta.registrarCliente(dto));
    }

    // ----------------------------------------------------------------
    // POST /api/plataforma/asesores
    // ----------------------------------------------------------------
    @PostMapping("/asesores")
    public ResponseEntity<Asesor> registrarAsesor(@RequestBody AsesorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plataformaBeta.registrarAsesor(dto));
    }

    // ================================================================
    // VISITAS ORQUESTADAS
    // (actualiza historial del cliente, carga del asesor y el grafo)
    // ================================================================

    // ----------------------------------------------------------------
    // POST /api/plataforma/visitas
    // ----------------------------------------------------------------
    @PostMapping("/visitas")
    public ResponseEntity<Visita> agendarVisita(@RequestBody VisitaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plataformaBeta.agendarVisita(dto));
    }

    // ================================================================
    // OPERACIONES ORQUESTADAS
    // (registra cierre en el asesor e historial de negociación del cliente)
    // ================================================================

    // ----------------------------------------------------------------
    // POST /api/plataforma/operaciones
    // ----------------------------------------------------------------
    @PostMapping("/operaciones")
    public ResponseEntity<Operacion> registrarOperacion(@RequestBody OperacionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plataformaBeta.registrarOperacion(dto));
    }

    // ================================================================
    // ALERTAS AUTOMÁTICAS
    // ================================================================

    // ----------------------------------------------------------------
    // POST /api/plataforma/alertas/generar
    // Analiza todo el sistema y genera alertas sobre inmuebles inactivos,
    // visitas sin confirmar y clientes sin seguimiento.
    // ----------------------------------------------------------------
    @PostMapping("/alertas/generar")
    public ResponseEntity<List<Alerta>> generarAlertas() {
        return ResponseEntity.ok(plataformaBeta.generarAlertas());
    }

    // ================================================================
    // DETECCIÓN DE COMPORTAMIENTOS INUSUALES
    // ================================================================

    // ----------------------------------------------------------------
    // POST /api/plataforma/eventos/detectar
    // Recorre clientes, asesores e inmuebles buscando patrones inusuales
    // y registra los EventoInusual correspondientes.
    // ----------------------------------------------------------------
    @PostMapping("/eventos/detectar")
    public ResponseEntity<Void> detectarComportamientos() {
        plataformaBeta.detectarComportamientosInusuales();
        return ResponseEntity.noContent().build();
    }

    // ================================================================
    // RECOMENDACIONES
    // ================================================================

    // ----------------------------------------------------------------
    // GET /api/plataforma/recomendaciones/{idCliente}
    // Genera y devuelve recomendaciones ordenadas por puntaje para el cliente.
    // ----------------------------------------------------------------
    @GetMapping("/recomendaciones/{idCliente}")
    public ResponseEntity<List<Recomendacion>> generarRecomendaciones(
            @PathVariable String idCliente) {
        return ResponseEntity.ok(plataformaBeta.generarRecomendaciones(idCliente));
    }

    // ----------------------------------------------------------------
    // GET /api/plataforma/similares/{codigoInmueble}
    // Devuelve inmuebles disponibles similares al indicado
    // (mismo tipo, finalidad y precio ±20%).
    // ----------------------------------------------------------------
    @GetMapping("/similares/{codigoInmueble}")
    public ResponseEntity<List<Inmueble>> sugerirSimilares(
            @PathVariable String codigoInmueble) {
        return ResponseEntity.ok(plataformaBeta.sugerirSimilares(codigoInmueble));
    }

    // ================================================================
    // ANÁLISIS CON GRAFOS
    // ================================================================

    // ----------------------------------------------------------------
    // GET /api/plataforma/grafo/clientes/{codigoInmueble}
    // Devuelve los IDs de clientes conectados a un inmueble en el grafo.
    // ----------------------------------------------------------------
    @GetMapping("/grafo/clientes/{codigoInmueble}")
    public ResponseEntity<List<String>> clientesConectadosAInmueble(
            @PathVariable String codigoInmueble) {
        return ResponseEntity.ok(
                plataformaBeta.obtenerClientesConectadosAInmueble(codigoInmueble));
    }

    // ----------------------------------------------------------------
    // GET /api/plataforma/grafo/inmuebles/{idCliente}
    // Devuelve los códigos de inmuebles visitados por un cliente en el grafo.
    // ----------------------------------------------------------------
    @GetMapping("/grafo/inmuebles/{idCliente}")
    public ResponseEntity<List<String>> inmueblesConectadosACliente(
            @PathVariable String idCliente) {
        return ResponseEntity.ok(
                plataformaBeta.obtenerInmueblesConectadosACliente(idCliente));
    }

    // ================================================================
    // REPORTES Y RANKINGS
    // ================================================================

    // ----------------------------------------------------------------
    // GET /api/plataforma/rankings/asesores
    // Asesores ordenados por número de cierres (descendente).
    // ----------------------------------------------------------------
    @GetMapping("/rankings/asesores")
    public ResponseEntity<List<Asesor>> rankingAsesores() {
        return ResponseEntity.ok(plataformaBeta.rankingAsesoresPorCierres());
    }

    // ----------------------------------------------------------------
    // GET /api/plataforma/rankings/zonas
    // Mapa zona/barrio → número de visitas registradas.
    // ----------------------------------------------------------------
    @GetMapping("/rankings/zonas")
    public ResponseEntity<Map<String, Integer>> rankingZonas() {
        return ResponseEntity.ok(plataformaBeta.rankingZonasPorActividad());
    }
    // ----------------------------------------------------------------
// GET /api/plataforma/grafo/bfs/{nodo}
// Recorrido BFS desde un nodo del grafo, retorna lista de nodos visitados.
// ----------------------------------------------------------------
    @GetMapping("/grafo/bfs/{nodo}")
    public ResponseEntity<List<String>> bfsDesdeNodo(@PathVariable String nodo) {
        return ResponseEntity.ok(plataformaBeta.analizarRelacionesBFS(nodo));
    }

    // ----------------------------------------------------------------
// POST /api/plataforma/visitas/vip/procesar
// Procesa la siguiente visita VIP de la cola de prioridad.
// ----------------------------------------------------------------
    @PostMapping("/visitas/vip/procesar")
    public ResponseEntity<Visita> procesarVisitaVIP() {
        return ResponseEntity.ok(plataformaBeta.procesarVisitaVIP());
    }

    // ----------------------------------------------------------------
    // GET /api/plataforma/inmuebles/ordenados
    // Devuelve todos los inmuebles ordenados por precio (inOrder del AVL).
    // ----------------------------------------------------------------
    @GetMapping("/inmuebles/ordenados")
    public ResponseEntity<List<Inmueble>> inmueblesOrdenadosPorPrecio() {
        return ResponseEntity.ok(plataformaBeta.obtenerInmueblesOrdenadosPorPrecio());
    }

    // ----------------------------------------------------------------
    // GET /api/plataforma/inmuebles/buscar
    // Búsqueda combinada por múltiples filtros.
    // ?tipo=CASA&finalidad=VENTA&ciudad=Medellín&precioMax=400000&habitacionesMin=3
    // ----------------------------------------------------------------
    @GetMapping("/inmuebles/buscar")
    public ResponseEntity<List<Inmueble>> buscarConFiltros(
            @RequestParam(required = false) TipoInmueble tipo,
            @RequestParam(required = false) FinalidadInmueble finalidad,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false, defaultValue = "0") double precioMax,
            @RequestParam(required = false, defaultValue = "0") int habitacionesMin) {

        return ResponseEntity.ok(
                plataformaBeta.buscarPorFiltrosCombinados(
                        tipo, finalidad, ciudad, precioMax, habitacionesMin));
    }
}
