package co.edu.uniquindio.poo.PropTech.exception;

/**
 * Excepción base del dominio PropTech.
 * Todas las excepciones propias heredan de esta clase,
 * lo que permite capturarlas de forma genérica en el GlobalExceptionHandler.
 */
public class PropTechException extends RuntimeException {

    public PropTechException(String message) {
        super(message);
    }

    public PropTechException(String message, Throwable cause) {
        super(message, cause);
    }
}
