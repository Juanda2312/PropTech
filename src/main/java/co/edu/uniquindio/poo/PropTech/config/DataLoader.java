package co.edu.uniquindio.poo.PropTech.config;

import co.edu.uniquindio.poo.PropTech.model.dto.*;
import co.edu.uniquindio.poo.PropTech.model.entity.*;
import co.edu.uniquindio.poo.PropTech.model.enums.*;
import co.edu.uniquindio.poo.PropTech.repository.*;
import co.edu.uniquindio.poo.PropTech.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final PlataformaBeta          plataforma;
    private final PersistenciaService     persistencia;
    private final AsesorRepository        asesorRepository;
    private final ClienteRepository       clienteRepository;
    private final InmuebleRepository      inmuebleRepository;
    private final VisitaRepository        visitaRepository;
    private final OperacionRepository     operacionRepository;
    private final AlertaRepository        alertaRepository;
    private final EventoInusualRepository eventoRepository;

    public DataLoader(PlataformaBeta plataforma,
                      PersistenciaService persistencia,
                      AsesorRepository asesorRepository,
                      ClienteRepository clienteRepository,
                      InmuebleRepository inmuebleRepository,
                      VisitaRepository visitaRepository,
                      OperacionRepository operacionRepository,
                      AlertaRepository alertaRepository,
                      EventoInusualRepository eventoRepository) {
        this.plataforma          = plataforma;
        this.persistencia        = persistencia;
        this.asesorRepository    = asesorRepository;
        this.clienteRepository   = clienteRepository;
        this.inmuebleRepository  = inmuebleRepository;
        this.visitaRepository    = visitaRepository;
        this.operacionRepository = operacionRepository;
        this.alertaRepository    = alertaRepository;
        this.eventoRepository    = eventoRepository;
    }

    @Override
    public void run(String... args) {
        if (persistencia.hayDatosGuardados()) {
            System.out.println("📂 [DataLoader] Cargando datos desde archivos guardados...");
            cargarDesdeArchivos();
            System.out.println("✅ [DataLoader] Datos restaurados correctamente.");
        } else {
            System.out.println("🆕 [DataLoader] Primera ejecución, cargando datos de prueba...");
            cargarDatosPrueba();
            System.out.println("✅ [DataLoader] Datos de prueba cargados correctamente.");
        }
    }

    // ================================================================
    // RESTAURAR DESDE ARCHIVOS
    // ================================================================

    private void cargarDesdeArchivos() {
        for (Asesor a : persistencia.cargarAsesores()) {
            try { asesorRepository.save(a); } catch (Exception ignored) {}
        }
        for (Cliente c : persistencia.cargarClientes()) {
            try {
                clienteRepository.save(c);
                plataforma.agregarNodoGrafo(c.getId());
            } catch (Exception ignored) {}
        }
        for (Inmueble i : persistencia.cargarInmuebles()) {
            try {
                if (i.getAsesor() != null) {
                    Asesor asesor = asesorRepository.findById(i.getAsesor().getId()).orElse(i.getAsesor());
                    i.setAsesor(asesor);
                    asesor.getInmueblesAsignados().addLast(i);
                }
                inmuebleRepository.save(i);
                plataforma.agregarNodoGrafo(i.getCodigo());
            } catch (Exception ignored) {}
        }
        for (Visita v : persistencia.cargarVisitas()) {
            try {
                if (v.getCliente() != null)
                    v.setCliente(clienteRepository.findById(v.getCliente().getId()).orElse(v.getCliente()));
                if (v.getInmueble() != null)
                    v.setInmueble(inmuebleRepository.findById(v.getInmueble().getCodigo()).orElse(v.getInmueble()));
                if (v.getAsesor() != null)
                    v.setAsesor(asesorRepository.findById(v.getAsesor().getId()).orElse(v.getAsesor()));
                visitaRepository.save(v);
                if (v.getCliente() != null && v.getInmueble() != null)
                    plataforma.agregarAristaGrafo(v.getCliente().getId(), v.getInmueble().getCodigo());
            } catch (Exception ignored) {}
        }
        for (Operacion op : persistencia.cargarOperaciones()) {
            try {
                if (op.getInmueble() != null)
                    op.setInmueble(inmuebleRepository.findById(op.getInmueble().getCodigo()).orElse(op.getInmueble()));
                if (op.getCliente() != null)
                    op.setCliente(clienteRepository.findById(op.getCliente().getId()).orElse(op.getCliente()));
                if (op.getAsesor() != null)
                    op.setAsesor(asesorRepository.findById(op.getAsesor().getId()).orElse(op.getAsesor()));
                operacionRepository.save(op);
            } catch (Exception ignored) {}
        }
        for (Alerta al : persistencia.cargarAlertas()) {
            try { alertaRepository.save(al); } catch (Exception ignored) {}
        }
        for (EventoInusual ev : persistencia.cargarEventos()) {
            try { eventoRepository.save(ev); } catch (Exception ignored) {}
        }
    }

    // ================================================================
    // DATOS DE PRUEBA (primera ejecución)
    // ================================================================

    private void cargarDatosPrueba() {
        try { cargarAsesores(); }    catch (Exception e) { System.err.println("⚠️ " + e.getMessage()); }
        try { cargarClientes(); }    catch (Exception e) { System.err.println("⚠️ " + e.getMessage()); }
        try { cargarInmuebles(); }   catch (Exception e) { System.err.println("⚠️ " + e.getMessage()); }
        try { cargarVisitas(); }     catch (Exception e) { System.err.println("⚠️ " + e.getMessage()); }
        try { cargarOperaciones(); } catch (Exception e) { System.err.println("⚠️ " + e.getMessage()); }
    }

    // IDs asesores: cédula colombiana de exactamente 10 dígitos
    private void cargarAsesores() {
        String[][] asesores = {
                {"1071234567", "Carlos Mendoza",  "3101234567", "Norte"},
                {"1043567890", "Laura Jiménez",   "3209876543", "Sur"},
                {"1015678901", "Andrés Vargas",   "3154567890", "Centro"},
                {"1032456789", "Patricia Ruiz",   "3001112233", "Oeste"},
                {"1098765432", "Miguel Torres",   "3187654321", "Este"},
        };
        for (String[] a : asesores)
            try { plataforma.registrarAsesor(new AsesorDTO(a[0], a[1], a[2], a[3])); }
            catch (Exception ignored) {}
    }

    // IDs clientes: cédula colombiana de exactamente 10 dígitos (distintas a asesores)
    private void cargarClientes() {
        Object[][] clientes = {
                {"1094567890","Juan Pérez",     "juan@gmail.com",    "3112345678","COMPRADOR",    450_000_000d, new Zona[]{Zona.NORTE},           TipoInmueble.APARTAMENTO,    2, EstadoBusqueda.ACTIVO},
                {"1006234567","María García",   "maria@gmail.com",   "3223456789","ARRENDATARIO",   2_500_000d, new Zona[]{Zona.SUR},             TipoInmueble.CASA,           3, EstadoBusqueda.ACTIVO},
                {"1093123456","Carlos López",   "carlos@gmail.com",  "3334567890","COMPRADOR",    800_000_000d, new Zona[]{Zona.NORTE,Zona.ESTE},  TipoInmueble.CASA,           4, EstadoBusqueda.ACTIVO},
                {"1005678901","Ana Martínez",   "ana@hotmail.com",   "3445678901","ARRENDATARIO",   1_800_000d, new Zona[]{Zona.OESTE},           TipoInmueble.APARTAMENTO,    1, EstadoBusqueda.ACTIVO},
                {"1094890123","Pedro Gómez",    "pedro@gmail.com",   "3556789012","COMPRADOR",    300_000_000d, new Zona[]{Zona.SUR},             TipoInmueble.APARTAMENTO,    2, EstadoBusqueda.EN_PAUSA},
                {"1007345678","Sofía Herrera",  "sofia@gmail.com",   "3667890123","COMPRADOR",  1_200_000_000d, new Zona[]{Zona.NORTE},           TipoInmueble.OFICINA,        0, EstadoBusqueda.ACTIVO},
                {"1095012345","Tomás Díaz",     "tomas@gmail.com",   "3778901234","ARRENDATARIO",   3_500_000d, new Zona[]{Zona.ESTE},            TipoInmueble.LOCAL_COMERCIAL,0, EstadoBusqueda.ACTIVO},
                {"1008901234","Valentina Cruz", "vale@gmail.com",    "3889012345","COMPRADOR",    550_000_000d, new Zona[]{Zona.NORTE,Zona.SUR},   TipoInmueble.CASA,           3, EstadoBusqueda.ACTIVO},
                {"1090123456","Ricardo Mora",   "ricardo@gmail.com", "3990123456","COMPRADOR",    200_000_000d, new Zona[]{Zona.SUR},             TipoInmueble.LOTE,           0, EstadoBusqueda.ACTIVO},
                {"1001234560","Daniela Ospina", "dani@gmail.com",    "3101234560","ARRENDATARIO",   2_000_000d, new Zona[]{Zona.NORTE},           TipoInmueble.APARTAMENTO,    2, EstadoBusqueda.ACTIVO},
        };
        for (Object[] c : clientes)
            try {
                plataforma.registrarCliente(new ClienteDTO(
                        (String)c[0], (String)c[1], (String)c[2], (String)c[3],
                        (String)c[4], (Double)c[5], (Zona[])c[6],
                        (TipoInmueble)c[7], (Integer)c[8], (EstadoBusqueda)c[9]
                ));
            } catch (Exception ignored) {}
    }

    // IDs inmuebles: matrícula inmobiliaria estilo colombiano (municipio-número)
    private void cargarInmuebles() {
        Object[][] inmuebles = {
                {"001-1234567","Calle 10 # 43-20", "Medellín","El Poblado",    TipoInmueble.APARTAMENTO,   FinalidadInmueble.VENTA,    380_000_000d,  85d,3,2,"DISPONIBLE",true, "1071234567"},
                {"001-2345678","Cra 70 # 34-15",   "Medellín","Laureles",      TipoInmueble.CASA,          FinalidadInmueble.VENTA,    650_000_000d, 180d,4,3,"DISPONIBLE",true, "1071234567"},
                {"001-3456789","Av 80 # 12-05",    "Medellín","Robledo",       TipoInmueble.APARTAMENTO,   FinalidadInmueble.ARRIENDO,   1_800_000d,  60d,2,1,"DISPONIBLE",true, "1043567890"},
                {"001-4567890","Calle 50 # 55-30", "Medellín","Ciudad del Río",TipoInmueble.APARTAMENTO,   FinalidadInmueble.VENTA,    520_000_000d, 110d,3,2,"DISPONIBLE",true, "1043567890"},
                {"001-5678901","Cra 43 # 18-90",   "Medellín","El Poblado",    TipoInmueble.OFICINA,       FinalidadInmueble.ARRIENDO,   4_500_000d,  95d,0,1,"DISPONIBLE",true, "1015678901"},
                {"001-6789012","Calle 33 # 76-10", "Medellín","Belén",         TipoInmueble.CASA,          FinalidadInmueble.VENTA,    420_000_000d, 150d,4,2,"DISPONIBLE",true, "1015678901"},
                {"011-1234567","Cra 65 # 44-22",   "Bogotá",  "Chapinero",     TipoInmueble.APARTAMENTO,   FinalidadInmueble.VENTA,    310_000_000d,  75d,2,2,"DISPONIBLE",true, "1032456789"},
                {"011-2345678","Calle 72 # 11-30", "Bogotá",  "Usaquén",       TipoInmueble.CASA,          FinalidadInmueble.VENTA,    890_000_000d, 220d,5,3,"DISPONIBLE",true, "1032456789"},
                {"011-3456789","Cra 15 # 93-40",   "Bogotá",  "Chico",         TipoInmueble.OFICINA,       FinalidadInmueble.VENTA,    750_000_000d, 130d,0,2,"DISPONIBLE",true, "1098765432"},
                {"076-1234567","Calle 5 # 39-20",  "Cali",    "Granada",       TipoInmueble.APARTAMENTO,   FinalidadInmueble.ARRIENDO,   2_200_000d,  70d,3,2,"DISPONIBLE",true, "1098765432"},
                {"076-2345678","Cra 100 # 16-55",  "Cali",    "Ciudad Jardín", TipoInmueble.CASA,          FinalidadInmueble.VENTA,    480_000_000d, 160d,4,3,"DISPONIBLE",true, "1071234567"},
                {"011-4567890","Calle 19 # 6-30",  "Bogotá",  "La Candelaria", TipoInmueble.LOCAL_COMERCIAL,FinalidadInmueble.ARRIENDO,  3_800_000d,  45d,0,1,"DISPONIBLE",true, "1043567890"},
                {"001-7890123","Cra 28 # 14-10",   "Medellín","Guayabal",      TipoInmueble.BODEGA,        FinalidadInmueble.ARRIENDO,   5_500_000d, 300d,0,1,"DISPONIBLE",true, "1015678901"},
                {"011-5678901","Calle 80 # 50-40", "Bogotá",  "Modelia",       TipoInmueble.APARTAMENTO,   FinalidadInmueble.VENTA,    270_000_000d,  65d,2,1,"DISPONIBLE",true, "1032456789"},
                {"011-6789012","Cra 9 # 77-25",    "Bogotá",  "Rosales",       TipoInmueble.CASA,          FinalidadInmueble.VENTA,  1_100_000_000d, 280d,5,4,"DISPONIBLE",true, "1098765432"},
                {"001-8901234","Calle 34 # 43-15", "Medellín","El Estadio",    TipoInmueble.APARTAMENTO,   FinalidadInmueble.ARRIENDO,   1_600_000d,  55d,2,1,"DISPONIBLE",true, "1071234567"},
                {"001-9012345","Cra 48 # 22-60",   "Medellín","Buenos Aires",  TipoInmueble.LOTE,          FinalidadInmueble.VENTA,    180_000_000d, 400d,0,0,"DISPONIBLE",true, "1043567890"},
                {"076-3456789","Calle 16 # 85-30", "Cali",    "San Fernando",  TipoInmueble.APARTAMENTO,   FinalidadInmueble.VENTA,    230_000_000d,  72d,2,2,"DISPONIBLE",true, "1015678901"},
                {"011-7890123","Cra 30 # 45-10",   "Bogotá",  "Teusaquillo",   TipoInmueble.OFICINA,       FinalidadInmueble.ARRIENDO,   6_200_000d, 150d,0,2,"DISPONIBLE",true, "1032456789"},
                {"076-4567890","Calle 10 # 1-30",  "Cali",    "El Peñón",      TipoInmueble.CASA,          FinalidadInmueble.VENTA,    560_000_000d, 190d,4,3,"DISPONIBLE",true, "1098765432"},
        };
        for (Object[] i : inmuebles)
            try {
                plataforma.registrarInmueble(new InmuebleDTO(
                        (String)i[0],  (String)i[1],  (String)i[2],  (String)i[3],
                        (TipoInmueble)i[4], (FinalidadInmueble)i[5], (Double)i[6],
                        (Double)i[7],  (Integer)i[8], (Integer)i[9],
                        (String)i[10], (Boolean)i[11], (String)i[12]
                ));
            } catch (Exception ignored) {}
    }

    // IDs visitas: V + número (sin guiones, sin prefijos largos)
    private void cargarVisitas() {
        Object[][] visitas = {
                {"V001","1094567890","001-1234567", LocalDate.now().minusDays(10), LocalTime.of(10,  0), "1071234567", "Muy interesado"},
                {"V002","1006234567","001-3456789", LocalDate.now().minusDays(8),  LocalTime.of(11, 30), "1043567890", "Evaluando sector"},
                {"V003","1093123456","001-2345678", LocalDate.now().minusDays(7),  LocalTime.of(15,  0), "1071234567", "Cumple expectativas"},
                {"V004","1005678901","001-8901234", LocalDate.now().minusDays(6),  LocalTime.of( 9,  0), "1071234567", "Interesada en precio"},
                {"V005","1094890123","011-5678901", LocalDate.now().minusDays(5),  LocalTime.of(14,  0), "1032456789", "Comparando inmuebles"},
                {"V006","1007345678","001-5678901", LocalDate.now().minusDays(5),  LocalTime.of(16,  0), "1015678901", "Requiere ampliación"},
                {"V007","1095012345","011-4567890", LocalDate.now().minusDays(4),  LocalTime.of(10, 30), "1043567890", "Flujo peatonal alto"},
                {"V008","1008901234","001-6789012", LocalDate.now().minusDays(3),  LocalTime.of(11,  0), "1015678901", "Buen estado general"},
                {"V009","1094567890","001-4567890", LocalDate.now().minusDays(3),  LocalTime.of(15, 30), "1043567890", "Segunda visita"},
                {"V010","1093123456","011-2345678", LocalDate.now().minusDays(2),  LocalTime.of( 9, 30), "1032456789", "Evaluando barrio"},
        };
        for (Object[] v : visitas)
            try {
                plataforma.agendarVisita(new VisitaDTO(
                        (String)v[0], (String)v[1], (String)v[2],
                        (LocalDate)v[3], (LocalTime)v[4], (String)v[5],
                        null, (String)v[6]
                ));
            } catch (Exception ignored) {}
    }

    // IDs operaciones: O + número simple
    private void cargarOperaciones() {
        Object[][] ops = {
                {"O001","001-1234567","1094567890","1071234567", LocalDate.now().minusDays(5), TipoOperacion.VENTA,    380_000_000d, 3d, "CERRADO"},
                {"O002","001-3456789","1006234567","1043567890", LocalDate.now().minusDays(4), TipoOperacion.ARRIENDO,   1_800_000d, 8d, "CERRADO"},
                {"O003","001-6789012","1008901234","1015678901", LocalDate.now().minusDays(3), TipoOperacion.VENTA,    415_000_000d, 3d, "EN_PROCESO"},
                {"O004","011-4567890","1095012345","1043567890", LocalDate.now().minusDays(2), TipoOperacion.ARRIENDO,   3_800_000d, 8d, "EN_PROCESO"},
        };
        for (Object[] op : ops)
            try {
                plataforma.registrarOperacion(new OperacionDTO(
                        (String)op[0], (String)op[1], (String)op[2], (String)op[3],
                        (LocalDate)op[4], (TipoOperacion)op[5],
                        (Double)op[6], (Double)op[7], (String)op[8], null
                ));
            } catch (Exception ignored) {}
    }
}