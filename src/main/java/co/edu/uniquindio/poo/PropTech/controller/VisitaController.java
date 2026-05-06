package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.model.dto.VisitaDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Visita;
import co.edu.uniquindio.poo.PropTech.model.enums.EstadoVisita;
import co.edu.uniquindio.poo.PropTech.service.AsesorService;
import co.edu.uniquindio.poo.PropTech.service.ClienteService;
import co.edu.uniquindio.poo.PropTech.service.InmuebleService;
import co.edu.uniquindio.poo.PropTech.service.VisitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visitas")
public class VisitaController {

    private final VisitaService   visitaService;
    private final ClienteService  clienteService;
    private final InmuebleService inmuebleService;
    private final AsesorService   asesorService;

    public VisitaController(VisitaService visitaService,
                            ClienteService clienteService,
                            InmuebleService inmuebleService,
                            AsesorService asesorService) {
        this.visitaService   = visitaService;
        this.clienteService  = clienteService;
        this.inmuebleService = inmuebleService;
        this.asesorService   = asesorService;
    }

    // ----------------------------------------------------------------
    // POST /api/visitas
    // Programa una nueva visita. Resuelve las entidades relacionadas
    // antes de delegar al servicio.
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<Visita> programar(@RequestBody VisitaDTO dto) {
        Cliente  cliente  = clienteService.buscarPorId(dto.getIdCliente());
        Inmueble inmueble = inmuebleService.buscarPorCodigo(dto.getCodigoInmueble());
        Asesor   asesor   = asesorService.buscarPorId(dto.getIdAsesor());
        Visita   visita   = visitaService.programar(dto, cliente, inmueble, asesor);
        return ResponseEntity.status(HttpStatus.CREATED).body(visita);
    }

    // ----------------------------------------------------------------
    // GET /api/visitas/{id}
    // ----------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Visita> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(visitaService.buscarPorId(id));
    }

    // ----------------------------------------------------------------
    // GET /api/visitas
    // ?estado=PENDIENTE          → filtra por estado
    // ?idCliente=CLI-01          → filtra por cliente
    // ?codigoInmueble=INM-01     → filtra por inmueble
    // (sin params)               → todas las visitas
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Visita>> listar(
            @RequestParam(required = false) EstadoVisita estado,
            @RequestParam(required = false) String idCliente,
            @RequestParam(required = false) String codigoInmueble) {

        if (estado != null) {
            return ResponseEntity.ok(visitaService.obtenerPorEstado(estado));
        }
        if (idCliente != null) {
            return ResponseEntity.ok(visitaService.obtenerPorCliente(idCliente));
        }
        if (codigoInmueble != null) {
            return ResponseEntity.ok(visitaService.obtenerPorInmueble(codigoInmueble));
        }
        return ResponseEntity.ok(visitaService.obtenerTodas());
    }

    // ----------------------------------------------------------------
    // PATCH /api/visitas/{id}/confirmar
    // ----------------------------------------------------------------
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<Void> confirmar(@PathVariable String id) {
        visitaService.confirmar(id);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // PATCH /api/visitas/{id}/cancelar
    // Body: { "observaciones": "motivo de cancelación" }
    // ----------------------------------------------------------------
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable String id,
                                         @RequestBody Map<String, String> body) {
        visitaService.cancelar(id, body.getOrDefault("observaciones", ""));
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // PATCH /api/visitas/{id}/reprogramar
    // Body: nuevo VisitaDTO con fecha y hora actualizadas.
    // ----------------------------------------------------------------
    @PatchMapping("/{id}/reprogramar")
    public ResponseEntity<Void> reprogramar(@PathVariable String id,
                                            @RequestBody VisitaDTO dto) {
        visitaService.reprogramar(id, dto);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // PATCH /api/visitas/{id}/realizar
    // Body: { "observaciones": "notas de la visita realizada" }
    // ----------------------------------------------------------------
    @PatchMapping("/{id}/realizar")
    public ResponseEntity<Void> marcarRealizada(@PathVariable String id,
                                                @RequestBody Map<String, String> body) {
        visitaService.marcarRealizada(id, body.getOrDefault("observaciones", ""));
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // POST /api/visitas/pendientes/procesar
    // Saca y devuelve la siguiente visita de la cola FIFO de pendientes.
    // ----------------------------------------------------------------
    @PostMapping("/pendientes/procesar")
    public ResponseEntity<Visita> procesarSiguiente() {
        return ResponseEntity.ok(visitaService.procesarSiguientePendiente());
    }

    // ----------------------------------------------------------------
    // GET /api/visitas/pendientes/total
    // Devuelve el tamaño actual de la cola de pendientes.
    // ----------------------------------------------------------------
    @GetMapping("/pendientes/total")
    public ResponseEntity<Map<String, Integer>> totalPendientes() {
        return ResponseEntity.ok(Map.of("totalPendientes", visitaService.totalPendientes()));
    }
}
