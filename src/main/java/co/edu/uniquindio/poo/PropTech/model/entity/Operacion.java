package co.edu.uniquindio.poo.PropTech.model.entity;

import co.edu.uniquindio.poo.PropTech.model.enums.TipoOperacion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Operacion implements Comparable<Operacion> {

    private String idOperacion;

    @JsonIgnoreProperties({"listaVisitas","asesor"})
    private Inmueble inmueble;

    @JsonIgnoreProperties({"inmueblesConsultados","propiedadesVisitadas",
            "inmueblesDescartados","inmueblesGuardados","inmueblesNegociados","listaRecomendaciones"})
    private Cliente cliente;

    @JsonIgnoreProperties({"inmueblesAsignados","visitasAgendadas","cierresRealizados"})
    private Asesor asesor;

    private LocalDate fecha;
    private TipoOperacion tipoOperacion;
    private double valorAcordado;
    private double comision;
    private String estadoProceso;

    // Fecha en que vence el contrato (arriendo o renovación).
    // Para VENTA y CANCELACION queda null
    private LocalDate fechaVencimiento;

    public double calcularComision() {
        return valorAcordado * comision / 100.0;
    }

    public boolean estaProximaAVencer(int diasUmbral) {
        if (fechaVencimiento == null) return false;
        LocalDate hoy = LocalDate.now();
        long diasRestantes = hoy.until(fechaVencimiento).getDays();
        return diasRestantes >= 0 && diasRestantes <= diasUmbral;
    }

    public boolean estaVencida() {
        if (fechaVencimiento == null) return false;
        return LocalDate.now().isAfter(fechaVencimiento);
    }

    @Override
    public int compareTo(Operacion otra) {
        return this.fecha.compareTo(otra.fecha);
    }
}