package co.edu.uniquindio.poo.PropTech.model.dto;

import co.edu.uniquindio.poo.PropTech.model.enums.FinalidadInmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInmueble;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InmuebleDTO {
    private String codigo;
    private String direccion;
    private String ciudad;
    private String barrio;
    private TipoInmueble tipoInmueble;
    private FinalidadInmueble finalidad;
    private double precio;
    private double area;
    private int habitaciones;
    private int banos;
    private String estado;
    private boolean disponibilidad;
    private String idAsesor;
}