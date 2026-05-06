package co.edu.uniquindio.poo.PropTech.model.entity;

import co.edu.uniquindio.poo.PropTech.model.enums.TipoOperacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Operacion implements Comparable<Operacion> {

    private String idOperacion;
    private Inmueble inmueble;
    private Cliente cliente;
    private Asesor asesor;
    private LocalDate fecha;
    private TipoOperacion tipoOperacion;
    private double valorAcordado;
    private double comision;
    private String estadoProceso;

    public double calcularComision() {
        return valorAcordado * comision / 100.0;
    }

    @Override
    public int compareTo(Operacion otra) {
        return this.fecha.compareTo(otra.fecha);
    }
}