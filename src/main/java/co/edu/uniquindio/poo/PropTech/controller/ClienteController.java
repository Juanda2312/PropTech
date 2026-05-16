package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.model.dto.ClienteDTO;
import co.edu.uniquindio.poo.PropTech.model.dto.IntencionDTO;
import co.edu.uniquindio.poo.PropTech.model.dto.VisitaDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Interaccion;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInteraccion;
import co.edu.uniquindio.poo.PropTech.service.ClienteService;
import co.edu.uniquindio.poo.PropTech.service.InmuebleService;
import co.edu.uniquindio.poo.PropTech.service.PlataformaBeta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class    ClienteController {

    private final ClienteService  clienteService;
    private final InmuebleService inmuebleService;
    private final PlataformaBeta  plataformaBeta;

    public ClienteController(ClienteService clienteService,
                             InmuebleService inmuebleService,
                             PlataformaBeta plataformaBeta) {
        this.clienteService  = clienteService;
        this.inmuebleService = inmuebleService;
        this.plataformaBeta  = plataformaBeta;
    }

    // ----------------------------------------------------------------
    // CRUD
    // ----------------------------------------------------------------

    @PostMapping
    public ResponseEntity<Cliente> registrar(@RequestBody ClienteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.registrar(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listar(
            @RequestParam(required = false, defaultValue = "false") boolean ordenarPorPresupuesto,
            @RequestParam(required = false, defaultValue = "0") double presupuestoMax) {

        if (ordenarPorPresupuesto) {
            return ResponseEntity.ok(clienteService.obtenerOrdenadosPorPresupuesto());
        }
        if (presupuestoMax > 0) {
            return ResponseEntity.ok(clienteService.obtenerPorPresupuestoMaximo(presupuestoMax));
        }
        return ResponseEntity.ok(clienteService.obtenerTodos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable String id,
                                           @RequestBody ClienteDTO dto) {
        clienteService.actualizar(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // Favoritos
    // ----------------------------------------------------------------

    @PostMapping("/{id}/favoritos/{codigoInmueble}")
    public ResponseEntity<Void> marcarFavorito(@PathVariable String id,
                                               @PathVariable String codigoInmueble) {
        Inmueble inmueble = inmuebleService.buscarPorCodigo(codigoInmueble);
        clienteService.marcarFavorito(id, inmueble);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/favoritos/{codigoInmueble}")
    public ResponseEntity<Void> eliminarFavorito(@PathVariable String id,
                                                 @PathVariable String codigoInmueble) {
        clienteService.eliminarFavorito(id, codigoInmueble);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/favoritos")
    public ResponseEntity<List<Inmueble>> obtenerFavoritos(@PathVariable String id) {
        Cliente cliente = clienteService.buscarPorId(id);
        List<Inmueble> favoritos = new java.util.ArrayList<>();
        cliente.getInmueblesGuardados().forEach(favoritos::add);
        return ResponseEntity.ok(favoritos);
    }

    @PostMapping("/{id}/descartados/{codigoInmueble}")
    public ResponseEntity<Void> descartarInmueble(@PathVariable String id,
                                                  @PathVariable String codigoInmueble) {
        Inmueble inmueble = inmuebleService.buscarPorCodigo(codigoInmueble);
        clienteService.registrarInmuebleDescartado(id, inmueble);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<Inmueble>> obtenerHistorial(@PathVariable String id) {
        Cliente cliente = clienteService.buscarPorId(id);
        // LinkedHashSet para eliminar duplicados manteniendo orden de inserción
        java.util.LinkedHashSet<Inmueble> set = new java.util.LinkedHashSet<>();
        cliente.getInmueblesConsultados().forEach(set::add);
        cliente.getPropiedadesVisitadas().forEach(set::add);
        cliente.getInmueblesNegociados().forEach(set::add);
        return ResponseEntity.ok(new java.util.ArrayList<>(set));
    }

    // ----------------------------------------------------------------
    // Historial de interacciones
    // GET /api/clientes/{id}/interacciones
    // GET /api/clientes/{id}/interacciones?tipo=INTENCION_COMPRA
    // ----------------------------------------------------------------

    @GetMapping("/{id}/interacciones")
    public ResponseEntity<List<Interaccion>> obtenerInteracciones(
            @PathVariable String id,
            @RequestParam(required = false) TipoInteraccion tipo) {

        if (tipo != null) {
            return ResponseEntity.ok(clienteService.obtenerHistorialPorTipo(id, tipo));
        }
        return ResponseEntity.ok(clienteService.obtenerHistorial(id));
    }

    // ----------------------------------------------------------------
    // Intención de compra o renta
    // POST /api/clientes/{id}/intencion
    // Body: { "codigoInmueble": "...", "tipo": "INTENCION_COMPRA", "detalle": "..." }
    // ----------------------------------------------------------------

    @PostMapping("/{id}/intencion")
    public ResponseEntity<Interaccion> registrarIntencion(
            @PathVariable String id,
            @RequestBody IntencionDTO dto) {

        if (dto.getTipo() != TipoInteraccion.INTENCION_COMPRA
                && dto.getTipo() != TipoInteraccion.INTENCION_RENTA) {
            return ResponseEntity.badRequest().build();
        }

        Inmueble inmueble = inmuebleService.buscarPorCodigo(dto.getCodigoInmueble());
        String descripcion = (dto.getTipo() == TipoInteraccion.INTENCION_COMPRA
                ? "Intención de compra"
                : "Intención de renta")
                + " para " + inmueble.getDireccion()
                + ", " + inmueble.getCiudad()
                + (dto.getDetalle() != null && !dto.getDetalle().isBlank()
                ? " — " + dto.getDetalle() : "");

        Interaccion interaccion = clienteService.registrarInteraccion(id, dto.getTipo(), inmueble, descripcion);
        // También registrar en historial de consultas
        clienteService.buscarPorId(id).getInmueblesConsultados().addLast(inmueble);

        return ResponseEntity.status(HttpStatus.CREATED).body(interaccion);
    }

    // ----------------------------------------------------------------
    // Agendar visita desde el portal del cliente
    // POST /api/clientes/{id}/visitas
    // ----------------------------------------------------------------

    @PostMapping("/{id}/visitas")
    public ResponseEntity<Interaccion> agendarVisitaDesdePortal(
            @PathVariable String id,
            @RequestBody VisitaDTO dto) {

        dto.setIdCliente(id);
        // Delegar a PlataformaBeta que orquesta todo y actualiza el grafo
        plataformaBeta.agendarVisita(dto);

        Inmueble inmueble = inmuebleService.buscarPorCodigo(dto.getCodigoInmueble());
        String descripcion = "Visita agendada para el " + dto.getFecha()
                + " a las " + dto.getHora()
                + " en " + inmueble.getDireccion() + ", " + inmueble.getCiudad();

        Interaccion interaccion = clienteService.registrarInteraccion(
                id, TipoInteraccion.VISITA_AGENDADA, inmueble, descripcion);

        return ResponseEntity.status(HttpStatus.CREATED).body(interaccion);
    }

    // Registrar consulta de inmueble desde el portal del cliente
    // POST /api/clientes/{id}/consulta/{codigoInmueble}
    // Se llama cuando el cliente interactúa con una tarjeta de inmueble
    // (hover, click en acción) para registrar el interés en el historial.
    // ----------------------------------------------------------------

    @PostMapping("/{id}/consulta/{codigoInmueble}")
    public ResponseEntity<Interaccion> registrarConsulta(
            @PathVariable String id,
            @PathVariable String codigoInmueble) {

        Inmueble inmueble = inmuebleService.buscarPorCodigo(codigoInmueble);

        // Agrega el inmueble a inmueblesConsultados (lista para el historial)
        clienteService.registrarInmuebleConsultado(id, inmueble);

        // También registra en el historial unificado de interacciones
        Interaccion interaccion = clienteService.registrarInteraccion(
                id,
                TipoInteraccion.INMUEBLE_CONSULTADO,
                inmueble,
                "Inmueble consultado: " + inmueble.getDireccion() + ", " + inmueble.getCiudad()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(interaccion);
    }

}