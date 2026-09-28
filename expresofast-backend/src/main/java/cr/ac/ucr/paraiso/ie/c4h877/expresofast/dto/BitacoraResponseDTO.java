package cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto;

import java.time.LocalDateTime;

public class BitacoraResponseDTO {

    private final Integer id;
    private final String estadoAnterior;
    private final String estadoNuevo;
    private final LocalDateTime fechaCambio;
    private final String usuario;
    private final String observaciones;

    public BitacoraResponseDTO(Integer id, String estadoAnterior, String estadoNuevo,
            LocalDateTime fechaCambio, String usuario, String observaciones) {
        this.id = id;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fechaCambio = fechaCambio;
        this.usuario = usuario;
        this.observaciones = observaciones;
    }

    public Integer getId() {
        return id;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
