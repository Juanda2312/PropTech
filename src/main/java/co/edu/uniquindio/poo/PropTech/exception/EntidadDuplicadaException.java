package co.edu.uniquindio.poo.PropTech.exception;

/**
 * Se lanza cuando se intenta registrar una entidad con un ID que ya existe.
 * Aplica para: Inmueble, Cliente, Asesor, Visita, Operacion, Alerta, EventoInusual.
 * El GlobalExceptionHandler la mapea a HTTP 409 Conflict.
 */
public class EntidadDuplicadaException extends PropTechException {

    private final String entidad;
    private final String id;

    public EntidadDuplicadaException(String entidad, String id) {
        super(entidad + " ya existe con id: " + id);
        this.entidad = entidad;
        this.id = id;
    }

    public String getEntidad() {
        return entidad;
    }

    public String getId() {
        return id;
    }
}
