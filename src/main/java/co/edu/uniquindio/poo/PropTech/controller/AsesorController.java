package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.model.dto.AsesorDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.service.AsesorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asesores")
public class AsesorController {

    private final AsesorService asesorService;

    public AsesorController(AsesorService asesorService) {
        this.asesorService = asesorService;
    }

    // ----------------------------------------------------------------
    // POST /api/asesores
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<Asesor> registrar(@RequestBody AsesorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asesorService.registrar(dto));
    }

    // ----------------------------------------------------------------
    // GET /api/asesores/{id}
    // ----------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Asesor> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(asesorService.buscarPorId(id));
    }

    // ----------------------------------------------------------------
    // GET /api/asesores
    // ?ranking=true  → ordenados por número de cierres (recorrido desc del AVL)
    // (sin params)   → lista completa
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Asesor>> listar(
            @RequestParam(required = false, defaultValue = "false") boolean ranking) {
        if (ranking) {
            return ResponseEntity.ok(asesorService.obtenerRankingPorCierres());
        }
        return ResponseEntity.ok(asesorService.obtenerTodos());
    }

    // ----------------------------------------------------------------
    // PUT /api/asesores/{id}
    // ----------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable String id,
                                           @RequestBody AsesorDTO dto) {
        asesorService.actualizar(id, dto);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // GET /api/asesores/{id}/carga
    // Devuelve la carga actual del asesor: visitas agendadas + inmuebles asignados.
    // ----------------------------------------------------------------
    @GetMapping("/{id}/carga")
    public ResponseEntity<Map<String, Integer>> obtenerCarga(@PathVariable String id) {
        int total = asesorService.contarCarga(id);
        return ResponseEntity.ok(Map.of("cargaTotal", total));
    }
}
