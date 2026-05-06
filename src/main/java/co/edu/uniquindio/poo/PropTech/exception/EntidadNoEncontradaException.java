package co.edu.uniquindio.poo.PropTech.exception;

/**
 * Se lanza cuando una entidad no se encuentra en el repositorio.
 * Aplica para: Inmueble, Cliente, Asesor, Visita, Operacion, Alerta, EventoInusual.
 * El GlobalExceptionHandler la mapea a HTTP 404.
 */
public class EntidadNoEncontradaException extends PropTechException {

    private final String entidad;
    private final String id;

    public EntidadNoEncontradaException(String entidad, String id) {
        super(entidad + " no encontrado/a con id: " + id);
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
