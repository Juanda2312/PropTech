package co.edu.uniquindio.poo.PropTech.exception;

import co.edu.uniquindio.poo.PropTech.util.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Intercepta todas las excepciones lanzadas por los controladores
 * y las transforma en respuestas HTTP con el código y cuerpo adecuados.
 *
 * Jerarquía de manejo (de más específico a más general):
 *  EntidadNoEncontradaException  → 404 Not Found
 *  EntidadDuplicadaException     → 409 Conflict
 *  InmuebleNoDisponibleException → 409 Conflict
 *  EstadoInvalidoException       → 422 Unprocessable Entity
 *  ReglaNegocioException         → 400 Bad Request
 *  PropTechException             → 400 Bad Request  (fallback del dominio)
 *  Exception                     → 500 Internal Server Error (fallback global)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ----------------------------------------------------------------
    // 404 — Entidad no encontrada
    // ----------------------------------------------------------------

    @ExceptionHandler(EntidadNoEncontradaException.class)
    public ResponseEntity<ApiError> handleNotFound(EntidadNoEncontradaException ex,
                                                   HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // ----------------------------------------------------------------
    // 409 — Conflicto: duplicado o inmueble no disponible
    // ----------------------------------------------------------------

    @ExceptionHandler(EntidadDuplicadaException.class)
    public ResponseEntity<ApiError> handleDuplicate(EntidadDuplicadaException ex,
                                                    HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InmuebleNoDisponibleException.class)
    public ResponseEntity<ApiError> handleInmuebleNoDisponible(InmuebleNoDisponibleException ex,
                                                               HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ----------------------------------------------------------------
    // 422 — Transición de estado inválida
    // ----------------------------------------------------------------

    @ExceptionHandler(EstadoInvalidoException.class)
    public ResponseEntity<ApiError> handleEstadoInvalido(EstadoInvalidoException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    // ----------------------------------------------------------------
    // 400 — Regla de negocio violada / fallback del dominio
    // ----------------------------------------------------------------

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> handleReglaNegocio(ReglaNegocioException ex,
                                                       HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(PropTechException.class)
    public ResponseEntity<ApiError> handlePropTech(PropTechException ex,
                                                   HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // ----------------------------------------------------------------
    // 500 — Fallback global
    // ----------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex,
                                                  HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor. Contacte al administrador.", request);
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------

    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           HttpServletRequest request) {
        ApiError error = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}
