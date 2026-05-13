package co.edu.uniquindio.poo.PropTech.model.entity;

import co.edu.uniquindio.poo.PropTech.model.enums.FinalidadInmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInmueble;
import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inmueble implements Comparable<Inmueble> {

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

    @JsonIgnoreProperties({"inmueblesAsignados","visitasAgendadas","cierresRealizados"})
    private Asesor asesor;

    @JsonIgnore
    private SimpleLinkedList<Visita> listaVisitas = new SimpleLinkedList<>();

    // Para ordenar en el AVL por precio
    @Override
    public int compareTo(Inmueble otro) {
        return Double.compare(this.precio, otro.precio);
    }
}