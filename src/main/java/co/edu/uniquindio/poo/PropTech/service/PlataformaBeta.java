package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.dto.*;
import co.edu.uniquindio.poo.PropTech.model.entity.*;
import co.edu.uniquindio.poo.PropTech.model.enums.*;
import co.edu.uniquindio.poo.PropTech.structures.Graph;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PlataformaBeta {

    private final InmuebleService       inmuebleService;
    private final ClienteService        clienteService;
    private final AsesorService         asesorService;
    private final VisitaService         visitaService;
    private final OperacionService      operacionService;
    private final AlertaService         alertaService;
    private final EventoInusualService  eventoService;
    private final RecomendacionService  recomendacionService;

    // Grafo de relaciones cliente ↔ inmueble (no dirigido)
    private final Graph<String> grafoRelaciones = new Graph<>(false);

    public PlataformaBeta(InmuebleService inmuebleService,
                          ClienteService clienteService,
                          AsesorService asesorService,
                          VisitaService visitaService,
                          OperacionService operacionService,
                          AlertaService alertaService,
                          EventoInusualService eventoService,
                          RecomendacionService recomendacionService) {
        this.inmuebleService      = inmuebleService;
        this.clienteService       = clienteService;
        this.asesorService        = asesorService;
        this.visitaService        = visitaService;
        this.operacionService     = operacionService;
        this.alertaService        = alertaService;
        this.eventoService        = eventoService;
        this.recomendacionService = recomendacionService;
    }

    // ================================================================
    // REGISTRO DE ENTIDADES
    // ================================================================

    public Inmueble registrarInmueble(InmuebleDTO dto) {
        Asesor asesor = asesorService.buscarPorId(dto.getIdAsesor());
        Inmueble inmueble = inmuebleService.registrar(dto, asesor);
        asesorService.asignarInmueble(asesor.getId(), inmueble);
        grafoRelaciones.addVertex(inmueble.getCodigo());
        return inmueble;
    }

    public Cliente registrarCliente(ClienteDTO dto) {
        Cliente cliente = clienteService.registrar(dto);
        grafoRelaciones.addVertex(cliente.getId());
        return cliente;
    }

    public Asesor registrarAsesor(AsesorDTO dto) {
        return asesorService.registrar(dto);
    }

    // ================================================================
    // VISITAS
    // ================================================================

    public Visita agendarVisita(VisitaDTO dto) {
        Cliente  cliente  = clienteService.buscarPorId(dto.getIdCliente());
        Inmueble inmueble = inmuebleService.buscarPorCodigo(dto.getCodigoInmueble());
        Asesor   asesor   = asesorService.buscarPorId(dto.getIdAsesor());

        Visita visita = visitaService.programar(dto, cliente, inmueble, asesor);

        asesorService.agregarVisitaAgendada(asesor.getId(), visita);
        clienteService.registrarPropiedadVisitada(cliente.getId(), inmueble);
        clienteService.registrarInmuebleConsultado(cliente.getId(), inmueble);

        // Registramos la relación en el grafo
        grafoRelaciones.addEdge(cliente.getId(), inmueble.getCodigo());

        // Detección de comportamiento inusual: cliente con muchas visitas recientes
        detectarExcesoVisitasCliente(cliente.getId());

        // Detección de sobrecarga del asesor
        detectarSobrecargaAsesor(asesor.getId());

        return visita;
    }

    // ================================================================
    // OPERACIONES DE NEGOCIO
    // ================================================================

    public Operacion registrarOperacion(OperacionDTO dto) {
        Inmueble inmueble = inmuebleService.buscarPorCodigo(dto.getCodigoInmueble());
        Cliente  cliente  = clienteService.buscarPorId(dto.getIdCliente());
        Asesor   asesor   = asesorService.buscarPorId(dto.getIdAsesor());

        Operacion operacion = operacionService.registrar(dto, inmueble, cliente, asesor);

        asesorService.registrarCierre(asesor.getId(), operacion);
        clienteService.registrarInmuebleNegociado(cliente.getId(), inmueble);

        return operacion;
    }

    // ================================================================
    // ALERTAS AUTOMÁTICAS
    // ================================================================

    public List<Alerta> generarAlertas() {
        List<Alerta> alertasGeneradas = new ArrayList<>();

        alertasGeneradas.addAll(alertarInmueblesInactivos());
        alertasGeneradas.addAll(alertarVisitasPendientes());
        alertasGeneradas.addAll(alertarClientesSinSeguimiento());

        return alertasGeneradas;
    }

    // Inmuebles disponibles sin ninguna visita registrada
    private List<Alerta> alertarInmueblesInactivos() {
        List<Alerta> alertas = new ArrayList<>();

        for (Inmueble i : inmuebleService.obtenerTodos()) {
            if (i.isDisponibilidad() && i.getListaVisitas().isEmpty()) {
                Alerta alerta = alertaService.generar(
                        "ALT-INM-" + i.getCodigo(),
                        "INMUEBLE_INACTIVO",
                        "El inmueble " + i.getCodigo() + " no ha recibido visitas",
                        NivelAtencion.BAJO
                );
                alertas.add(alerta);
            }
        }

        return alertas;
    }

    // Visitas que llevan más de 3 días en estado PENDIENTE sin confirmar
    private List<Alerta> alertarVisitasPendientes() {
        List<Alerta> alertas = new ArrayList<>();

        for (Visita v : visitaService.obtenerPorEstado(EstadoVisita.PENDIENTE)) {
            long diasEspera = ChronoUnit.DAYS.between(v.getFecha(), LocalDate.now());
            if (diasEspera > 3) {
                Alerta alerta = alertaService.generar(
                        "ALT-VIS-" + v.getIdVisita(),
                        "VISITA_PENDIENTE",
                        "La visita " + v.getIdVisita() + " lleva " + diasEspera + " días sin confirmar",
                        NivelAtencion.MEDIO
                );
                alertas.add(alerta);
            }
        }

        return alertas;
    }

    // Clientes activos sin ninguna interacción registrada
    private List<Alerta> alertarClientesSinSeguimiento() {
        List<Alerta> alertas = new ArrayList<>();

        for (Cliente c : clienteService.obtenerTodos()) {
            if (c.getEstadoBusqueda() == EstadoBusqueda.ACTIVO
                    && c.getPropiedadesVisitadas().isEmpty()
                    && c.getInmueblesConsultados().isEmpty()) {

                Alerta alerta = alertaService.generar(
                        "ALT-CLI-" + c.getId(),
                        "CLIENTE_SIN_SEGUIMIENTO",
                        "El cliente " + c.getNombre() + " no tiene interacciones registradas",
                        NivelAtencion.BAJO
                );
                alertas.add(alerta);
            }
        }

        return alertas;
    }

    // ================================================================
    // DETECCIÓN DE COMPORTAMIENTOS INUSUALES
    // ================================================================

    public void detectarComportamientosInusuales() {
        for (Cliente c : clienteService.obtenerTodos()) {
            detectarExcesoVisitasCliente(c.getId());
        }
        for (Asesor a : asesorService.obtenerTodos()) {
            detectarSobrecargaAsesor(a.getId());
        }
        detectarInmueblesConAltaDemanda();
    }

    private void detectarExcesoVisitasCliente(String idCliente) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        int totalVisitas = cliente.getPropiedadesVisitadas().getSize();

        if (totalVisitas > 10) {
            eventoService.registrar(
                    "EVT-CLI-" + idCliente + "-" + System.currentTimeMillis(),
                    "EXCESO_VISITAS_CLIENTE",
                    "Cliente " + cliente.getNombre() + " tiene " + totalVisitas + " visitas sin cierre",
                    NivelAtencion.MEDIO
            );
        }
    }

    private void detectarSobrecargaAsesor(String idAsesor) {
        int carga = asesorService.contarCarga(idAsesor);

        if (carga > 15) {
            Asesor asesor = asesorService.buscarPorId(idAsesor);
            eventoService.registrar(
                    "EVT-ASR-" + idAsesor + "-" + System.currentTimeMillis(),
                    "SOBRECARGA_ASESOR",
                    "Asesor " + asesor.getNombre() + " tiene carga de " + carga + " elementos",
                    NivelAtencion.ALTO
            );
        }
    }

    private void detectarInmueblesConAltaDemanda() {
        for (Inmueble i : inmuebleService.obtenerTodos()) {
            int totalVisitas = i.getListaVisitas().getSize();
            if (totalVisitas > 20 && i.isDisponibilidad()) {
                eventoService.registrar(
                        "EVT-INM-" + i.getCodigo() + "-" + System.currentTimeMillis(),
                        "ALTA_DEMANDA_SIN_CIERRE",
                        "Inmueble " + i.getCodigo() + " tiene " + totalVisitas + " visitas sin cerrarse",
                        NivelAtencion.ALTO
                );
            }
        }
    }

    // ================================================================
    // RECOMENDACIONES
    // ================================================================

    public List<Recomendacion> generarRecomendaciones(String idCliente) {
        return recomendacionService.generarParaCliente(idCliente);
    }

    public List<Inmueble> sugerirSimilares(String codigoInmueble) {
        return recomendacionService.sugerirSimilares(codigoInmueble);
    }

    // ================================================================
    // ANÁLISIS CON GRAFOS
    // ================================================================

    // Clientes que visitaron el mismo inmueble (conexiones compartidas)
    public List<String> obtenerClientesConectadosAInmueble(String codigoInmueble) {
        return grafoRelaciones.getNeighbors(codigoInmueble);
    }

    // Inmuebles visitados por un cliente
    public List<String> obtenerInmueblesConectadosACliente(String idCliente) {
        return grafoRelaciones.getNeighbors(idCliente);
    }

    // Recorrido BFS desde un nodo del grafo
    public void analizarRelacionesBFS(String nodoInicio) {
        grafoRelaciones.breadthFirstSearch(nodoInicio);
    }

    // ================================================================
    // REPORTES Y RANKINGS
    // ================================================================

    public List<Asesor> rankingAsesoresPorCierres() {
        return asesorService.obtenerRankingPorCierres();
    }

    public Map<String, Integer> rankingZonasPorActividad() {
        Map<String, Integer> conteo = new java.util.HashMap<>();

        for (Visita v : visitaService.obtenerTodas()) {
            String barrio = v.getInmueble().getBarrio();
            conteo.merge(barrio, 1, Integer::sum);
        }

        return conteo;
    }

    public List<Inmueble> obtenerInmueblesOrdenadosPorPrecio() {
        return inmuebleService.obtenerTodos(); // el AVL ya los entrega in-order por precio
    }

    public List<Inmueble> buscarPorFiltrosCombinados(TipoInmueble tipo,
                                                     FinalidadInmueble finalidad,
                                                     String ciudad,
                                                     double precioMax,
                                                     int habitacionesMin) {
        return inmuebleService.filtrarCombinado(tipo, finalidad, ciudad, precioMax, habitacionesMin);
    }
}