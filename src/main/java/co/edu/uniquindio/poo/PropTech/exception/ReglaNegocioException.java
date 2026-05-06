package co.edu.uniquindio.poo.PropTech.exception;

/**
 * Se lanza cuando se viola una regla de negocio que no encaja
 * en ninguna otra excepción más específica.
 * Casos de uso:
 *  - Intentar deshacer cambios cuando no hay snapshots en el historial.
 *  - Procesar la cola de visitas o alertas pendientes cuando está vacía.
 *  - Registrar una operación con un valor acordado negativo o cero.
 * El GlobalExceptionHandler la mapea a HTTP 400 Bad Request.
 */
public class ReglaNegocioException extends PropTechException {

    public ReglaNegocioException(String message) {
        super(message);
    }
}
