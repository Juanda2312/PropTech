package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.entity.*;
import co.edu.uniquindio.poo.PropTech.model.enums.*;
import co.edu.uniquindio.poo.PropTech.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PersistenciaService {

    private static final String DIR               = "data/";
    private static final String ASESORES_FILE     = DIR + "asesores.json";
    private static final String CLIENTES_FILE     = DIR + "clientes.json";
    private static final String INMUEBLES_FILE    = DIR + "inmuebles.json";
    private static final String VISITAS_FILE      = DIR + "visitas.json";
    private static final String OPERACIONES_FILE  = DIR + "operaciones.json";
    private static final String ALERTAS_FILE      = DIR + "alertas.json";
    private static final String EVENTOS_FILE      = DIR + "eventos.json";
    // Nuevo: interacciones guardadas como lista plana con idCliente
    private static final String INTERACCIONES_FILE = DIR + "interacciones.json";

    private final ObjectMapper mapper;
    private final AsesorRepository        asesorRepository;
    private final ClienteRepository       clienteRepository;
    private final InmuebleRepository      inmuebleRepository;
    private final VisitaRepository        visitaRepository;
    private final OperacionRepository     operacionRepository;
    private final AlertaRepository        alertaRepository;
    private final EventoInusualRepository eventoRepository;

    public PersistenciaService(AsesorRepository asesorRepository,
                               ClienteRepository clienteRepository,
                               InmuebleRepository inmuebleRepository,
                               VisitaRepository visitaRepository,
                               OperacionRepository operacionRepository,
                               AlertaRepository alertaRepository,
                               EventoInusualRepository eventoRepository) {
        this.asesorRepository    = asesorRepository;
        this.clienteRepository   = clienteRepository;
        this.inmuebleRepository  = inmuebleRepository;
        this.visitaRepository    = visitaRepository;
        this.operacionRepository = operacionRepository;
        this.alertaRepository    = alertaRepository;
        this.eventoRepository    = eventoRepository;

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Ignorar propiedades desconocidas al deserializar
        this.mapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        new File(DIR).mkdirs();
    }

    // ================================================================
    // DTO interno para persistir interacciones con su idCliente
    // ================================================================

    /**
     * Clase auxiliar para serializar/deserializar interacciones
     * junto con el ID del cliente al que pertenecen.
     * Se usa solo en persistencia, no en la API REST.
     */
    public static class InteraccionPersistida {
        public String idCliente;
        public String idInteraccion;
        public String fecha;           // ISO: "2025-05-10"
        public String tipoInteraccion; // Nombre del enum
        public String detalle;
        public String codigoInmueble;  // Solo el código, se resuelve al cargar

        public InteraccionPersistida() {}

        public InteraccionPersistida(String idCliente, Interaccion interaccion) {
            this.idCliente       = idCliente;
            this.idInteraccion   = interaccion.getId();
            this.fecha           = interaccion.getFecha() != null
                    ? interaccion.getFecha().toString() : null;
            this.tipoInteraccion = interaccion.getTipoInteraccion() != null
                    ? interaccion.getTipoInteraccion().name() : null;
            this.detalle         = interaccion.getDetalle();
            this.codigoInmueble  = interaccion.getInmueble() != null
                    ? interaccion.getInmueble().getCodigo() : null;
        }
    }

    // ================================================================
    // GUARDAR AL APAGAR
    // ================================================================

    @PreDestroy
    public void guardarTodo() {
        System.out.println("💾 [Persistencia] Guardando datos...");

        // Sincronizar favoritos antes de guardar
        for (Cliente c : clienteRepository.findAll()) {
            List<String> codigos = new ArrayList<>();
            c.getInmueblesGuardados().forEach(i -> codigos.add(i.getCodigo()));
            c.setCodigosFavoritos(codigos);
        }

        guardar(ASESORES_FILE,    asesorRepository.findAll());
        guardar(CLIENTES_FILE,    clienteRepository.findAll());
        guardar(INMUEBLES_FILE,   inmuebleRepository.findAll());
        guardar(VISITAS_FILE,     visitaRepository.findAll());
        guardar(OPERACIONES_FILE, operacionRepository.findAll());
        guardar(ALERTAS_FILE,     alertaRepository.findAll());
        guardar(EVENTOS_FILE,     eventoRepository.findAll());

        // Guardar interacciones de todos los clientes
        guardarInteracciones();

        System.out.println("✅ [Persistencia] Datos guardados correctamente en /data/");
    }

    /**
     * Recorre todos los clientes y convierte cada Interaccion
     * a InteraccionPersistida (con idCliente + codigoInmueble en lugar del objeto completo).
     * Esto evita referencias circulares y el problema del @JsonIgnore.
     */
    private void guardarInteracciones() {
        List<InteraccionPersistida> todas = new ArrayList<>();

        for (Cliente cliente : clienteRepository.findAll()) {
            for (Interaccion interaccion : cliente.getHistorialInteracciones()) {
                todas.add(new InteraccionPersistida(cliente.getId(), interaccion));
            }
        }

        guardar(INTERACCIONES_FILE, todas);
        System.out.println("   💬 Interacciones guardadas: " + todas.size());
    }

    private void guardar(String ruta, List<?> lista) {
        try {
            mapper.writeValue(new File(ruta), lista);
        } catch (IOException e) {
            System.err.println("⚠️  [Persistencia] Error guardando " + ruta + ": " + e.getMessage());
        }
    }

    // ================================================================
    // CARGAR AL INICIAR
    // ================================================================

    public boolean hayDatosGuardados() {
        return new File(ASESORES_FILE).exists();
    }

    public List<Asesor>        cargarAsesores()    { return cargar(ASESORES_FILE,    Asesor.class); }
    public List<Cliente>       cargarClientes()    { return cargar(CLIENTES_FILE,    Cliente.class); }
    public List<Inmueble>      cargarInmuebles()   { return cargar(INMUEBLES_FILE,   Inmueble.class); }
    public List<Visita>        cargarVisitas()     { return cargar(VISITAS_FILE,     Visita.class); }
    public List<Operacion>     cargarOperaciones() { return cargar(OPERACIONES_FILE, Operacion.class); }
    public List<Alerta>        cargarAlertas()     { return cargar(ALERTAS_FILE,     Alerta.class); }
    public List<EventoInusual> cargarEventos()     { return cargar(EVENTOS_FILE,     EventoInusual.class); }

    /**
     * Carga las interacciones persistidas y las reconstruye
     * en el historialInteracciones de cada cliente.
     * Se llama desde DataLoader DESPUÉS de cargar clientes e inmuebles.
     */
    public void restaurarInteracciones() {
        File archivo = new File(INTERACCIONES_FILE);
        if (!archivo.exists()) {
            System.out.println("   ℹ️  No hay archivo de interacciones previo.");
            return;
        }

        List<InteraccionPersistida> persistidas =
                cargar(INTERACCIONES_FILE, InteraccionPersistida.class);

        int restauradas = 0;
        for (InteraccionPersistida p : persistidas) {
            try {
                // Buscar el cliente
                Cliente cliente = clienteRepository.findById(p.idCliente).orElse(null);
                if (cliente == null) continue;

                // Buscar el inmueble (puede ser null si fue eliminado)
                Inmueble inmueble = null;
                if (p.codigoInmueble != null) {
                    inmueble = inmuebleRepository.findById(p.codigoInmueble).orElse(null);
                }

                // Reconstruir la interaccion
                Interaccion interaccion = new Interaccion();
                interaccion.setId(p.idInteraccion);
                interaccion.setFecha(p.fecha != null
                        ? java.time.LocalDate.parse(p.fecha) : java.time.LocalDate.now());
                interaccion.setTipoInteraccion(p.tipoInteraccion != null
                        ? TipoInteraccion.valueOf(p.tipoInteraccion) : null);
                interaccion.setDetalle(p.detalle);
                interaccion.setInmueble(inmueble);

                // Agregar al historial del cliente (al final para mantener orden)
                cliente.getHistorialInteracciones().addLast(interaccion);

                // También restaurar en las listas específicas según el tipo
                if (inmueble != null && p.tipoInteraccion != null) {
                    switch (TipoInteraccion.valueOf(p.tipoInteraccion)) {
                        case INMUEBLE_CONSULTADO ->
                                cliente.getInmueblesConsultados().addLast(inmueble);
                        case FAVORITO_GUARDADO ->
                                cliente.getInmueblesGuardados().addLast(inmueble);
                        case INMUEBLE_DESCARTADO ->
                                cliente.getInmueblesDescartados().addLast(inmueble);
                        case VISITA_AGENDADA, COMPRA_REALIZADA, ARRIENDO_REALIZADO ->
                                cliente.getPropiedadesVisitadas().addLast(inmueble);
                        default -> {}
                    }
                }

                restauradas++;
            } catch (Exception e) {
                System.err.println("⚠️  Error restaurando interacción " + p.idInteraccion
                        + ": " + e.getMessage());
            }
        }

        System.out.println("   💬 Interacciones restauradas: " + restauradas + "/" + persistidas.size());
    }

    private <T> List<T> cargar(String ruta, Class<T> clase) {
        File archivo = new File(ruta);
        if (!archivo.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(archivo,
                    mapper.getTypeFactory().constructCollectionType(List.class, clase));
        } catch (IOException e) {
            System.err.println("⚠️  [Persistencia] Error cargando " + ruta + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}