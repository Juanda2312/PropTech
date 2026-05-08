package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.dto.*;
import co.edu.uniquindio.poo.PropTech.model.entity.*;
import co.edu.uniquindio.poo.PropTech.model.enums.*;
import co.edu.uniquindio.poo.PropTech.structures.Graph;
import co.edu.uniquindio.poo.PropTech.structures.PriorityQueue;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class PlataformaBeta {

    private final InmuebleService      inmuebleService;
    private final ClienteService       clienteService;
    private final AsesorService        asesorService;
    private final VisitaService        visitaService;
    private final OperacionService     operacionService;
    private final AlertaService        alertaService;
    private final EventoInusualService eventoService;
    private final RecomendacionService recomendacionService;

    private final Graph<String> grafoRelaciones = new Graph<>(false);

    // Historial de precios por inmueble para detectar cambios frecuentes
    private final Map<String, List<Double>> historialPrecios = new HashMap<>();

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
        historialPrecios.put(inmueble.getCodigo(), new ArrayList<>(List.of(dto.getPrecio())));
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

        grafoRelaciones.addEdge(cliente.getId(), inmueble.getCodigo());

        detectarExcesoVisitasCliente(cliente.getId());
        detectarSobrecargaAsesor(asesor.getId());

        return visita;
    }

    // ================================================================
    // OPERACIONES
    // ================================================================

    public Operacion registrarOperacion(OperacionDTO dto) {
        Inmueble inmueble = inmuebleService.buscarPorCodigo(dto.getCodigoInmueble());
        Cliente  cliente  = clienteService.buscarPorId(dto.getIdCliente());
        Asesor   asesor   = asesorService.buscarPorId(dto.getIdAsesor());

        // Registrar el precio actual en el historial para detección de cambios
        historialPrecios.computeIfAbsent(inmueble.getCodigo(), k -> new ArrayList<>())
                .add(inmueble.getPrecio());

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

    private List<Alerta> alertarInmueblesInactivos() {
        List<Alerta> alertas = new ArrayList<>();
        for (Inmueble i : inmuebleService.obtenerTodos()) {
            if (i.isDisponibilidad() && i.getListaVisitas().isEmpty()) {
                alertas.add(alertaService.generar(
                        "ALT-INM-" + i.getCodigo(), "INMUEBLE_INACTIVO",
                        "El inmueble " + i.getCodigo() + " no ha recibido visitas",
                        NivelAtencion.BAJO));
            }
        }
        return alertas;
    }

    private List<Alerta> alertarVisitasPendientes() {
        List<Alerta> alertas = new ArrayList<>();
        for (Visita v : visitaService.obtenerPorEstado(EstadoVisita.PENDIENTE)) {
            long dias = ChronoUnit.DAYS.between(v.getFecha(), LocalDate.now());
            if (dias > 3) {
                alertas.add(alertaService.generar(
                        "ALT-VIS-" + v.getIdVisita(), "VISITA_PENDIENTE",
                        "La visita " + v.getIdVisita() + " lleva " + dias + " días sin confirmar",
                        NivelAtencion.MEDIO));
            }
        }
        return alertas;
    }

    private List<Alerta> alertarClientesSinSeguimiento() {
        List<Alerta> alertas = new ArrayList<>();
        for (Cliente c : clienteService.obtenerTodos()) {
            if (c.getEstadoBusqueda() == EstadoBusqueda.ACTIVO
                    && c.getPropiedadesVisitadas().isEmpty()
                    && c.getInmueblesConsultados().isEmpty()) {
                alertas.add(alertaService.generar(
                        "ALT-CLI-" + c.getId(), "CLIENTE_SIN_SEGUIMIENTO",
                        "El cliente " + c.getNombre() + " no tiene interacciones registradas",
                        NivelAtencion.BAJO));
            }
        }
        return alertas;
    }

    // ================================================================
    // DETECCIÓN DE COMPORTAMIENTOS INUSUALES
    // ================================================================

    public void detectarComportamientosInusuales() {
        for (Cliente c : clienteService.obtenerTodos()) detectarExcesoVisitasCliente(c.getId());
        for (Asesor a : asesorService.obtenerTodos()) detectarSobrecargaAsesor(a.getId());
        detectarInmueblesConAltaDemanda();
        detectarCambiosFrecuentesDePrecio();
        detectarConcentracionZona();
    }

    private void detectarExcesoVisitasCliente(String idCliente) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        int total = cliente.getPropiedadesVisitadas().getSize();
        if (total > 10) {
            eventoService.registrar(
                    "EVT-CLI-" + idCliente + "-" + System.currentTimeMillis(),
                    "EXCESO_VISITAS_CLIENTE",
                    "Cliente " + cliente.getNombre() + " tiene " + total + " visitas sin cierre",
                    NivelAtencion.MEDIO);
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
                    NivelAtencion.ALTO);
        }
    }

    private void detectarInmueblesConAltaDemanda() {
        for (Inmueble i : inmuebleService.obtenerTodos()) {
            int total = i.getListaVisitas().getSize();
            if (total > 20 && i.isDisponibilidad()) {
                eventoService.registrar(
                        "EVT-INM-" + i.getCodigo() + "-" + System.currentTimeMillis(),
                        "ALTA_DEMANDA_SIN_CIERRE",
                        "Inmueble " + i.getCodigo() + " tiene " + total + " visitas sin cerrarse",
                        NivelAtencion.ALTO);
            }
        }
    }

    // NUEVO: detecta inmuebles cuyo precio cambió más de 3 veces
    private void detectarCambiosFrecuentesDePrecio() {
        for (Map.Entry<String, List<Double>> entry : historialPrecios.entrySet()) {
            if (entry.getValue().size() > 3) {
                eventoService.registrar(
                        "EVT-PRECIO-" + entry.getKey() + "-" + System.currentTimeMillis(),
                        "PRECIO_CAMBIA_FRECUENTEMENTE",
                        "El inmueble " + entry.getKey() + " ha tenido "
                                + entry.getValue().size() + " cambios de precio",
                        NivelAtencion.MEDIO);
            }
        }
    }

    // NUEVO: detecta concentración de visitas en una misma zona
    private void detectarConcentracionZona() {
        Map<String, Integer> conteo = rankingZonasPorActividad();
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            if (entry.getValue() > 15) {
                eventoService.registrar(
                        "EVT-ZONA-" + entry.getKey() + "-" + System.currentTimeMillis(),
                        "CONCENTRACION_ZONA",
                        "La zona " + entry.getKey() + " tiene " + entry.getValue()
                                + " visitas recientes, concentración inusual",
                        NivelAtencion.BAJO);
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

    public List<String> obtenerClientesConectadosAInmueble(String codigoInmueble) {
        return grafoRelaciones.getNeighbors(codigoInmueble);
    }

    public List<String> obtenerInmueblesConectadosACliente(String idCliente) {
        return grafoRelaciones.getNeighbors(idCliente);
    }

    // CORREGIDO: ahora retorna la lista en lugar de solo imprimir
    public List<String> analizarRelacionesBFS(String nodoInicio) {
        if (!grafoRelaciones.containsVertex(nodoInicio)) return new ArrayList<>();

        Queue<String> cola = new java.util.LinkedList<>();
        Set<String> visitados = new HashSet<>();
        List<String> resultado = new ArrayList<>();

        cola.add(nodoInicio);
        visitados.add(nodoInicio);

        while (!cola.isEmpty()) {
            String actual = cola.poll();
            resultado.add(actual);
            for (String vecino : grafoRelaciones.getNeighbors(actual)) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }
        return resultado;
    }

    // ================================================================
    // REPORTES Y RANKINGS
    // ================================================================

    public List<Asesor> rankingAsesoresPorCierres() {
        return asesorService.obtenerRankingPorCierres();
    }

    public Map<String, Integer> rankingZonasPorActividad() {
        Map<String, Integer> conteo = new HashMap<>();
        for (Visita v : visitaService.obtenerTodas()) {
            String zona = v.getInmueble().getBarrio();
            conteo.merge(zona, 1, Integer::sum);
        }
        return conteo;
    }

    public List<Inmueble> obtenerInmueblesOrdenadosPorPrecio() {
        return inmuebleService.obtenerTodos();
    }

    public List<Inmueble> buscarPorFiltrosCombinados(TipoInmueble tipo,
                                                     FinalidadInmueble finalidad,
                                                     String ciudad, double precioMax,
                                                     int habitacionesMin) {
        return inmuebleService.filtrarCombinado(tipo, finalidad, ciudad, precioMax, habitacionesMin);
    }
    public Visita procesarVisitaVIP() {
        return visitaService.procesarSiguienteVIP();
    }
}