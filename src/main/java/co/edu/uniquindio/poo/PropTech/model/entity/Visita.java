package co.edu.uniquindio.poo.PropTech.model.entity;

import co.edu.uniquindio.poo.PropTech.model.enums.EstadoVisita;
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
    private Cliente cliente;
    private Inmueble inmueble;
    private LocalDate fecha;
    private LocalTime hora;
    private Asesor asesor;
    private EstadoVisita estado;
    private String observaciones;

    @Override
    public int compareTo(Visita otra) {
        int cmp = this.fecha.compareTo(otra.fecha);
        return cmp != 0 ? cmp : this.hora.compareTo(otra.hora);
    }
}