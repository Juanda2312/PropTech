package co.edu.uniquindio.poo.PropTech.model.entity;

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
public class Recomendacion implements Comparable<Recomendacion> {

    private String idRecomendacion;

    @JsonIgnoreProperties({"listaVisitas","asesor"})
    private Inmueble inmueble;
    private double puntaje;
    private String criterio;
    private LocalDate fechaGeneracion;

    public double calcularCoincidencia(Cliente cliente) {
        double score = 0;
        if (inmueble.getPrecio() <= cliente.getPresupuesto()) score += 40;
        if (inmueble.getTipoInmueble() == cliente.getTipoInmuebleDeseado()) score += 30;
        if (inmueble.getHabitaciones() >= cliente.getHabitacionesMinimas()) score += 30;
        return score;
    }

    @Override
    public int compareTo(Recomendacion otra) {
        return Double.compare(this.puntaje, otra.puntaje); // mayor puntaje primero
    }
}