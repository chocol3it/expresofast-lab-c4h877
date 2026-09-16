package cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CambioEstadoDTO {

    @NotBlank(message = "El nuevo estado es obligatorio")
    private String nuevoEstado;

    @Size(max = 250, message = "Las observaciones no pueden superar 250 caracteres")
    private String observaciones;

    public String getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(String nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
