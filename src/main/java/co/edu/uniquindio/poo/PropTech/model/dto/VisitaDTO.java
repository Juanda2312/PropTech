package co.edu.uniquindio.poo.PropTech.model.dto;

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
public class VisitaDTO {
    private String idVisita;
    private String idCliente;
    private String codigoInmueble;
    private LocalDate fecha;
    private LocalTime hora;
    private String idAsesor;
    private EstadoVisita estado;
    private String observaciones;
}