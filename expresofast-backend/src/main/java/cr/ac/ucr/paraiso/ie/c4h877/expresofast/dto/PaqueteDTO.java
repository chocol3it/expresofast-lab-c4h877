package cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PaqueteDTO(
        Integer id,
        @NotBlank(message = "La descripción del paquete es obligatoria") String descripcion,
        @NotNull(message = "El peso del paquete es obligatorio")
        @Positive(message = "El peso debe ser mayor a cero") BigDecimal pesoKg) {
}
