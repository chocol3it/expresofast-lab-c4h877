package cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class CrearEnvioDTO {

    @NotBlank(message = "El destinatario es obligatorio")
    private String destinatario;

    @NotBlank(message = "La dirección de destino es obligatoria")
    private String direccionDestino;

    @NotNull(message = "El monto de flete es obligatorio")
    @Positive(message = "El monto de flete debe ser mayor a cero")
    private BigDecimal montoFlete;

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getDireccionDestino() {
        return direccionDestino;
    }

    public void setDireccionDestino(String direccionDestino) {
        this.direccionDestino = direccionDestino;
    }

    public BigDecimal getMontoFlete() {
        return montoFlete;
    }

    public void setMontoFlete(BigDecimal montoFlete) {
        this.montoFlete = montoFlete;
    }
}
