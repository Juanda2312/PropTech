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

@RestController
@RequestMapping("/api/plataforma")
public class PlataformaBetaController {

    private final PlataformaBeta plataformaBeta;

    public PlataformaBetaController(PlataformaBeta plataformaBeta) {
        this.plataformaBeta = plataformaBeta;
    }

    // ================================================================
    // REGISTRO ORQUESTADO DE ENTIDADES
    // ================================================================

    @PostMapping("/inmuebles")
    public ResponseEntity<Inmueble> registrarInmueble(@RequestBody InmuebleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plataformaBeta.registrarInmueble(dto));
    }

    @PostMapping("/clientes")
    public ResponseEntity<Cliente> registrarCliente(@RequestBody ClienteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plataformaBeta.registrarCliente(dto));
    }

    @PostMapping("/asesores")
    public ResponseEntity<Asesor> registrarAsesor(@RequestBody AsesorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plataformaBeta.registrarAsesor(dto));
    }

    // ================================================================
    // VISITAS ORQUESTADAS
    // ================================================================

    @PostMapping("/visitas")
    public ResponseEntity<Visita> agendarVisita(@RequestBody VisitaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plataformaBeta.agendarVisita(dto));
    }

    @PostMapping("/visitas/vip/procesar")
    public ResponseEntity<Visita> procesarVisitaVIP() {
        return ResponseEntity.ok(plataformaBeta.procesarVisitaVIP());
    }

    // ================================================================
    // OPERACIONES ORQUESTADAS
    // ================================================================

    @PostMapping("/operaciones")
    public ResponseEntity<Operacion> registrarOperacion(@RequestBody OperacionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plataformaBeta.registrarOperacion(dto));
    }

    // ================================================================
    // ALERTAS AUTOMÁTICAS
    // ================================================================

    @PostMapping("/alertas/generar")
    public ResponseEntity<List<Alerta>> generarAlertas() {
        return ResponseEntity.ok(plataformaBeta.generarAlertas());
    }

    // ================================================================
    // DETECCIÓN DE COMPORTAMIENTOS INUSUALES
    // ================================================================

    @PostMapping("/eventos/detectar")
    public ResponseEntity<Void> detectarComportamientos() {
        plataformaBeta.detectarComportamientosInusuales();
        return ResponseEntity.noContent().build();
    }

    // ================================================================
    // RECOMENDACIONES
    // ================================================================

    @GetMapping("/recomendaciones/{idCliente}")
    public ResponseEntity<List<Recomendacion>> generarRecomendaciones(
            @PathVariable String idCliente) {
        return ResponseEntity.ok(plataformaBeta.generarRecomendaciones(idCliente));
    }

    @GetMapping("/similares/{codigoInmueble}")
    public ResponseEntity<List<Inmueble>> sugerirSimilares(
            @PathVariable String codigoInmueble) {
        return ResponseEntity.ok(plataformaBeta.sugerirSimilares(codigoInmueble));
    }

    // ================================================================
    // ANÁLISIS CON GRAFOS
    // ================================================================

    @GetMapping("/grafo/clientes/{codigoInmueble}")
    public ResponseEntity<List<String>> clientesConectadosAInmueble(
            @PathVariable String codigoInmueble) {
        return ResponseEntity.ok(
                plataformaBeta.obtenerClientesConectadosAInmueble(codigoInmueble));
    }

    @GetMapping("/grafo/inmuebles/{idCliente}")
    public ResponseEntity<List<String>> inmueblesConectadosACliente(
            @PathVariable String idCliente) {
        return ResponseEntity.ok(
                plataformaBeta.obtenerInmueblesConectadosACliente(idCliente));
    }

    @GetMapping("/grafo/bfs/{nodo}")
    public ResponseEntity<List<String>> bfsDesdeNodo(@PathVariable String nodo) {
        return ResponseEntity.ok(plataformaBeta.analizarRelacionesBFS(nodo));
    }

    //clientes con perfil similar (visitaron inmuebles en común)
    @GetMapping("/grafo/similares/{idCliente}")
    public ResponseEntity<List<String>> clientesConPerfilSimilar(
            @PathVariable String idCliente) {
        return ResponseEntity.ok(
                plataformaBeta.obtenerClientesConPerfilSimilar(idCliente));
    }

    // ================================================================
    // REPORTES Y RANKINGS
    // ================================================================

    @GetMapping("/rankings/asesores")
    public ResponseEntity<List<Asesor>> rankingAsesores() {
        return ResponseEntity.ok(plataformaBeta.rankingAsesoresPorCierres());
    }

    @GetMapping("/rankings/zonas")
    public ResponseEntity<Map<String, Integer>> rankingZonas() {
        return ResponseEntity.ok(plataformaBeta.rankingZonasPorActividad());
    }

    //clientes con alta probabilidad de cierre
    @GetMapping("/clientes/alta-probabilidad-cierre")
    public ResponseEntity<List<Cliente>> clientesConAltaProbabilidadDeCierre() {
        return ResponseEntity.ok(plataformaBeta.obtenerClientesConAltaProbabilidadDeCierre());
    }

    // simulación de crecimiento de demanda por sector
    @GetMapping("/rankings/demanda-sectores")
    public ResponseEntity<Map<String, Map<String, Object>>> simularCrecimientoDemanda() {
        return ResponseEntity.ok(plataformaBeta.simularCrecimientoDemandaPorSector());
    }

    // ================================================================
    // INMUEBLES — ORDENADOS Y FILTROS
    // ================================================================

    @GetMapping("/inmuebles/ordenados")
    public ResponseEntity<List<Inmueble>> inmueblesOrdenadosPorPrecio() {
        return ResponseEntity.ok(plataformaBeta.obtenerInmueblesOrdenadosPorPrecio());
    }

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