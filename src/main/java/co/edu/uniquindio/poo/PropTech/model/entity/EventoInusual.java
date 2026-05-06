package co.edu.uniquindio.poo.PropTech.model.entity;

import co.edu.uniquindio.poo.PropTech.model.enums.EstadoEvento;
import co.edu.uniquindio.poo.PropTech.model.enums.NivelAtencion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoInusual implements Comparable<EventoInusual> {

    private String idEvento;
    private String tipoEvento;
    private String descripcion;
    private LocalDate fechaDeteccion;
    private NivelAtencion nivelAtencion;
    private EstadoEvento estadoEvento;

    @Override
    public int compareTo(EventoInusual otro) {
        return otro.nivelAtencion.ordinal() - this.nivelAtencion.ordinal();
    }
}