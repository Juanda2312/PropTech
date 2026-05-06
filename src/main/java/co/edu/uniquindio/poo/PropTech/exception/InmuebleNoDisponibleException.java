package co.edu.uniquindio.poo.PropTech.exception;

/**
 * Se lanza cuando se intenta operar sobre un inmueble que no está disponible.
 * Casos de uso:
 *  - Agendar una visita sobre un inmueble no disponible.
 *  - Registrar una venta o arriendo sobre un inmueble ya cerrado.
 * El GlobalExceptionHandler la mapea a HTTP 409 Conflict.
 */
public class InmuebleNoDisponibleException extends PropTechException {

    private final String codigoInmueble;

    public InmuebleNoDisponibleException(String codigoInmueble) {
        super("El inmueble con código " + codigoInmueble + " no está disponible para esta operación.");
        this.codigoInmueble = codigoInmueble;
    }

    public String getCodigoInmueble() {
        return codigoInmueble;
    }
}
