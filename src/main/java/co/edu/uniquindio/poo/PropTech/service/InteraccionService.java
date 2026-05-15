package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.exception.EntidadNoEncontradaException;
import co.edu.uniquindio.poo.PropTech.exception.ReglaNegocioException;
import co.edu.uniquindio.poo.PropTech.model.dto.InteraccionDTO;
import co.edu.uniquindio.poo.PropTech.model.dto.VisitaDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.*;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInteraccion;
import co.edu.uniquindio.poo.PropTech.repository.InteraccionRepository;
import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio central del historial de interacciones.
 *
 * Cada acción del cliente genera una Interaccion que queda registrada
 * en la SimpleLinkedList de su historial (dentro de InteraccionRepository).
 *
 * Tipos soportados:
 *   - VISITA_AGENDADA    : cliente agenda visita desde el portal
 *   - COMPRA_REALIZADA   : operación de venta cerrada
 *   - RENTA_REALIZADA    : operación de arriendo cerrada
 *   - FAVORITO_MARCADO   : cliente guarda un inmueble
 *   - INTENCION_COMPRA   : cliente declara intención formal de comprar
 *   - INTENCION_RENTA    : cliente declara intención formal de rentar
 */
@Service
public class InteraccionService {

    private final InteraccionRepository interaccionRepository;
    private final ClienteService clienteService;
    private final InmuebleService inmuebleService;
    private final AsesorService asesorService;
    private final VisitaService visitaService;

    public InteraccionService(InteraccionRepository interaccionRepository,
                              ClienteService clienteService,
                              InmuebleService inmuebleService,
                              AsesorService asesorService,
                              VisitaService visitaService) {
        this.interaccionRepository = interaccionRepository;
        this.clienteService = clienteService;
        this.inmuebleService = inmuebleService;
        this.asesorService = asesorService;
        this.visitaService = visitaService;
    }

    // ================================================================
    // REGISTRO DE INTERACCIONES
    // ================================================================

    /**
     * Registra cualquier tipo de interacción a partir del DTO.
     * Orquesta la lógica específica de cada tipo.
     */
    public Interaccion registrar(InteraccionDTO dto) {
        if (dto.getTipoInteraccion() == null) {
            throw new ReglaNegocioException("El tipo de interacción es obligatorio.");
        }

        return switch (dto.getTipoInteraccion()) {
            case VISITA_AGENDADA  -> registrarVisitaDesdePortal(dto);
            case FAVORITO_MARCADO -> registrarFavorito(dto);
            case INTENCION_COMPRA -> registrarIntencion(dto, TipoInteraccion.INTENCION_COMPRA);
            case INTENCION_RENTA  -> registrarIntencion(dto, TipoInteraccion.INTENCION_RENTA);
            case COMPRA_REALIZADA -> registrarOperacion(dto, TipoInteraccion.COMPRA_REALIZADA);
            case RENTA_REALIZADA  -> registrarOperacion(dto, TipoInteraccion.RENTA_REALIZADA);
        };
    }

    // ----------------------------------------------------------------
    // VISITA AGENDADA DESDE EL PORTAL
    // ----------------------------------------------------------------

