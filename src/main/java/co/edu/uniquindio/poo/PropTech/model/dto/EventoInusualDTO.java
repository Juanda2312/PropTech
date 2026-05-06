package co.edu.uniquindio.poo.PropTech.model.dto;

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
public class EventoInusualDTO {
    private String idEvento;
    private String tipoEvento;
    private String descripcion;
    private LocalDate fechaDeteccion;
    private NivelAtencion nivelAtencion;
    private EstadoEvento estadoEvento;
}