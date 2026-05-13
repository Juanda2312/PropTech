package co.edu.uniquindio.poo.PropTech.model.entity;

import co.edu.uniquindio.poo.PropTech.model.enums.EstadoBusqueda;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.Zona;
import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Cliente extends Persona {

    private String correo;
    private String telefono;
    private String tipoCliente;
    private double presupuesto;
    private Zona[] zonasInteres;
    private TipoInmueble tipoInmuebleDeseado;
    private int habitacionesMinimas;
    private EstadoBusqueda estadoBusqueda;

    @JsonIgnore
    private SimpleLinkedList<Inmueble> inmueblesConsultados  = new SimpleLinkedList<>();
    @JsonIgnore
    private SimpleLinkedList<Inmueble> propiedadesVisitadas  = new SimpleLinkedList<>();
    @JsonIgnore
    private SimpleLinkedList<Inmueble> inmueblesDescartados  = new SimpleLinkedList<>();
    @JsonIgnore
    private SimpleLinkedList<Inmueble> inmueblesGuardados    = new SimpleLinkedList<>();
    @JsonIgnore
    private SimpleLinkedList<Inmueble> inmueblesNegociados   = new SimpleLinkedList<>();
    @JsonIgnore
    private SimpleLinkedList<Recomendacion> listaRecomendaciones = new SimpleLinkedList<>();

    public Cliente(String id, String nombre, String correo, String telefono,
                   String tipoCliente, double presupuesto, Zona[] zonasInteres,
                   TipoInmueble tipoInmuebleDeseado, int habitacionesMinimas,
                   EstadoBusqueda estadoBusqueda) {
        super(id, nombre);
        this.correo = correo;
        this.telefono = telefono;
        this.tipoCliente = tipoCliente;
        this.presupuesto = presupuesto;
        this.zonasInteres = zonasInteres;
        this.tipoInmuebleDeseado = tipoInmuebleDeseado;
        this.habitacionesMinimas = habitacionesMinimas;
        this.estadoBusqueda = estadoBusqueda;
    }
}