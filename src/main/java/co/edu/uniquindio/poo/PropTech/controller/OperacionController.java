package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.model.dto.OperacionDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Operacion;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoOperacion;
import co.edu.uniquindio.poo.PropTech.service.AsesorService;
import co.edu.uniquindio.poo.PropTech.service.ClienteService;
import co.edu.uniquindio.poo.PropTech.service.InmuebleService;
import co.edu.uniquindio.poo.PropTech.service.OperacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operaciones")
public class OperacionController {

    private final OperacionService operacionService;
    private final InmuebleService  inmuebleService;
    private final ClienteService   clienteService;
    private final AsesorService    asesorService;

    public OperacionController(OperacionService operacionService,
                               InmuebleService inmuebleService,
                               ClienteService clienteService,
                               AsesorService asesorService) {
        this.operacionService = operacionService;
        this.inmuebleService  = inmuebleService;
        this.clienteService   = clienteService;
        this.asesorService    = asesorService;
    }

    // ----------------------------------------------------------------
    // POST /api/operaciones
    // Registra una operación de venta, arriendo, renovación o cancelación.
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<Operacion> registrar(@RequestBody OperacionDTO dto) {
        Inmueble inmueble = inmuebleService.buscarPorCodigo(dto.getCodigoInmueble());
        Cliente  cliente  = clienteService.buscarPorId(dto.getIdCliente());
        Asesor   asesor   = asesorService.buscarPorId(dto.getIdAsesor());
        Operacion creada  = operacionService.registrar(dto, inmueble, cliente, asesor);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // ----------------------------------------------------------------
    // GET /api/operaciones/{id}
    // ----------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Operacion> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(operacionService.buscarPorId(id));
    }

    // ----------------------------------------------------------------
    // GET /api/operaciones
    // ?tipo=VENTA               → filtra por tipo de operación
    // ?idCliente=CLI-01         → filtra por cliente
    // ?idAsesor=ASR-01          → filtra por asesor
    // (sin params)              → todas las operaciones
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Operacion>> listar(
            @RequestParam(required = false) TipoOperacion tipo,
            @RequestParam(required = false) String idCliente,
            @RequestParam(required = false) String idAsesor) {

        if (tipo != null) {
            return ResponseEntity.ok(operacionService.obtenerPorTipo(tipo));
        }
        if (idCliente != null) {
            return ResponseEntity.ok(operacionService.obtenerPorCliente(idCliente));
        }
        if (idAsesor != null) {
            return ResponseEntity.ok(operacionService.obtenerPorAsesor(idAsesor));
        }
        return ResponseEntity.ok(operacionService.obtenerTodas());
    }

    // ----------------------------------------------------------------
    // PATCH /api/operaciones/{id}/cancelar
    // ----------------------------------------------------------------
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable String id) {
        operacionService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // PATCH /api/operaciones/{id}/cerrar
    // ----------------------------------------------------------------
    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<Void> cerrar(@PathVariable String id) {
        operacionService.cerrar(id);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // GET /api/operaciones/comisiones/{idAsesor}
    // Devuelve la suma total de comisiones generadas por un asesor.
    // ----------------------------------------------------------------
    @GetMapping("/comisiones/{idAsesor}")
    public ResponseEntity<Map<String, Double>> calcularComisiones(@PathVariable String idAsesor) {
        double total = operacionService.calcularTotalComisiones(idAsesor);
        return ResponseEntity.ok(Map.of("totalComisiones", total));
    }
}