    /**
     * El cliente agenda una visita desde el portal.
     * 1. Valida disponibilidad del inmueble.
     * 2. Auto-asigna un asesor si no se especifica uno.
     * 3. Crea la Visita vía VisitaService.
     * 4. Registra la interacción VISITA_AGENDADA.
     */
    public Interaccion registrarVisitaDesdePortal(InteraccionDTO dto) {
        Cliente cliente = clienteService.buscarPorId(dto.getIdCliente());
        Inmueble inmueble = inmuebleService.buscarPorCodigo(dto.getCodigoInmueble());

        if (!inmueble.isDisponibilidad()) {
            throw new ReglaNegocioException(
                    "El inmueble " + dto.getCodigoInmueble() + " no está disponible para visitas.");
        }
        if (dto.getFecha() == null || dto.getFecha().isBlank()) {
            throw new ReglaNegocioException("La fecha de la visita es obligatoria.");
        }
        if (dto.getHora() == null || dto.getHora().isBlank()) {
            throw new ReglaNegocioException("La hora de la visita es obligatoria.");
        }

        // Auto-asignar asesor: el del inmueble o el primero disponible
        String idAsesor = dto.getIdAsesor();
        if (idAsesor == null || idAsesor.isBlank()) {
            if (inmueble.getAsesor() != null) {
                idAsesor = inmueble.getAsesor().getId();
            } else {
                List<Asesor> asesores = asesorService.obtenerTodos();
                if (asesores.isEmpty()) {
                    throw new ReglaNegocioException("No hay asesores disponibles para asignar la visita.");
                }
                // Tomar el de menor carga
                idAsesor = asesores.stream()
                        .min((a, b) -> asesorService.contarCarga(a.getId())
                                - asesorService.contarCarga(b.getId()))
                        .map(Asesor::getId)
                        .orElse(asesores.get(0).getId());
            }
        }

        Asesor asesor = asesorService.buscarPorId(idAsesor);

        // Generar ID de visita único
        String idVisita = "V-PORT-" + System.currentTimeMillis();

        VisitaDTO visitaDTO = new VisitaDTO();
        visitaDTO.setIdVisita(idVisita);
        visitaDTO.setIdCliente(dto.getIdCliente());
        visitaDTO.setCodigoInmueble(dto.getCodigoInmueble());
        visitaDTO.setFecha(LocalDate.parse(dto.getFecha()));
        visitaDTO.setHora(LocalTime.parse(dto.getHora()));
        visitaDTO.setIdAsesor(asesor.getId());
        visitaDTO.setObservaciones(
                dto.getDetalle() != null ? dto.getDetalle() : "Agendada desde el portal del cliente");

        Visita visita = visitaService.programar(visitaDTO, cliente, inmueble, asesor);

        // Registrar en historial del cliente
        clienteService.registrarPropiedadVisitada(cliente.getId(), inmueble);
        clienteService.registrarInmuebleConsultado(cliente.getId(), inmueble);

        Interaccion interaccion = buildInteraccion(
                dto,
                cliente,
                inmueble,
                TipoInteraccion.VISITA_AGENDADA,
                "Visita agendada para el " + dto.getFecha() + " a las " + dto.getHora()
                        + " con asesor " + asesor.getNombre()
        );
        interaccion.setIdVisitaRelacionada(idVisita);

        return interaccionRepository.save(interaccion);
    }

    // ----------------------------------------------------------------
    // FAVORITO MARCADO
    // ----------------------------------------------------------------

    public Interaccion registrarFavorito(InteraccionDTO dto) {
        Cliente cliente = clienteService.buscarPorId(dto.getIdCliente());
        Inmueble inmueble = inmuebleService.buscarPorCodigo(dto.getCodigoInmueble());

        // Delegar al ClienteService (que ya valida duplicados)
        clienteService.marcarFavorito(cliente.getId(), inmueble);

        Interaccion interaccion = buildInteraccion(
                dto, cliente, inmueble,
                TipoInteraccion.FAVORITO_MARCADO,
                "Inmueble guardado como favorito: " + inmueble.getDireccion() + ", " + inmueble.getCiudad()
        );
        return interaccionRepository.save(interaccion);
    }

    // ----------------------------------------------------------------
    // INTENCIÓN DE COMPRA / RENTA
    // ----------------------------------------------------------------

    /**
     * El cliente declara formalmente su intención de comprar o rentar un inmueble.
     * No crea una operación real; notifica al sistema para seguimiento comercial.
     */
    public Interaccion registrarIntencion(InteraccionDTO dto, TipoInteraccion tipo) {
        if (dto.getCodigoInmueble() == null || dto.getCodigoInmueble().isBlank()) {
            throw new ReglaNegocioException("Debe indicar el código del inmueble de interés.");
        }

        Cliente cliente = clienteService.buscarPorId(dto.getIdCliente());
        Inmueble inmueble = inmuebleService.buscarPorCodigo(dto.getCodigoInmueble());

        if (!inmueble.isDisponibilidad()) {
            throw new ReglaNegocioException(
                    "El inmueble " + dto.getCodigoInmueble() + " ya no está disponible.");
        }

        // Registrar en historial del cliente
        clienteService.registrarInmuebleConsultado(cliente.getId(), inmueble);

        String accion = tipo == TipoInteraccion.INTENCION_COMPRA ? "compra" : "renta";
        StringBuilder detalle = new StringBuilder();
        detalle.append("Intención de ").append(accion).append(" expresada para: ")
                .append(inmueble.getDireccion()).append(", ").append(inmueble.getCiudad());

        if (dto.getPresupuestoDeclarado() != null && dto.getPresupuestoDeclarado() > 0) {
            detalle.append(" | Presupuesto declarado: $")
                    .append(String.format("%,.0f", dto.getPresupuestoDeclarado()));
        }
        if (dto.getMensajeCliente() != null && !dto.getMensajeCliente().isBlank()) {
            detalle.append(" | Mensaje: ").append(dto.getMensajeCliente());
        }

        Interaccion interaccion = buildInteraccion(dto, cliente, inmueble, tipo, detalle.toString());
        interaccion.setPresupuestoDeclarado(dto.getPresupuestoDeclarado());
        interaccion.setMensajeCliente(dto.getMensajeCliente());

        return interaccionRepository.save(interaccion);
    }

