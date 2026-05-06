package co.edu.uniquindio.poo.PropTech.exception;

/**
 * Se lanza cuando se intenta realizar una transición de estado inválida.
 * Casos de uso:
 *  - Confirmar una visita que ya fue cancelada.
 *  - Cancelar una operación que ya está cerrada.
 *  - Cerrar un evento inusual que ya estaba cerrado.
 * El GlobalExceptionHandler la mapea a HTTP 422 Unprocessable Entity.
 */
public class EstadoInvalidoException extends PropTechException {

    private final String entidad;
    private final String estadoActual;
    private final String transicionIntentada;

    public EstadoInvalidoException(String entidad, String estadoActual, String transicionIntentada) {
        super("No se puede realizar '" + transicionIntentada + "' sobre "
                + entidad + " en estado: " + estadoActual);
        this.entidad = entidad;
        this.estadoActual = estadoActual;
        this.transicionIntentada = transicionIntentada;
    }

    public String getEntidad() {
        return entidad;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public String getTransicionIntentada() {
        return transicionIntentada;
    }
}
