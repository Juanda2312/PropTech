package co.edu.uniquindio.poo.PropTech.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Interaccion implements Comparable<Interaccion> {

    private String id;
    private LocalDate fecha;
    private String tipoInteraccion;
    private String detalle;

    @Override
    public int compareTo(Interaccion otra) {
        return this.fecha.compareTo(otra.fecha);
    }
}