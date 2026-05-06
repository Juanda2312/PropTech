package co.edu.uniquindio.poo.PropTech.model.dto;

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
public class AlertaDTO {
    private String idAlerta;
    private String tipoAlerta;
    private String descripcion;
    private LocalDate fechaGeneracion;
    private NivelAtencion nivel;
    private boolean cerrada;
}