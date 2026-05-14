package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.dto.*;
import co.edu.uniquindio.poo.PropTech.model.entity.*;
import co.edu.uniquindio.poo.PropTech.model.enums.*;
import co.edu.uniquindio.poo.PropTech.structures.Graph;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import co.edu.uniquindio.poo.PropTech.structures.Queue;
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

    // Historial de precios por inmueble para detectar cambios frecuentes.
    // Usamos HashTable propia en lugar de java.util.HashMap
    private final HashTable<String, List<Double>> historialPrecios = new HashTable<>();

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
        List<Double> precios = new ArrayList<>();
        precios.add(dto.getPrecio());
        historialPrecios.put(inmueble.getCodigo(), precios);
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

        // Registrar precio actual en historial para deteccion de cambios frecuentes
        List<Double> precios = historialPrecios.get(inmueble.getCodigo());
        if (precios == null) {
            precios = new ArrayList<>();
            historialPrecios.put(inmueble.getCodigo(), precios);
        }
        precios.add(inmueble.getPrecio());

        Operacion operacion = operacionService.registrar(dto, inmueble, cliente, asesor);
        asesorService.registrarCierre(asesor.getId(), operacion);
        clienteService.registrarInmuebleNegociado(cliente.getId(), inmueble);

        return operacion;
    }

    // ================================================================
    // ALERTAS AUTOMATICAS — cubre los 6 tipos del PDF
    // ================================================================

    public List<Alerta> generarAlertas() {
        List<Alerta> alertasGeneradas = new ArrayList<>();
        alertasGeneradas.addAll(alertarInmueblesInactivos());
        alertasGeneradas.addAll(alertarVisitasPendientesSinConfirmar());
        alertasGeneradas.addAll(alertarClientesSinSeguimiento());
        alertasGeneradas.addAll(alertarContratosProximosAVencer());
        alertasGeneradas.addAll(alertarInmueblesConAltaDemanda());
        alertasGeneradas.addAll(alertarInmueblesReservadosSinCierre());
        return alertasGeneradas;
    }

    // 1. Inmuebles disponibles sin ninguna visita
    private List<Alerta> alertarInmueblesInactivos() {
        List<Alerta> alertas = new ArrayList<>();
        for (Inmueble i : inmuebleService.obtenerTodos()) {
            if (i.isDisponibilidad() && i.getListaVisitas().isEmpty()) {
                alertas.add(alertaService.generar(
                        "ALT-INM-INACT-" + i.getCodigo(),
                        "INMUEBLE_SIN_VISITAS",
                        "El inmueble " + i.getCodigo() + " (" + i.getDireccion() +
                                ") no ha recibido ninguna visita desde que fue publicado.",
                        NivelAtencion.BAJO));
            }
        }
        return alertas;
    }

    // 2. Visitas pendientes sin confirmar hace mas de 3 dias
    private List<Alerta> alertarVisitasPendientesSinConfirmar() {
        List<Alerta> alertas = new ArrayList<>();
        for (Visita v : visitaService.obtenerPorEstado(EstadoVisita.PENDIENTE)) {
            long dias = ChronoUnit.DAYS.between(v.getFecha(), LocalDate.now());
            if (dias > 3) {
                alertas.add(alertaService.generar(
                        "ALT-VIS-PEND-" + v.getIdVisita(),
                        "VISITA_PENDIENTE_SIN_CONFIRMAR",
                        "La visita " + v.getIdVisita() + " del cliente " +
                                v.getCliente().getNombre() + " lleva " + dias +
                                " dias pendiente sin confirmar.",
                        NivelAtencion.MEDIO));
            }
        }
        return alertas;
    }

    // 3. Clientes activos sin ninguna interaccion
    private List<Alerta> alertarClientesSinSeguimiento() {
        List<Alerta> alertas = new ArrayList<>();
        for (Cliente c : clienteService.obtenerTodos()) {
            if (c.getEstadoBusqueda() == EstadoBusqueda.ACTIVO
                    && c.getPropiedadesVisitadas().isEmpty()
                    && c.getInmueblesConsultados().isEmpty()) {
                alertas.add(alertaService.generar(
                        "ALT-CLI-SEG-" + c.getId(),
                        "CLIENTE_SIN_SEGUIMIENTO",
                        "El cliente " + c.getNombre() + " (" + c.getId() +
                                ") esta activo pero no tiene ninguna interaccion registrada.",
                        NivelAtencion.BAJO));
            }
        }
        return alertas;
    }

    // 4. Contratos proximos a vencer (dentro de 30 dias)
    private List<Alerta> alertarContratosProximosAVencer() {
        List<Alerta> alertas = new ArrayList<>();
        for (Operacion op : operacionService.obtenerTodas()) {
            if (op.getFechaVencimiento() == null) continue;
            if ("CERRADO".equals(op.getEstadoProceso()) ||
                    "CANCELADO".equals(op.getEstadoProceso())) continue;
            if (op.estaProximaAVencer(30)) {
                long diasRestantes = ChronoUnit.DAYS.between(
                        LocalDate.now(), op.getFechaVencimiento());
                NivelAtencion nivel = diasRestantes <= 7  ? NivelAtencion.CRITICO
                        : diasRestantes <= 15 ? NivelAtencion.ALTO
                          : NivelAtencion.MEDIO;
                alertas.add(alertaService.generar(
                        "ALT-OP-VENC-" + op.getIdOperacion(),
                        "CONTRATO_PROXIMO_A_VENCER",
                        "El contrato " + op.getIdOperacion() + " del inmueble " +
                                op.getInmueble().getCodigo() + " vence en " + diasRestantes +
                                " dia(s) (" + op.getFechaVencimiento() + ").",
                        nivel));
            }
        }
        return alertas;
    }

    // 5. Inmuebles disponibles con mas de 10 visitas sin cierre
    private List<Alerta> alertarInmueblesConAltaDemanda() {
        List<Alerta> alertas = new ArrayList<>();
        for (Inmueble i : inmuebleService.obtenerTodos()) {
            int totalVisitas = visitaService.obtenerPorInmueble(i.getCodigo()).size();
            if (i.isDisponibilidad() && totalVisitas > 10) {
                alertas.add(alertaService.generar(
                        "ALT-INM-DEMAND-" + i.getCodigo(),
                        "INMUEBLE_CON_ALTA_DEMANDA",
                        "El inmueble " + i.getCodigo() + " tiene " + totalVisitas +
                                " visitas y sigue disponible. Revisar precio o condiciones.",
                        NivelAtencion.MEDIO));
            }
        }
        return alertas;
    }

    // 6. Operaciones activas por mas de 60 dias sin cerrarse
    private List<Alerta> alertarInmueblesReservadosSinCierre() {
        List<Alerta> alertas = new ArrayList<>();
        for (Operacion op : operacionService.obtenerTodas()) {
            if ("CERRADO".equals(op.getEstadoProceso()) ||
                    "CANCELADO".equals(op.getEstadoProceso())) continue;
            long dias = ChronoUnit.DAYS.between(op.getFecha(), LocalDate.now());
            if (dias > 60) {
                alertas.add(alertaService.generar(
                        "ALT-OP-RESERV-" + op.getIdOperacion(),
                        "INMUEBLE_RESERVADO_SIN_CIERRE",
                        "El inmueble " + op.getInmueble().getCodigo() +
                                " lleva " + dias + " dias en proceso de " +
                                op.getTipoOperacion() + " sin cerrarse.",
                        NivelAtencion.ALTO));
            }
        }
        return alertas;
    }

    // ================================================================
    // DETECCION DE COMPORTAMIENTOS INUSUALES
    // ================================================================

    public void detectarComportamientosInusuales() {
        for (Cliente c : clienteService.obtenerTodos()) detectarExcesoVisitasCliente(c.getId());
        for (Asesor a : asesorService.obtenerTodos()) detectarSobrecargaAsesor(a.getId());
        detectarInmueblesConAltaDemandaEvento();
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
                    "Cliente " + cliente.getNombre() + " tiene " + total +
                            " visitas registradas sin haber cerrado ninguna operacion.",
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
                    "Asesor " + asesor.getNombre() + " tiene carga total de " + carga + " elementos.",
                    NivelAtencion.ALTO);
        }
    }

    private void detectarInmueblesConAltaDemandaEvento() {
        for (Inmueble i : inmuebleService.obtenerTodos()) {
            int total = visitaService.obtenerPorInmueble(i.getCodigo()).size();
            if (total > 20 && i.isDisponibilidad()) {
                eventoService.registrar(
                        "EVT-INM-" + i.getCodigo() + "-" + System.currentTimeMillis(),
                        "ALTA_DEMANDA_SIN_CIERRE",
                        "El inmueble " + i.getCodigo() + " tiene " + total +
                                " visitas y continua disponible.",
                        NivelAtencion.ALTO);
            }
        }
    }

    private void detectarCambiosFrecuentesDePrecio() {
        for (Inmueble i : inmuebleService.obtenerTodos()) {
            List<Double> precios = historialPrecios.get(i.getCodigo());
            if (precios != null && precios.size() > 3) {
                eventoService.registrar(
                        "EVT-PRECIO-" + i.getCodigo() + "-" + System.currentTimeMillis(),
                        "PRECIO_CAMBIA_FRECUENTEMENTE",
                        "El inmueble " + i.getCodigo() + " ha tenido " +
                                precios.size() + " cambios de precio.",
                        NivelAtencion.MEDIO);
            }
        }
    }

    private void detectarConcentracionZona() {
        Map<String, Integer> conteo = rankingZonasPorActividad();
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            if (entry.getValue() > 15) {
                eventoService.registrar(
                        "EVT-ZONA-" + entry.getKey().replace(" ", "_") + "-" + System.currentTimeMillis(),
                        "CONCENTRACION_ZONA",
                        "La zona '" + entry.getKey() + "' tiene " + entry.getValue() +
                                " visitas recientes, concentracion inusual.",
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
    // ANALISIS CON GRAFOS — BFS usa Queue propia, sin java.util.LinkedList
    // ================================================================

    public List<String> obtenerClientesConectadosAInmueble(String codigoInmueble) {
        return grafoRelaciones.getNeighbors(codigoInmueble);
    }

    public List<String> obtenerInmueblesConectadosACliente(String idCliente) {
        return grafoRelaciones.getNeighbors(idCliente);
    }

    /**
     * BFS desde un nodo usando Queue y HashTable propias.
     * No usa java.util.LinkedList ni java.util.HashSet.
     */
    public List<String> analizarRelacionesBFS(String nodoInicio) {
        List<String> resultado = new ArrayList<>();
        if (!grafoRelaciones.containsVertex(nodoInicio)) return resultado;

        // Queue propia en lugar de java.util.LinkedList
        Queue<String> cola = new Queue<>();
        // HashTable propia en lugar de java.util.HashSet
        HashTable<String, Boolean> visitados = new HashTable<>();

        cola.enqueue(nodoInicio);
        visitados.put(nodoInicio, true);

        while (!cola.isEmpty()) {
            String actual = cola.dequeue();
            resultado.add(actual);
            for (String vecino : grafoRelaciones.getNeighbors(actual)) {
                if (!visitados.containsKey(vecino)) {
                    visitados.put(vecino, true);
                    cola.enqueue(vecino);
                }
            }
        }
        return resultado;
    }

    /**
     * Clientes con perfil similar: dado un cliente, busca otros clientes
     * que visitaron los mismos inmuebles (vecinos de vecinos en el grafo).
     */
    public List<String> obtenerClientesConPerfilSimilar(String idCliente) {
        List<String> inmueblesDelCliente = grafoRelaciones.getNeighbors(idCliente);
        // HashTable propia para evitar duplicados
        HashTable<String, Boolean> clientesSimilares = new HashTable<>();
        List<String> resultado = new ArrayList<>();

        for (String codigoInmueble : inmueblesDelCliente) {
            for (String vecino : grafoRelaciones.getNeighbors(codigoInmueble)) {
                if (!vecino.equals(idCliente) && vecino.startsWith("CLI-")
                        && !clientesSimilares.containsKey(vecino)) {
                    clientesSimilares.put(vecino, true);
                    resultado.add(vecino);
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
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Visita v : visitaService.obtenerTodas()) {
            String zona = v.getInmueble().getBarrio();
            conteo.merge(zona, 1, Integer::sum);
        }
        return conteo;
    }

    /**
     * Clientes con alta probabilidad de cierre:
     * - Estado ACTIVO
     * - Tienen al menos un favorito guardado
     * - Visitaron 2 o mas inmuebles
     * - Su presupuesto alcanza algun inmueble disponible del tipo que buscan
     */
    public List<Cliente> obtenerClientesConAltaProbabilidadDeCierre() {
        List<Cliente> resultado = new ArrayList<>();
        List<Inmueble> disponibles = inmuebleService.filtrarDisponibles();

        for (Cliente c : clienteService.obtenerTodos()) {
            if (c.getEstadoBusqueda() != EstadoBusqueda.ACTIVO) continue;
            boolean tieneFavoritos  = !c.getInmueblesGuardados().isEmpty();
            boolean visitoSuficiente = c.getPropiedadesVisitadas().getSize() >= 2;
            boolean hayInmuebleAcorde = false;
            for (Inmueble i : disponibles) {
                if (i.getPrecio() <= c.getPresupuesto()
                        && i.getTipoInmueble() == c.getTipoInmuebleDeseado()) {
                    hayInmuebleAcorde = true;
                    break;
                }
            }
            if (tieneFavoritos && visitoSuficiente && hayInmuebleAcorde) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /**
     * Simulacion de crecimiento de demanda por sector:
     * compara visitas de los ultimos 30 dias vs los 30 anteriores por barrio.
     */
    public Map<String, Map<String, Object>> simularCrecimientoDemandaPorSector() {
        Map<String, Integer> recientes  = new LinkedHashMap<>();
        Map<String, Integer> anteriores = new LinkedHashMap<>();

        LocalDate hoy    = LocalDate.now();
        LocalDate hace30 = hoy.minusDays(30);
        LocalDate hace60 = hoy.minusDays(60);

        for (Visita v : visitaService.obtenerTodas()) {
            String zona = v.getInmueble().getBarrio();
            LocalDate fecha = v.getFecha();
            if (!fecha.isBefore(hace30) && !fecha.isAfter(hoy)) {
                recientes.merge(zona, 1, Integer::sum);
            } else if (!fecha.isBefore(hace60) && fecha.isBefore(hace30)) {
                anteriores.merge(zona, 1, Integer::sum);
            }
        }

        Map<String, Map<String, Object>> simulacion = new LinkedHashMap<>();
        Set<String> zonas = new LinkedHashSet<>();
        zonas.addAll(recientes.keySet());
        zonas.addAll(anteriores.keySet());

        for (String zona : zonas) {
            int visR = recientes.getOrDefault(zona, 0);
            int visA = anteriores.getOrDefault(zona, 0);
            double crecimiento = visA == 0
                    ? (visR > 0 ? 100.0 : 0.0)
                    : ((visR - visA) / (double) visA) * 100.0;

            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("visitasUltimos30Dias",   visR);
            datos.put("visitasPeriodoAnterior",  visA);
            datos.put("crecimientoPorcentaje",   Math.round(crecimiento * 10.0) / 10.0);
            datos.put("tendencia", crecimiento > 10 ? "CRECIENDO"
                    : crecimiento < -10 ? "DECAYENDO"
                      : "ESTABLE");
            simulacion.put(zona, datos);
        }
        return simulacion;
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