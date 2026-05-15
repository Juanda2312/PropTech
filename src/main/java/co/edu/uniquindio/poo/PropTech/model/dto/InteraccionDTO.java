package co.edu.uniquindio.poo.PropTech.model.dto;

import co.edu.uniquindio.poo.PropTech.model.enums.TipoInteraccion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para registrar una interacción desde el portal del cliente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InteraccionDTO {

    /** ID único de la interacción. Se genera automáticamente si se omite. */
    private String idInteraccion;

    /** ID del cliente que genera la interacción */
    private String idCliente;

    /** Código del inmueble con el que se interactúa */
    private String codigoInmueble;

    /** Tipo de interacción */
    private TipoInteraccion tipoInteraccion;

    /** Mensaje o detalle adicional (opcional) */
    private String detalle;

    // Campos específicos para INTENCION_COMPRA / INTENCION_RENTA
    private Double presupuestoDeclarado;
    private String mensajeCliente;

    // Campos específicos para VISITA_AGENDADA (agendar desde el portal)
    private String fecha;   // LocalDate ISO: "2025-05-15"
    private String hora;    // LocalTime ISO: "10:30"
    private String idAsesor; // asesor asignado (puede ser null para auto-asignar)
}