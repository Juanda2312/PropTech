package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.model.entity.Alerta;
import co.edu.uniquindio.poo.PropTech.model.enums.NivelAtencion;
import co.edu.uniquindio.poo.PropTech.service.AlertaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    // ----------------------------------------------------------------
    // GET /api/alertas
    // ?nivel=CRITICO   → filtra por nivel de atención
    // ?abiertas=true   → solo alertas no cerradas
    // (sin params)     → todas las alertas
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Alerta>> listar(
            @RequestParam(required = false) NivelAtencion nivel,
            @RequestParam(required = false) Boolean abiertas) {

        if (nivel != null) {
            return ResponseEntity.ok(alertaService.obtenerPorNivel(nivel));
        }
        if (Boolean.TRUE.equals(abiertas)) {
            return ResponseEntity.ok(alertaService.obtenerAbiertas());
        }
        return ResponseEntity.ok(alertaService.obtenerTodas());
    }

    // ----------------------------------------------------------------
    // PATCH /api/alertas/{id}/cerrar
    // ----------------------------------------------------------------
    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<Void> cerrar(@PathVariable String id) {
        alertaService.cerrar(id);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // POST /api/alertas/pendientes/procesar
    // Saca y devuelve la siguiente alerta de la cola FIFO de pendientes.
    // ----------------------------------------------------------------
    @PostMapping("/pendientes/procesar")
    public ResponseEntity<Alerta> procesarSiguiente() {
        return ResponseEntity.ok(alertaService.procesarSiguiente());
    }

    // ----------------------------------------------------------------
    // GET /api/alertas/pendientes/total
    // ----------------------------------------------------------------
    @GetMapping("/pendientes/total")
    public ResponseEntity<Map<String, Integer>> totalPendientes() {
        return ResponseEntity.ok(Map.of("totalPendientes", alertaService.totalPendientes()));
    }
}
