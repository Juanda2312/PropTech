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
import java.util.List;

@Service
public class PersistenciaService {

    private static final String DIR = "data/";
    private static final String ASESORES_FILE    = DIR + "asesores.json";
    private static final String CLIENTES_FILE    = DIR + "clientes.json";
    private static final String INMUEBLES_FILE   = DIR + "inmuebles.json";
    private static final String VISITAS_FILE     = DIR + "visitas.json";
    private static final String OPERACIONES_FILE = DIR + "operaciones.json";
    private static final String ALERTAS_FILE     = DIR + "alertas.json";
    private static final String EVENTOS_FILE     = DIR + "eventos.json";

    private final ObjectMapper mapper;
    private final AsesorRepository     asesorRepository;
    private final ClienteRepository    clienteRepository;
    private final InmuebleRepository   inmuebleRepository;
    private final VisitaRepository     visitaRepository;
    private final OperacionRepository  operacionRepository;
    private final AlertaRepository     alertaRepository;
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

        new File(DIR).mkdirs();
    }

    // ================================================================
    // GUARDAR AL APAGAR
    // ================================================================

    @PreDestroy
    public void guardarTodo() {
        System.out.println("💾 [Persistencia] Guardando datos...");
        guardar(ASESORES_FILE,    asesorRepository.findAll());
        guardar(CLIENTES_FILE,    clienteRepository.findAll());
        guardar(INMUEBLES_FILE,   inmuebleRepository.findAll());
        guardar(VISITAS_FILE,     visitaRepository.findAll());
        guardar(OPERACIONES_FILE, operacionRepository.findAll());
        guardar(ALERTAS_FILE,     alertaRepository.findAll());
        guardar(EVENTOS_FILE,     eventoRepository.findAll());
        System.out.println("✅ [Persistencia] Datos guardados correctamente en /data/");
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

    public List<Asesor> cargarAsesores() {
        return cargar(ASESORES_FILE, Asesor.class);
    }

    public List<Cliente> cargarClientes() {
        return cargar(CLIENTES_FILE, Cliente.class);
    }

    public List<Inmueble> cargarInmuebles() {
        return cargar(INMUEBLES_FILE, Inmueble.class);
    }

    public List<Visita> cargarVisitas() {
        return cargar(VISITAS_FILE, Visita.class);
    }

    public List<Operacion> cargarOperaciones() {
        return cargar(OPERACIONES_FILE, Operacion.class);
    }

    public List<Alerta> cargarAlertas() {
        return cargar(ALERTAS_FILE, Alerta.class);
    }

    public List<EventoInusual> cargarEventos() {
        return cargar(EVENTOS_FILE, EventoInusual.class);
    }

    private <T> List<T> cargar(String ruta, Class<T> clase) {
        File archivo = new File(ruta);
        if (!archivo.exists()) return new java.util.ArrayList<>();
        try {
            return mapper.readValue(archivo,
                    mapper.getTypeFactory().constructCollectionType(List.class, clase));
        } catch (IOException e) {
            System.err.println("⚠️  [Persistencia] Error cargando " + ruta + ": " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}