package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.model.dto.InteraccionDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Interaccion;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInteraccion;
import co.edu.uniquindio.poo.PropTech.service.InteraccionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints para el historial de interacciones del cliente.
 *
 * Base: /api/interacciones
 *
 * POST /api/interacciones
 *   Registra cualquier tipo de interacción (VISITA_AGENDADA, FAVORITO_MARCADO,
 *   INTENCION_COMPRA, INTENCION_RENTA, COMPRA_REALIZADA, RENTA_REALIZADA).
 *
 * GET  /api/interacciones/cliente/{idCliente}
 *   Historial completo del cliente, más reciente primero.
 *
 * GET  /api/interacciones/cliente/{idCliente}?tipo=INTENCION_COMPRA
 *   Filtrado por tipo.
 *
 * GET  /api/interacciones
 *   Todas las interacciones del sistema (uso admin).
 */
@RestController
@RequestMapping("/api/interacciones")
public class InteraccionController {

    private final InteraccionService interaccionService;

    public InteraccionController(InteraccionService interaccionService) {
        this.interaccionService = interaccionService;
    }

    // ----------------------------------------------------------------
    // POST /api/interacciones
    // Registra una interacción. El tipo determina la lógica interna.
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<Interaccion> registrar(@RequestBody InteraccionDTO dto) {
        Interaccion creada = interaccionService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // ----------------------------------------------------------------
    // GET /api/interacciones/cliente/{idCliente}
    // ?tipo=INTENCION_COMPRA  → filtra por tipo
    // (sin params)            → historial completo
    // ----------------------------------------------------------------
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<Interaccion>> listarPorCliente(
            @PathVariable String idCliente,
            @RequestParam(required = false) TipoInteraccion tipo) {

        if (tipo != null) {
            return ResponseEntity.ok(
                    interaccionService.obtenerPorClienteYTipo(idCliente, tipo));
        }
        return ResponseEntity.ok(
                interaccionService.obtenerHistorialClienteLista(idCliente));
    }

    // ----------------------------------------------------------------
    // GET /api/interacciones/cliente/{idCliente}/total
    // ----------------------------------------------------------------
    @GetMapping("/cliente/{idCliente}/total")
    public ResponseEntity<java.util.Map<String, Integer>> totalInteracciones(
            @PathVariable String idCliente) {
        return ResponseEntity.ok(java.util.Map.of(
                "total", interaccionService.contarInteraccionesCliente(idCliente)));
    }

    // ----------------------------------------------------------------
    // GET /api/interacciones  (admin)
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Interaccion>> listarTodas() {
        return ResponseEntity.ok(interaccionService.obtenerTodas());
    }
}