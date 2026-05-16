package co.edu.uniquindio.poo.PropTech.model.entity;

import co.edu.uniquindio.poo.PropTech.model.enums.TipoInteraccion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private TipoInteraccion tipoInteraccion;
    private String detalle;

    @JsonIgnoreProperties({"listaVisitas", "asesor"})
    private Inmueble inmueble;

    @Override
    public int compareTo(Interaccion otra) {
        return this.fecha.compareTo(otra.fecha);
    }
}