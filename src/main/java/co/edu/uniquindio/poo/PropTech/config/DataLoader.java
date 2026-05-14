package co.edu.uniquindio.poo.PropTech.config;

import co.edu.uniquindio.poo.PropTech.model.dto.*;
import co.edu.uniquindio.poo.PropTech.model.enums.*;
import co.edu.uniquindio.poo.PropTech.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final PlataformaBeta plataforma;

    public DataLoader(PlataformaBeta plataforma) {
        this.plataforma = plataforma;
    }

    @Override
    public void run(String... args) {
        try {
            cargarAsesores();
            cargarClientes();
            cargarInmuebles();
            cargarVisitas();
            cargarOperaciones();
            System.out.println("✅ [DataLoader] Datos de prueba cargados correctamente.");
        } catch (Exception e) {
            System.err.println("⚠️  [DataLoader] Error cargando datos: " + e.getMessage());
        }
    }

    // ================================================================
    // ASESORES
    // ================================================================
    private void cargarAsesores() {
        String[][] asesores = {
                {"ASR-001", "Carlos Mendoza",    "3101234567", "Norte"},
                {"ASR-002", "Laura Jiménez",     "3209876543", "Sur"},
                {"ASR-003", "Andrés Vargas",     "3154567890", "Centro"},
                {"ASR-004", "Patricia Ruiz",     "3001112233", "Oeste"},
                {"ASR-005", "Miguel Torres",     "3187654321", "Este"},
        };
        for (String[] a : asesores) {
            try {
                plataforma.registrarAsesor(new AsesorDTO(a[0], a[1], a[2], a[3]));
            } catch (Exception ignored) {}
        }
    }

    // ================================================================
    // CLIENTES
    // ================================================================
    private void cargarClientes() {
        Object[][] clientes = {
                {"CLI-001","Juan Pérez",      "juan@gmail.com",    "3112345678", "COMPRADOR",    450_000_000d, new Zona[]{Zona.NORTE},       TipoInmueble.APARTAMENTO, 2, EstadoBusqueda.ACTIVO},
                {"CLI-002","María García",    "maria@gmail.com",   "3223456789", "ARRENDATARIO", 2_500_000d,   new Zona[]{Zona.SUR},          TipoInmueble.CASA,        3, EstadoBusqueda.ACTIVO},
                {"CLI-003","Carlos López",    "carlos@gmail.com",  "3334567890", "COMPRADOR",    800_000_000d, new Zona[]{Zona.NORTE,Zona.ESTE}, TipoInmueble.CASA,      4, EstadoBusqueda.ACTIVO},
                {"CLI-004","Ana Martínez",    "ana@hotmail.com",   "3445678901", "ARRENDATARIO", 1_800_000d,   new Zona[]{Zona.OESTE},        TipoInmueble.APARTAMENTO, 1, EstadoBusqueda.ACTIVO},
                {"CLI-005","Pedro Gómez",     "pedro@gmail.com",   "3556789012", "COMPRADOR",    300_000_000d, new Zona[]{Zona.SUR},          TipoInmueble.APARTAMENTO, 2, EstadoBusqueda.EN_PAUSA},
                {"CLI-006","Sofía Herrera",   "sofia@gmail.com",   "3667890123", "COMPRADOR",    1_200_000_000d,new Zona[]{Zona.NORTE},       TipoInmueble.OFICINA,     0, EstadoBusqueda.ACTIVO},
                {"CLI-007","Tomás Díaz",      "tomas@gmail.com",   "3778901234", "ARRENDATARIO", 3_500_000d,   new Zona[]{Zona.ESTE},         TipoInmueble.LOCAL_COMERCIAL, 0, EstadoBusqueda.ACTIVO},
                {"CLI-008","Valentina Cruz",  "vale@gmail.com",    "3889012345", "COMPRADOR",    550_000_000d, new Zona[]{Zona.NORTE,Zona.SUR}, TipoInmueble.CASA,      3, EstadoBusqueda.ACTIVO},
                {"CLI-009","Ricardo Mora",    "ricardo@gmail.com", "3990123456", "COMPRADOR",    200_000_000d, new Zona[]{Zona.SUR},          TipoInmueble.LOTE,        0, EstadoBusqueda.ACTIVO},
                {"CLI-010","Daniela Ospina",  "dani@gmail.com",    "3101234560", "ARRENDATARIO", 2_000_000d,   new Zona[]{Zona.NORTE},        TipoInmueble.APARTAMENTO, 2, EstadoBusqueda.ACTIVO},
        };
        for (Object[] c : clientes) {
            try {
                plataforma.registrarCliente(new ClienteDTO(
                        (String)c[0], (String)c[1], (String)c[2], (String)c[3],
                        (String)c[4], (Double)c[5], (Zona[])c[6],
                        (TipoInmueble)c[7], (Integer)c[8], (EstadoBusqueda)c[9]
                ));
            } catch (Exception ignored) {}
        }
    }

    // ================================================================
    // INMUEBLES
    // ================================================================
    private void cargarInmuebles() {
        Object[][] inmuebles = {
                // codigo, dir, ciudad, barrio, tipo, finalidad, precio, area, hab, ban, estado, disp, asesor
                {"INM-001","Calle 10 # 43-20","Medellín","El Poblado",    TipoInmueble.APARTAMENTO,  FinalidadInmueble.VENTA,    380_000_000d, 85d,  3, 2, "DISPONIBLE", true,  "ASR-001"},
                {"INM-002","Cra 70 # 34-15", "Medellín","Laureles",       TipoInmueble.CASA,         FinalidadInmueble.VENTA,    650_000_000d, 180d, 4, 3, "DISPONIBLE", true,  "ASR-001"},
                {"INM-003","Av 80 # 12-05",  "Medellín","Robledo",        TipoInmueble.APARTAMENTO,  FinalidadInmueble.ARRIENDO, 1_800_000d,   60d,  2, 1, "DISPONIBLE", true,  "ASR-002"},
                {"INM-004","Calle 50 # 55-30","Medellín","Ciudad del Río", TipoInmueble.APARTAMENTO,  FinalidadInmueble.VENTA,    520_000_000d, 110d, 3, 2, "DISPONIBLE", true,  "ASR-002"},
                {"INM-005","Cra 43 # 18-90", "Medellín","El Poblado",     TipoInmueble.OFICINA,      FinalidadInmueble.ARRIENDO, 4_500_000d,   95d,  0, 1, "DISPONIBLE", true,  "ASR-003"},
                {"INM-006","Calle 33 # 76-10","Medellín","Belén",         TipoInmueble.CASA,         FinalidadInmueble.VENTA,    420_000_000d, 150d, 4, 2, "DISPONIBLE", true,  "ASR-003"},
                {"INM-007","Cra 65 # 44-22", "Bogotá",  "Chapinero",      TipoInmueble.APARTAMENTO,  FinalidadInmueble.VENTA,    310_000_000d, 75d,  2, 2, "DISPONIBLE", true,  "ASR-004"},
                {"INM-008","Calle 72 # 11-30","Bogotá",  "Usaquén",       TipoInmueble.CASA,         FinalidadInmueble.VENTA,    890_000_000d, 220d, 5, 3, "DISPONIBLE", true,  "ASR-004"},
                {"INM-009","Cra 15 # 93-40", "Bogotá",  "Chico",          TipoInmueble.OFICINA,      FinalidadInmueble.VENTA,    750_000_000d, 130d, 0, 2, "DISPONIBLE", true,  "ASR-005"},
                {"INM-010","Calle 5 # 39-20","Cali",    "Granada",        TipoInmueble.APARTAMENTO,  FinalidadInmueble.ARRIENDO, 2_200_000d,   70d,  3, 2, "DISPONIBLE", true,  "ASR-005"},
                {"INM-011","Cra 100 # 16-55","Cali",    "Ciudad Jardín",  TipoInmueble.CASA,         FinalidadInmueble.VENTA,    480_000_000d, 160d, 4, 3, "DISPONIBLE", true,  "ASR-001"},
                {"INM-012","Calle 19 # 6-30","Bogotá",  "La Candelaria",  TipoInmueble.LOCAL_COMERCIAL,FinalidadInmueble.ARRIENDO,3_800_000d,  45d,  0, 1, "DISPONIBLE", true,  "ASR-002"},
                {"INM-013","Cra 28 # 14-10","Medellín", "Guayabal",       TipoInmueble.BODEGA,       FinalidadInmueble.ARRIENDO, 5_500_000d,   300d, 0, 1, "DISPONIBLE", true,  "ASR-003"},
                {"INM-014","Calle 80 # 50-40","Bogotá", "Modelia",        TipoInmueble.APARTAMENTO,  FinalidadInmueble.VENTA,    270_000_000d, 65d,  2, 1, "DISPONIBLE", true,  "ASR-004"},
                {"INM-015","Cra 9 # 77-25",  "Bogotá",  "Rosales",        TipoInmueble.CASA,         FinalidadInmueble.VENTA,    1_100_000_000d,280d,5, 4, "DISPONIBLE", true,  "ASR-005"},
                {"INM-016","Calle 34 # 43-15","Medellín","El Estadio",    TipoInmueble.APARTAMENTO,  FinalidadInmueble.ARRIENDO, 1_600_000d,   55d,  2, 1, "DISPONIBLE", true,  "ASR-001"},
                {"INM-017","Cra 48 # 22-60", "Medellín","Buenos Aires",   TipoInmueble.LOTE,         FinalidadInmueble.VENTA,    180_000_000d, 400d, 0, 0, "DISPONIBLE", true,  "ASR-002"},
                {"INM-018","Calle 16 # 85-30","Cali",   "San Fernando",   TipoInmueble.APARTAMENTO,  FinalidadInmueble.VENTA,    230_000_000d, 72d,  2, 2, "DISPONIBLE", true,  "ASR-003"},
                {"INM-019","Cra 30 # 45-10", "Bogotá",  "Teusaquillo",    TipoInmueble.OFICINA,      FinalidadInmueble.ARRIENDO, 6_200_000d,   150d, 0, 2, "DISPONIBLE", true,  "ASR-004"},
                {"INM-020","Calle 10 # 1-30","Cali",    "El Peñón",       TipoInmueble.CASA,         FinalidadInmueble.VENTA,    560_000_000d, 190d, 4, 3, "DISPONIBLE", true,  "ASR-005"},
        };
        for (Object[] i : inmuebles) {
            try {
                InmuebleDTO dto = new InmuebleDTO(
                        (String)i[0],(String)i[1],(String)i[2],(String)i[3],
                        (TipoInmueble)i[4],(FinalidadInmueble)i[5],(Double)i[6],
                        (Double)i[7],(Integer)i[8],(Integer)i[9],
                        (String)i[10],(Boolean)i[11],(String)i[12]
                );
                plataforma.registrarInmueble(dto);
            } catch (Exception ignored) {}
        }
    }

    // ================================================================
    // VISITAS
    // ================================================================
    private void cargarVisitas() {
        Object[][] visitas = {
                {"VIS-001","CLI-001","INM-001", LocalDate.now().minusDays(10), LocalTime.of(10,0),  "ASR-001","Muy interesado, preguntó por financiación"},
                {"VIS-002","CLI-002","INM-003", LocalDate.now().minusDays(8),  LocalTime.of(11,30), "ASR-002","Cliente evaluando el sector"},
                {"VIS-003","CLI-003","INM-002", LocalDate.now().minusDays(7),  LocalTime.of(15,0),  "ASR-001","Inmueble cumple expectativas"},
                {"VIS-004","CLI-004","INM-016", LocalDate.now().minusDays(6),  LocalTime.of(9,0),   "ASR-001","Interesada en negociar precio"},
                {"VIS-005","CLI-005","INM-014", LocalDate.now().minusDays(5),  LocalTime.of(14,0),  "ASR-004","Comparando con otros inmuebles"},
                {"VIS-006","CLI-006","INM-005", LocalDate.now().minusDays(5),  LocalTime.of(16,0),  "ASR-003","Requiere ampliación de espacio"},
                {"VIS-007","CLI-007","INM-012", LocalDate.now().minusDays(4),  LocalTime.of(10,30), "ASR-002","Local bien ubicado, flujo peatonal alto"},
                {"VIS-008","CLI-008","INM-006", LocalDate.now().minusDays(3),  LocalTime.of(11,0),  "ASR-003","Casa con buen estado general"},
                {"VIS-009","CLI-001","INM-004", LocalDate.now().minusDays(3),  LocalTime.of(15,30), "ASR-002","Segunda visita, muy interesado"},
                {"VIS-010","CLI-003","INM-008", LocalDate.now().minusDays(2),  LocalTime.of(9,30),  "ASR-004","Evaluando el barrio"},
                {"VIS-011","CLI-009","INM-017", LocalDate.now().minusDays(2),  LocalTime.of(13,0),  "ASR-002","Quiere construir pronto"},
                {"VIS-012","CLI-010","INM-003", LocalDate.now().minusDays(1),  LocalTime.of(10,0),  "ASR-002","Presupuesto ajustado"},
                {"VIS-013","CLI-002","INM-010", LocalDate.now().minusDays(1),  LocalTime.of(14,30), "ASR-005","Prefiere Cali"},
                {"VIS-014","CLI-008","INM-011", LocalDate.now(),               LocalTime.of(11,0),  "ASR-001","En proceso de decisión"},
                {"VIS-015","CLI-006","INM-009", LocalDate.now(),               LocalTime.of(16,30), "ASR-005","Muy interesada en la oficina"},
                {"VIS-016","CLI-004","INM-003", LocalDate.now().plusDays(1),   LocalTime.of(10,0),  "ASR-002","Pendiente confirmar"},
                {"VIS-017","CLI-005","INM-007", LocalDate.now().plusDays(1),   LocalTime.of(15,0),  "ASR-004","Pendiente confirmar"},
                {"VIS-018","CLI-007","INM-019", LocalDate.now().plusDays(2),   LocalTime.of(9,0),   "ASR-004","Interesado en oficina grande"},
                {"VIS-019","CLI-010","INM-016", LocalDate.now().plusDays(2),   LocalTime.of(11,30), "ASR-001","Primera visita agendada"},
                {"VIS-020","CLI-003","INM-015", LocalDate.now().plusDays(3),   LocalTime.of(14,0),  "ASR-005","Casa de lujo, cliente premium"},
        };
        for (Object[] v : visitas) {
            try {
                VisitaDTO dto = new VisitaDTO(
                        (String)v[0],(String)v[1],(String)v[2],
                        (LocalDate)v[3],(LocalTime)v[4],(String)v[5],
                        null,(String)v[6]
                );
                plataforma.agendarVisita(dto);
            } catch (Exception ignored) {}
        }
    }

    // ================================================================
    // OPERACIONES
    // ================================================================
    private void cargarOperaciones() {
        Object[][] ops = {
                {"OP-001","INM-001","CLI-001","ASR-001", LocalDate.now().minusDays(5),  TipoOperacion.VENTA,    380_000_000d, 3d, "CERRADO"},
                {"OP-002","INM-003","CLI-002","ASR-002", LocalDate.now().minusDays(4),  TipoOperacion.ARRIENDO, 1_800_000d,   8d, "CERRADO"},
                {"OP-003","INM-006","CLI-008","ASR-003", LocalDate.now().minusDays(3),  TipoOperacion.VENTA,    415_000_000d, 3d, "EN_PROCESO"},
                {"OP-004","INM-012","CLI-007","ASR-002", LocalDate.now().minusDays(2),  TipoOperacion.ARRIENDO, 3_800_000d,   8d, "EN_PROCESO"},
                {"OP-005","INM-014","CLI-005","ASR-004", LocalDate.now().minusDays(1),  TipoOperacion.VENTA,    265_000_000d, 3d, "EN_PROCESO"},
                {"OP-006","INM-016","CLI-004","ASR-001", LocalDate.now(),               TipoOperacion.ARRIENDO, 1_600_000d,   8d, "EN_PROCESO"},
                {"OP-007","INM-002","CLI-003","ASR-001", LocalDate.now().minusDays(15), TipoOperacion.VENTA,    640_000_000d, 3d, "CERRADO"},
                {"OP-008","INM-010","CLI-002","ASR-005", LocalDate.now().minusDays(20), TipoOperacion.ARRIENDO, 2_200_000d,   8d, "CERRADO"},
        };
        for (Object[] op : ops) {
            try {
                OperacionDTO dto = new OperacionDTO(
                        (String)op[0],(String)op[1],(String)op[2],(String)op[3],
                        (LocalDate)op[4],(TipoOperacion)op[5],
                        (Double)op[6],(Double)op[7],(String)op[8],null
                );
                plataforma.registrarOperacion(dto);
            } catch (Exception ignored) {}
        }
    }
}