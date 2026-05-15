package co.edu.uniquindio.poo.PropTech.controller;

import co.edu.uniquindio.poo.PropTech.model.dto.ClienteDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.service.ClienteService;
import co.edu.uniquindio.poo.PropTech.service.InmuebleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService  clienteService;
    private final InmuebleService inmuebleService;

    public ClienteController(ClienteService clienteService, InmuebleService inmuebleService) {
        this.clienteService  = clienteService;
        this.inmuebleService = inmuebleService;
    }

    // ----------------------------------------------------------------
    // POST /api/clientes
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<Cliente> registrar(@RequestBody ClienteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.registrar(dto));
    }

    // ----------------------------------------------------------------
    // GET /api/clientes/{id}
    // ----------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    // ----------------------------------------------------------------
    // GET /api/clientes
    // ?ordenarPorPresupuesto=true  → recorrido inOrder del AVL
    // ?presupuestoMax=500000       → filtra por presupuesto máximo
    // (sin params)                 → lista completa
    // ----------------------------------------------------------------
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

    // ----------------------------------------------------------------
    // PUT /api/clientes/{id}
    // ----------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable String id,
                                           @RequestBody ClienteDTO dto) {
        clienteService.actualizar(id, dto);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // DELETE /api/clientes/{id}
    // ----------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // POST /api/clientes/{id}/favoritos/{codigoInmueble}
    // Marca un inmueble como favorito para el cliente.
    // ----------------------------------------------------------------
    @PostMapping("/{id}/favoritos/{codigoInmueble}")
    public ResponseEntity<Void> marcarFavorito(@PathVariable String id,
                                               @PathVariable String codigoInmueble) {
        Inmueble inmueble = inmuebleService.buscarPorCodigo(codigoInmueble);
        clienteService.marcarFavorito(id, inmueble);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // POST /api/clientes/{id}/descartados/{codigoInmueble}
    // Registra un inmueble como descartado por el cliente.
    // ----------------------------------------------------------------
    @PostMapping("/{id}/descartados/{codigoInmueble}")
    public ResponseEntity<Void> descartarInmueble(@PathVariable String id,
                                                  @PathVariable String codigoInmueble) {
        Inmueble inmueble = inmuebleService.buscarPorCodigo(codigoInmueble);
        clienteService.registrarInmuebleDescartado(id, inmueble);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // GET /api/clientes/{id}/favoritos
    // Devuelve la lista de inmuebles guardados del cliente.
    // ----------------------------------------------------------------
    @GetMapping("/{id}/favoritos")
    public ResponseEntity<List<Inmueble>> obtenerFavoritos(@PathVariable String id) {
        Cliente cliente = clienteService.buscarPorId(id);
        List<Inmueble> favoritos = new java.util.ArrayList<>();
        cliente.getInmueblesGuardados().forEach(favoritos::add);
        return ResponseEntity.ok(favoritos);
    }

    // ----------------------------------------------------------------
    // GET /api/clientes/{id}/historial
    // Devuelve el historial completo de inmuebles consultados.
    // ----------------------------------------------------------------
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<Inmueble>> obtenerHistorial(@PathVariable String id) {
        Cliente cliente = clienteService.buscarPorId(id);
        List<Inmueble> historial = new java.util.ArrayList<>();
        cliente.getInmueblesConsultados().forEach(historial::add);
        return ResponseEntity.ok(historial);
    }

    @DeleteMapping("/{id}/favoritos/{codigoInmueble}")
    public ResponseEntity<Void> eliminarFavorito(@PathVariable String id,
                                                 @PathVariable String codigoInmueble) {
        clienteService.eliminarFavorito(id, codigoInmueble);
        return ResponseEntity.noContent().build();
    }
}
