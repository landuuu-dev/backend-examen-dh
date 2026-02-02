package dh.tour.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Atrapa nuestra excepción personalizada (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessage> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    // Atrapa cualquier otro error inesperado (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> globalExceptionHandler(Exception ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now(),
                "Ocurrió un error inesperado: " + ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Este método atrapa los errores de @Valid (cuando los campos del DTO no cumplen las reglas)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        // Tomamos todos los errores de validación y los unimos en un solo String
        StringBuilder detalles = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                detalles.append(error.getField()).append(": ").append(error.getDefaultMessage()).append(". ")
        );

        ErrorMessage message = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                "Error de validación en los campos",
                detalles.toString() // Aquí el usuario verá qué campo falló exactamente
        );

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    // En GlobalExceptionHandler.java añade este método:

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ErrorMessage> handleOperationNotAllowed(OperationNotAllowedException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    // Agrega este import

    // Y este método en tu GlobalExceptionHandler
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessage> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                LocalDateTime.now(),
                "No tienes permisos para realizar esta acción o acceder a este recurso.",
                request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> handleInvalidEnum(HttpMessageNotReadableException ex, WebRequest request) {
        String mensaje = "Valor inválido en uno de los campos. ";

        // Intentamos extraer qué valores son los permitidos para ayudar al usuario
        if (ex.getMessage() != null && ex.getMessage().contains("EstadoTour")) {
            mensaje = "Estado inválido. Los valores permitidos son: ACTIVO, AGOTADO, CANCELADO.";
        }

        ErrorMessage message = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                mensaje,
                request.getDescription(false)
        );
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }
}