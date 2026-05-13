package co.edu.uniquindio.poo.PropTech.model.entity;

import co.edu.uniquindio.poo.PropTech.model.enums.EstadoVisita;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Visita implements Comparable<Visita> {

    private String idVisita;

    @JsonIgnoreProperties({"inmueblesConsultados","propiedadesVisitadas",
            "inmueblesDescartados","inmueblesGuardados","inmueblesNegociados","listaRecomendaciones"})
    private Cliente cliente;

    @JsonIgnoreProperties({"listaVisitas","asesor"})
    private Inmueble inmueble;
    private LocalDate fecha;
    private LocalTime hora;

    @JsonIgnoreProperties({"inmueblesAsignados","visitasAgendadas","cierresRealizados"})
    private Asesor asesor;
    private EstadoVisita estado;
    private String observaciones;

    @Override
    public int compareTo(Visita otra) {
        int cmp = this.fecha.compareTo(otra.fecha);
        return cmp != 0 ? cmp : this.hora.compareTo(otra.hora);
    }
}