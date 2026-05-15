package co.edu.uniquindio.poo.PropTech.model.entity;

import co.edu.uniquindio.poo.PropTech.model.enums.TipoInteraccion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa una interacción del cliente con la plataforma.
 * Tipos: VISITA_AGENDADA, COMPRA_REALIZADA, RENTA_REALIZADA,
 *        FAVORITO_MARCADO, INTENCION_COMPRA, INTENCION_RENTA
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Interaccion implements Comparable<Interaccion> {

    private String idInteraccion;

    @JsonIgnoreProperties({"inmueblesConsultados", "propiedadesVisitadas",
            "inmueblesDescartados", "inmueblesGuardados", "inmueblesNegociados", "listaRecomendaciones"})
    private Cliente cliente;

    @JsonIgnoreProperties({"listaVisitas", "asesor"})
    private Inmueble inmueble;

    private TipoInteraccion tipoInteraccion;
    private String detalle;
    private LocalDateTime fechaHora;

    // Solo para INTENCION: presupuesto declarado, mensaje al asesor
    private Double presupuestoDeclarado;
    private String mensajeCliente;

    // Para VISITA_AGENDADA: referencia a la visita creada
    private String idVisitaRelacionada;

    @Override
    public int compareTo(Interaccion otra) {
        return otra.fechaHora.compareTo(this.fechaHora); // más reciente primero
    }
}