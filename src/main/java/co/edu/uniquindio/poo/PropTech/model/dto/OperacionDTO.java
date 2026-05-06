package co.edu.uniquindio.poo.PropTech.model.dto;

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
public class OperacionDTO {
    private String idOperacion;
    private String codigoInmueble;
    private String idCliente;
    private String idAsesor;
    private LocalDate fecha;
    private TipoOperacion tipoOperacion;
    private double valorAcordado;
    private double comision;
    private String estadoProceso;
}