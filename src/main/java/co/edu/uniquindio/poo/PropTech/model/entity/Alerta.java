package co.edu.uniquindio.poo.PropTech.model.entity;

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
public class Alerta implements Comparable<Alerta> {

    private String idAlerta;
    private String tipoAlerta;
    private String descripcion;
    private LocalDate fechaGeneracion;
    private NivelAtencion nivel;
    private boolean cerrada;

    @Override
    public int compareTo(Alerta otra) {
        // CRITICO > ALTO > MEDIO > BAJO → mayor ordinal = mayor prioridad
        return Integer.compare(this.nivel.ordinal(), otra.nivel.ordinal());
    }
}