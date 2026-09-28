package cr.ac.ucr.paraiso.ie.c4h877.expresofast.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.GlobalExceptionHandler;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.ResourceNotFoundException;

@WebMvcTest(EnvioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class EnvioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvioService envioService;

    @Test
    void obtenerEnvio_Existente_Retorna200YDatos() throws Exception {
        EnvioResponseDTO respuesta = new EnvioResponseDTO(
                1, "EXP-1234", "Paraiso, Cartago", new BigDecimal("5.00"),
                new BigDecimal("3500.00"), "PENDIENTE", "102938", "Carlos Mora V.");

        when(envioService.obtenerEnvio(1)).thenReturn(respuesta);

        mockMvc.perform(get("/api/envios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"));
    }

    @Test
    void obtenerEnvio_Inexistente_Retorna404() throws Exception {
        when(envioService.obtenerEnvio(99))
                .thenThrow(new ResourceNotFoundException("Envío no encontrado"));

        mockMvc.perform(get("/api/envios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso No Encontrado"));
    }

    @Test
    void registrarEnvio_PayloadInvalido_Retorna400() throws Exception {
        String payload = """
                {
                  "codigoRastreo": "",
                  "direccionDestino": "",
                  "pesoKg": 0,
                  "costo": null,
                  "vehiculoId": null,
                  "conductorId": null
                }
                """;

        mockMvc.perform(post("/api/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.invalidFields").exists());
    }

    @Test
    void registrarEnvio_Valido_Retorna201() throws Exception {
        EnvioResponseDTO respuesta = new EnvioResponseDTO(
                1, "EXP-1234", "Paraiso, Cartago", new BigDecimal("5.00"),
                new BigDecimal("3500.00"), "PENDIENTE", "102938", "Carlos Mora V.");

        when(envioService.crearEnvio(any(EnvioRequestDTO.class))).thenReturn(respuesta);

        mockMvc.perform(post("/api/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoRastreo":"EXP-1234",
                                  "direccionDestino":"Paraiso, Cartago",
                                  "pesoKg":5.00,
                                  "costo":3500.00,
                                  "vehiculoId":1,
                                  "conductorId":1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"));
    }
}
