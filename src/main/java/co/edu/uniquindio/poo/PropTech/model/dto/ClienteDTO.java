package co.edu.uniquindio.poo.PropTech.model.dto;

import co.edu.uniquindio.poo.PropTech.model.enums.EstadoBusqueda;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.Zona;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private String id;
    private String nombre;
    private String correo;
    private String telefono;
    private String tipoCliente;
    private double presupuesto;
    private Zona[] zonasInteres;
    private TipoInmueble tipoInmuebleDeseado;
    private int habitacionesMinimas;
    private EstadoBusqueda estadoBusqueda;
}