package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.dto.*;
import co.edu.uniquindio.poo.PropTech.model.entity.*;
import co.edu.uniquindio.poo.PropTech.model.enums.*;
import co.edu.uniquindio.poo.PropTech.structures.Graph;
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

        historialPrecios.computeIfAbsent(inmueble.getCodigo(), k -> new ArrayList<>())
                .add(inmueble.getPrecio());

        Operacion operacion = operacionService.registrar(dto, inmueble, cliente, asesor);
        asesorService.registrarCierre(asesor.getId(), operacion);
        clienteService.registrarInmuebleNegociado(cliente.getId(), inmueble);

        return operacion;
    }

    // ================================================================
    // ALERTAS AUTOMÁTICAS — cubre los 6 tipos del PDF
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

    // 1. Inmuebles disponibles sin ninguna visita registrada
    private List<Alerta> alertarInmueblesInactivos() {
        List<Alerta> alertas = new ArrayList<>();
        for (Inmueble i : inmuebleService.obtenerTodos()) {
            if (i.isDisponibilidad() && i.getListaVisitas().isEmpty()) {
                String id = "ALT-INM-INACT-" + i.getCodigo();
                alertas.add(alertaService.generar(
                        id,
                        "INMUEBLE_SIN_VISITAS",
                        "El inmueble " + i.getCodigo() + " (" + i.getDireccion() +
                                ") no ha recibido ninguna visita desde que fue publicado.",
                        NivelAtencion.BAJO));
            }
        }
        return alertas;
    }

    // 2. Visitas pendientes sin confirmar hace más de 3 días
    private List<Alerta> alertarVisitasPendientesSinConfirmar() {
        List<Alerta> alertas = new ArrayList<>();
        for (Visita v : visitaService.obtenerPorEstado(EstadoVisita.PENDIENTE)) {
            long dias = ChronoUnit.DAYS.between(v.getFecha(), LocalDate.now());
            if (dias > 3) {
                String id = "ALT-VIS-PEND-" + v.getIdVisita();
                alertas.add(alertaService.generar(
                        id,
                        "VISITA_PENDIENTE_SIN_CONFIRMAR",
                        "La visita " + v.getIdVisita() + " del cliente " +
                                v.getCliente().getNombre() + " lleva " + dias +
                                " días pendiente sin confirmar.",
                        NivelAtencion.MEDIO));
            }
        }
        return alertas;
    }

    // 3. Clientes activos sin ninguna interacción registrada
    private List<Alerta> alertarClientesSinSeguimiento() {
        List<Alerta> alertas = new ArrayList<>();
        for (Cliente c : clienteService.obtenerTodos()) {
            if (c.getEstadoBusqueda() == EstadoBusqueda.ACTIVO
                    && c.getPropiedadesVisitadas().isEmpty()
                    && c.getInmueblesConsultados().isEmpty()) {
                String id = "ALT-CLI-SEG-" + c.getId();
                alertas.add(alertaService.generar(
                        id,
                        "CLIENTE_SIN_SEGUIMIENTO",
                        "El cliente " + c.getNombre() + " (" + c.getId() +
                                ") está activo pero no tiene ninguna interacción registrada.",
                        NivelAtencion.BAJO));
            }
        }
        return alertas;
    }

    // 4. Contratos próximos a vencer (dentro de los próximos 30 días)
    private List<Alerta> alertarContratosProximosAVencer() {
        List<Alerta> alertas = new ArrayList<>();
        int diasUmbral = 30;
        for (Operacion op : operacionService.obtenerTodas()) {
            if (op.getFechaVencimiento() == null) continue;
            if (!"CERRADO".equals(op.getEstadoProceso()) &&
                    !"CANCELADO".equals(op.getEstadoProceso()) &&
                    op.estaProximaAVencer(diasUmbral)) {

                long diasRestantes = ChronoUnit.DAYS.between(
                        LocalDate.now(), op.getFechaVencimiento());

                NivelAtencion nivel = diasRestantes <= 7
                        ? NivelAtencion.CRITICO
                        : diasRestantes <= 15
                          ? NivelAtencion.ALTO
                          : NivelAtencion.MEDIO;

                String id = "ALT-OP-VENC-" + op.getIdOperacion();
                alertas.add(alertaService.generar(
                        id,
                        "CONTRATO_PROXIMO_A_VENCER",
                        "El contrato " + op.getIdOperacion() + " del inmueble " +
                                op.getInmueble().getCodigo() + " vence en " + diasRestantes +
                                " día(s) (" + op.getFechaVencimiento() + ").",
                        nivel));
            }
        }
        return alertas;
    }

    // 5. Inmuebles disponibles con más de 10 visitas pero sin cierre (alta demanda sin resultado)
    private List<Alerta> alertarInmueblesConAltaDemanda() {
        List<Alerta> alertas = new ArrayList<>();
        for (Inmueble i : inmuebleService.obtenerTodos()) {
            int totalVisitas = visitaService.obtenerPorInmueble(i.getCodigo()).size();
            if (i.isDisponibilidad() && totalVisitas > 10) {
                String id = "ALT-INM-DEMAND-" + i.getCodigo();
                alertas.add(alertaService.generar(
                        id,
                        "INMUEBLE_CON_ALTA_DEMANDA",
                        "El inmueble " + i.getCodigo() + " (" + i.getDireccion() +
                                ") tiene " + totalVisitas +
                                " visitas registradas y sigue disponible. Revisar precio o condiciones.",
                        NivelAtencion.MEDIO));
            }
        }
        return alertas;
    }

    // 6. Inmuebles en operación activa (no cerrados ni cancelados) por más de 60 días sin cierre
    private List<Alerta> alertarInmueblesReservadosSinCierre() {
        List<Alerta> alertas = new ArrayList<>();
        int diasUmbral = 60;
        for (Operacion op : operacionService.obtenerTodas()) {
            if ("CERRADO".equals(op.getEstadoProceso()) ||
                    "CANCELADO".equals(op.getEstadoProceso())) continue;

            long diasTranscurridos = ChronoUnit.DAYS.between(op.getFecha(), LocalDate.now());
            if (diasTranscurridos > diasUmbral) {
                String id = "ALT-OP-RESERV-" + op.getIdOperacion();
                alertas.add(alertaService.generar(
                        id,
                        "INMUEBLE_RESERVADO_SIN_CIERRE",
                        "El inmueble " + op.getInmueble().getCodigo() +
                                " lleva " + diasTranscurridos +
                                " días en proceso de " + op.getTipoOperacion() +
                                " sin cerrarse. Operación: " + op.getIdOperacion() + ".",
                        NivelAtencion.ALTO));
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
                    "Cliente " + cliente.getNombre() + " tiene " + total +
                            " visitas registradas sin haber cerrado ninguna operación.",
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
                    "Asesor " + asesor.getNombre() + " tiene una carga total de " +
                            carga + " elementos (visitas + inmuebles asignados).",
                    NivelAtencion.ALTO);
        }
    }

    private void detectarInmueblesConAltaDemanda() {
        for (Inmueble i : inmuebleService.obtenerTodos()) {
            int total = visitaService.obtenerPorInmueble(i.getCodigo()).size();
            if (total > 20 && i.isDisponibilidad()) {
                eventoService.registrar(
                        "EVT-INM-" + i.getCodigo() + "-" + System.currentTimeMillis(),
                        "ALTA_DEMANDA_SIN_CIERRE",
                        "El inmueble " + i.getCodigo() + " tiene " + total +
                                " visitas registradas y continúa disponible sin cerrarse.",
                        NivelAtencion.ALTO);
            }
        }
    }

    private void detectarCambiosFrecuentesDePrecio() {
        for (Map.Entry<String, List<Double>> entry : historialPrecios.entrySet()) {
            if (entry.getValue().size() > 3) {
                eventoService.registrar(
                        "EVT-PRECIO-" + entry.getKey() + "-" + System.currentTimeMillis(),
                        "PRECIO_CAMBIA_FRECUENTEMENTE",
                        "El inmueble " + entry.getKey() + " ha tenido " +
                                entry.getValue().size() + " cambios de precio registrados.",
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
                                " visitas registradas, lo que indica una concentración inusual de interés.",
                        NivelAtencion.BAJO);
            }
        }
    }

    // ================================================================
    // RECOMENDACIONES — cubre los 6 criterios del PDF
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

    // Análisis de segundo nivel: dado un cliente, encuentra qué otros
    // clientes visitaron inmuebles similares (vecinos de vecinos en el grafo)
    public List<String> obtenerClientesConPerfilSimilar(String idCliente) {
        List<String> inmueblesDelCliente = grafoRelaciones.getNeighbors(idCliente);
        Set<String> clientesSimilares = new LinkedHashSet<>();

        for (String codigoInmueble : inmueblesDelCliente) {
            for (String vecino : grafoRelaciones.getNeighbors(codigoInmueble)) {
                // Excluye el cliente de origen y solo incluye IDs de clientes (empiezan con CLI-)
                if (!vecino.equals(idCliente) && vecino.startsWith("CLI-")) {
                    clientesSimilares.add(vecino);
                }
            }
        }
        return new ArrayList<>(clientesSimilares);
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

    // Detección de clientes con alta probabilidad de cierre:
    // tienen favoritos + visitaron propiedades + presupuesto cubre inmuebles disponibles
    public List<Cliente> obtenerClientesConAltaProbabilidadDeCierre() {
        List<Cliente> resultado = new ArrayList<>();
        List<Inmueble> disponibles = inmuebleService.filtrarDisponibles();

        for (Cliente c : clienteService.obtenerTodos()) {
            if (c.getEstadoBusqueda() != EstadoBusqueda.ACTIVO) continue;

            boolean tieneFavoritos  = !c.getInmueblesGuardados().isEmpty();
            boolean visitoInmuebles = c.getPropiedadesVisitadas().getSize() >= 2;
            boolean presupuestoVida = disponibles.stream()
                    .anyMatch(i -> i.getPrecio() <= c.getPresupuesto()
                            && i.getTipoInmueble() == c.getTipoInmuebleDeseado());

            if (tieneFavoritos && visitoInmuebles && presupuestoVida) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    // Simulación de crecimiento de demanda por sector:
    // compara visitas de los últimos 30 días vs los 30 anteriores por barrio
    public Map<String, Map<String, Object>> simularCrecimientoDemandaPorSector() {
        Map<String, Integer> recientes   = new HashMap<>(); // últimos 30 días
        Map<String, Integer> anteriores  = new HashMap<>(); // días 31 a 60

        LocalDate hoy      = LocalDate.now();
        LocalDate hace30   = hoy.minusDays(30);
        LocalDate hace60   = hoy.minusDays(60);

        for (Visita v : visitaService.obtenerTodas()) {
            String zona = v.getInmueble().getBarrio();
            LocalDate fechaVisita = v.getFecha();

            if (!fechaVisita.isBefore(hace30) && !fechaVisita.isAfter(hoy)) {
                recientes.merge(zona, 1, Integer::sum);
            } else if (!fechaVisita.isBefore(hace60) && fechaVisita.isBefore(hace30)) {
                anteriores.merge(zona, 1, Integer::sum);
            }
        }

        Map<String, Map<String, Object>> simulacion = new LinkedHashMap<>();
        Set<String> todasLasZonas = new HashSet<>();
        todasLasZonas.addAll(recientes.keySet());
        todasLasZonas.addAll(anteriores.keySet());

        for (String zona : todasLasZonas) {
            int visRecientes  = recientes.getOrDefault(zona, 0);
            int visAnteriores = anteriores.getOrDefault(zona, 0);
            double crecimiento = visAnteriores == 0
                    ? (visRecientes > 0 ? 100.0 : 0.0)
                    : ((visRecientes - visAnteriores) / (double) visAnteriores) * 100.0;

            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("visitasUltimos30Dias",    visRecientes);
            datos.put("visitasPeriodoAnterior",  visAnteriores);
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