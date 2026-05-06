package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.model.dto.InmuebleDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.FinalidadInmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInmueble;
import co.edu.uniquindio.poo.PropTech.service.AsesorService;
import co.edu.uniquindio.poo.PropTech.service.InmuebleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inmuebles")
public class InmuebleController {

    private final InmuebleService inmuebleService;
    private final AsesorService   asesorService;

    public InmuebleController(InmuebleService inmuebleService, AsesorService asesorService) {
        this.inmuebleService = inmuebleService;
        this.asesorService   = asesorService;
    }

    // ----------------------------------------------------------------
    // POST /api/inmuebles
    // Registra un nuevo inmueble y lo asigna al asesor indicado en el DTO.
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<Inmueble> registrar(@RequestBody InmuebleDTO dto) {
        Asesor asesor   = asesorService.buscarPorId(dto.getIdAsesor());
        Inmueble creado = inmuebleService.registrar(dto, asesor);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ----------------------------------------------------------------
    // GET /api/inmuebles/{codigo}
    // ----------------------------------------------------------------
    @GetMapping("/{codigo}")
    public ResponseEntity<Inmueble> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(inmuebleService.buscarPorCodigo(codigo));
    }

    // ----------------------------------------------------------------
    // GET /api/inmuebles
    // Devuelve todos los inmuebles ordenados por precio (recorrido inOrder del AVL).
    // Acepta filtros opcionales por query param:
    //   ?tipo=APARTAMENTO
    //   ?finalidad=VENTA
    //   ?ciudad=Bogota
    //   ?disponible=true
    //   ?precioMin=100000&precioMax=500000
    //   ?tipo=CASA&finalidad=ARRIENDO&ciudad=Cali&precioMax=800000&habitacionesMin=2
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Inmueble>> listar(
            @RequestParam(required = false) TipoInmueble tipo,
            @RequestParam(required = false) FinalidadInmueble finalidad,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(required = false, defaultValue = "0") double precioMin,
            @RequestParam(required = false, defaultValue = "0") double precioMax,
            @RequestParam(required = false, defaultValue = "0") int habitacionesMin) {

        // Filtro combinado cuando se pasa más de un criterio
        boolean usarCombinado = tipo != null || finalidad != null
                || ciudad != null || precioMax > 0 || habitacionesMin > 0;

        if (usarCombinado) {
            return ResponseEntity.ok(
                    inmuebleService.filtrarCombinado(tipo, finalidad, ciudad, precioMax, habitacionesMin));
        }
        if (disponible != null) {
            return ResponseEntity.ok(inmuebleService.filtrarDisponibles());
        }
        if (precioMin > 0 && precioMax > 0) {
            return ResponseEntity.ok(inmuebleService.filtrarPorRangoPrecio(precioMin, precioMax));
        }

        return ResponseEntity.ok(inmuebleService.obtenerTodos());
    }

    // ----------------------------------------------------------------
    // PUT /api/inmuebles/{codigo}
    // ----------------------------------------------------------------
    @PutMapping("/{codigo}")
    public ResponseEntity<Void> actualizar(@PathVariable String codigo,
                                           @RequestBody InmuebleDTO dto) {
        Asesor asesor = asesorService.buscarPorId(dto.getIdAsesor());
        inmuebleService.actualizar(codigo, dto, asesor);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // DELETE /api/inmuebles/{codigo}
    // ----------------------------------------------------------------
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        inmuebleService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // POST /api/inmuebles/deshacer
    // Deshace el último cambio sobre cualquier inmueble (usa el Stack interno).
    // ----------------------------------------------------------------
    @PostMapping("/deshacer")
    public ResponseEntity<Void> deshacerUltimoCambio() {
        inmuebleService.deshacerUltimoCambio();
        return ResponseEntity.noContent().build();
    }
}
