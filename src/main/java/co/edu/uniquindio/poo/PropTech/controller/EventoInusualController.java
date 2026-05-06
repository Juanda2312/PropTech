package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.model.dto.EventoInusualDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.EventoInusual;
import co.edu.uniquindio.poo.PropTech.model.enums.NivelAtencion;
import co.edu.uniquindio.poo.PropTech.service.EventoInusualService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoInusualController {

    private final EventoInusualService eventoService;

    public EventoInusualController(EventoInusualService eventoService) {
        this.eventoService = eventoService;
    }

    // ----------------------------------------------------------------
    // POST /api/eventos
    // Registra manualmente un evento inusual y genera su alerta asociada.
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<EventoInusual> registrar(@RequestBody EventoInusualDTO dto) {
        EventoInusual creado = eventoService.registrar(
                dto.getIdEvento(),
                dto.getTipoEvento(),
                dto.getDescripcion(),
                dto.getNivelAtencion()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ----------------------------------------------------------------
    // GET /api/eventos
    // ?nivel=ALTO      → filtra por nivel de atención
    // ?activos=true    → solo eventos en estado ACTIVO
    // (sin params)     → todos los eventos
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<EventoInusual>> listar(
            @RequestParam(required = false) NivelAtencion nivel,
            @RequestParam(required = false) Boolean activos) {

        if (nivel != null) {
            return ResponseEntity.ok(eventoService.obtenerPorNivel(nivel));
        }
        if (Boolean.TRUE.equals(activos)) {
            return ResponseEntity.ok(eventoService.obtenerActivos());
        }
        return ResponseEntity.ok(eventoService.obtenerTodos());
    }

    // ----------------------------------------------------------------
    // PATCH /api/eventos/{id}/cerrar
    // ----------------------------------------------------------------
    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<Void> cerrar(@PathVariable String id) {
        eventoService.cerrar(id);
        return ResponseEntity.noContent().build();
    }
}
