package cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto;

import java.math.BigDecimal;

public class EnvioResponseDTO {

    private final Integer id;
    private final String codigoRastreo;
    private final String direccionDestino;
    private final BigDecimal pesoKg;
    private final BigDecimal costo;
    private final String estadoEnvio;
    private final String placaVehiculo;
    private final String nombreConductor;

    public EnvioResponseDTO(Integer id, String codigoRastreo, String direccionDestino,
            BigDecimal pesoKg, BigDecimal costo, String estadoEnvio,
            String placaVehiculo, String nombreConductor) {
        this.id = id;
        this.codigoRastreo = codigoRastreo;
        this.direccionDestino = direccionDestino;
        this.pesoKg = pesoKg;
        this.costo = costo;
        this.estadoEnvio = estadoEnvio;
        this.placaVehiculo = placaVehiculo;
        this.nombreConductor = nombreConductor;
    }

    public Integer getId() {
        return id;
    }

    public String getCodigoRastreo() {
        return codigoRastreo;
    }

    public String getDireccionDestino() {
        return direccionDestino;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public String getEstadoEnvio() {
        return estadoEnvio;
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public String getNombreConductor() {
        return nombreConductor;
    }
}
