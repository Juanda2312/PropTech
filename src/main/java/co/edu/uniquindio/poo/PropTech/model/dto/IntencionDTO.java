package co.edu.uniquindio.poo.PropTech.model.dto;

import co.edu.uniquindio.poo.PropTech.model.enums.TipoInteraccion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntencionDTO {
    private String codigoInmueble;
    private TipoInteraccion tipo; // INTENCION_COMPRA o INTENCION_RENTA
    private String detalle;
}