package co.edu.uniquindio.poo.PropTech.model.entity;

import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class Asesor extends Persona {

    private String contacto;
    private String especialidadZona;

    @JsonIgnore
    private SimpleLinkedList<Inmueble> inmueblesAsignados   = new SimpleLinkedList<>();
    @JsonIgnore
    private SimpleLinkedList<Visita>   visitasAgendadas     = new SimpleLinkedList<>();
    @JsonIgnore
    private SimpleLinkedList<Operacion> cierresRealizados   = new SimpleLinkedList<>();

    public Asesor(String id, String nombre, String contacto, String especialidadZona) {
        super(id, nombre);
        this.contacto = contacto;
        this.especialidadZona = especialidadZona;
    }
}