    // ----------------------------------------------------------------
    // COMPRA / RENTA REALIZADA (desde operaciones del admin)
    // ----------------------------------------------------------------

    /**
     * Llamado automáticamente cuando el admin registra una operación de venta/arriendo.
     * No lo invoca el cliente directamente.
     */
    public Interaccion registrarOperacion(InteraccionDTO dto, TipoInteraccion tipo) {
        Cliente cliente = clienteService.buscarPorId(dto.getIdCliente());
        Inmueble inmueble = inmuebleService.buscarPorCodigo(dto.getCodigoInmueble());

        String accion = tipo == TipoInteraccion.COMPRA_REALIZADA ? "Compra" : "Renta";
        String detalle = accion + " realizada: " + inmueble.getDireccion()
                + ", " + inmueble.getCiudad()
                + (dto.getDetalle() != null ? " | " + dto.getDetalle() : "");

        Interaccion interaccion = buildInteraccion(dto, cliente, inmueble, tipo, detalle);
        return interaccionRepository.save(interaccion);
    }

    /**
     * Sobrecarga para llamar desde PlataformaBeta al registrar una operación.
     */
    public Interaccion registrarOperacionDesdeOperacion(Cliente cliente, Inmueble inmueble,
                                                        TipoInteraccion tipo, String detalleExtra) {
        String accion = tipo == TipoInteraccion.COMPRA_REALIZADA ? "Compra" : "Renta";
        String detalle = accion + " realizada: " + inmueble.getDireccion()
                + ", " + inmueble.getCiudad()
                + (detalleExtra != null ? " | " + detalleExtra : "");

        Interaccion interaccion = new Interaccion();
        interaccion.setIdInteraccion(UUID.randomUUID().toString());
        interaccion.setCliente(cliente);
        interaccion.setInmueble(inmueble);
        interaccion.setTipoInteraccion(tipo);
        interaccion.setDetalle(detalle);
        interaccion.setFechaHora(LocalDateTime.now());

        return interaccionRepository.save(interaccion);
    }

    // ================================================================
    // CONSULTAS
    // ================================================================

    /**
     * Historial completo de un cliente, más reciente primero.
     * Retorna SimpleLinkedList propia del repositorio.
     */
    public SimpleLinkedList<Interaccion> obtenerHistorialCliente(String idCliente) {
        clienteService.buscarPorId(idCliente); // valida existencia
        return interaccionRepository.findByCliente(idCliente);
    }

    /**
     * Historial del cliente como java.util.List para serialización JSON.
     */
    public List<Interaccion> obtenerHistorialClienteLista(String idCliente) {
        clienteService.buscarPorId(idCliente);
        List<Interaccion> lista = new ArrayList<>();
        for (Interaccion i : interaccionRepository.findByCliente(idCliente)) {
            lista.add(i);
        }
        return lista;
    }

    public List<Interaccion> obtenerPorClienteYTipo(String idCliente, TipoInteraccion tipo) {
        return interaccionRepository.findByClienteYTipo(idCliente, tipo);
    }

    public List<Interaccion> obtenerTodas() {
        return interaccionRepository.findAll();
    }

    public int contarInteraccionesCliente(String idCliente) {
        return interaccionRepository.countByCliente(idCliente);
    }

    // ================================================================
    // HELPER PRIVADO
    // ================================================================

    private Interaccion buildInteraccion(InteraccionDTO dto, Cliente cliente,
                                         Inmueble inmueble, TipoInteraccion tipo,
                                         String detalle) {
        Interaccion i = new Interaccion();
        i.setIdInteraccion(
                dto.getIdInteraccion() != null && !dto.getIdInteraccion().isBlank()
                        ? dto.getIdInteraccion()
                        : UUID.randomUUID().toString()
        );
        i.setCliente(cliente);
        i.setInmueble(inmueble);
        i.setTipoInteraccion(tipo);
        i.setDetalle(detalle);
        i.setFechaHora(LocalDateTime.now());
        return i;
    }
}