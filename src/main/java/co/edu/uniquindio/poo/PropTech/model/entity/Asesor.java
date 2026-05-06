package co.edu.uniquindio.poo.PropTech.model.entity;

import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Asesor extends Persona {

    private String contacto;
    private String especialidadZona;

    private SimpleLinkedList<Inmueble> inmueblesAsignados   = new SimpleLinkedList<>();
    private SimpleLinkedList<Visita>   visitasAgendadas     = new SimpleLinkedList<>();
    private SimpleLinkedList<Operacion> cierresRealizados   = new SimpleLinkedList<>();

    public Asesor(String id, String nombre, String contacto, String especialidadZona) {
        super(id, nombre);
        this.contacto = contacto;
        this.especialidadZona = especialidadZona;
    }
}