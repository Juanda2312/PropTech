package co.edu.uniquindio.poo.PropTech.util;

import java.time.LocalDateTime;

/**
 * Estructura estandarizada de respuesta de error para la API REST.
 * Es lo que el cliente recibe en el cuerpo cuando ocurre una excepción.
 *
 * Ejemplo de respuesta JSON:
 * {
 *   "timestamp": "2025-05-06T14:32:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Inmueble no encontrado/a con id: INM-001",
 *   "path": "/api/inmuebles/INM-001"
 * }
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    /**
     * Factory method para construir un ApiError de forma concisa desde el handler.
     */
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, error, message, path);
    }
}
