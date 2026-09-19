package cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorResponseDTO error = new ErrorResponseDTO("Error de Validación",
                HttpStatus.BAD_REQUEST.value(),
                "Los campos de la solicitud no cumplen las restricciones.",
                request.getRequestURI(), errores);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        return response("Recurso No Encontrado", HttpStatus.NOT_FOUND,
                exception.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateResource(
            DuplicateResourceException exception, HttpServletRequest request) {
        return response("Recurso Duplicado", HttpStatus.CONFLICT,
                exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidStateTransition(
            InvalidStateTransitionException exception, HttpServletRequest request) {
        return response("Transición de Estado Inválida", HttpStatus.BAD_REQUEST,
                exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException exception, HttpServletRequest request) {
        return response("Solicitud Inválida", HttpStatus.BAD_REQUEST,
                exception.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(
            BadCredentialsException exception, HttpServletRequest request) {
        return response("Falla de Autenticación", HttpStatus.UNAUTHORIZED,
                "Nombre de usuario o contraseña incorrectos.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
            AccessDeniedException exception, HttpServletRequest request) {
        return response("Acceso Denegado", HttpStatus.FORBIDDEN,
                "No posee los privilegios suficientes para este recurso.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception exception, HttpServletRequest request) {
        return response("Error Interno del Servidor", HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un fallo inesperado. Contacte al administrador.", request);
    }

    private ResponseEntity<ErrorResponseDTO> response(String title, HttpStatus status,
            String detail, HttpServletRequest request) {
        ErrorResponseDTO error = new ErrorResponseDTO(title, status.value(), detail,
                request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}
