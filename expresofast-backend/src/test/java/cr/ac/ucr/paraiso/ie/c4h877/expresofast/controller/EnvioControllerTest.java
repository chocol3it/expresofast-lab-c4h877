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
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.CrearEnvioDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.PaqueteDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.GlobalExceptionHandler;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

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

        mockMvc.perform(get("/api/v1/envios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"));
    }

    @Test
    void obtenerEnvio_Inexistente_Retorna404() throws Exception {
        when(envioService.obtenerEnvio(99))
                .thenThrow(new ResourceNotFoundException("Envío no encontrado"));

        mockMvc.perform(get("/api/v1/envios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso No Encontrado"));
    }

    @Test
    void registrarEnvio_PayloadInvalido_Retorna400() throws Exception {
        String payload = """
                {
                  "destinatario": "",
                  "direccionDestino": "",
                  "montoFlete": null
                }
                """;

        mockMvc.perform(post("/api/v1/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.invalidFields").exists());
    }

    @Test
    void registrarEnvio_Valido_Retorna201() throws Exception {
        EnvioDTO respuesta = new EnvioDTO(
                1, "EXP-2026-1234", "Carlos Mora", "Paraiso, Cartago",
                new BigDecimal("3500.00"), "PENDIENTE", LocalDateTime.now(),
                List.of(new PaqueteDTO(1, "Caja de libros", new BigDecimal("2.50"))));

        when(envioService.crearEnvioSimple(any(CrearEnvioDTO.class))).thenReturn(respuesta);

        mockMvc.perform(post("/api/v1/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoRastreo":"EXP-2026-1234",
                                  "destinatario":"Carlos Mora",
                                  "direccionDestino":"Paraiso, Cartago",
                                  "montoFlete":3500.00,
                                  "paquetes":[{"descripcion":"Caja de libros","pesoKg":2.50}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoRastreo").value("EXP-2026-1234"))
                .andExpect(jsonPath("$.paquetes[0].descripcion").value("Caja de libros"));
    }

    @Test
    void registrarEnvio_SinPaquetes_Retorna400() throws Exception {
        mockMvc.perform(post("/api/v1/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoRastreo":"EXP-2026-1234",
                                  "destinatario":"Carlos Mora",
                                  "direccionDestino":"Paraiso, Cartago",
                                  "montoFlete":3500.00,
                                  "paquetes":[]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalidFields.paquetes").exists());
    }

    @Test
    void checkTracking_CodigoExistente_RetornaTrue() throws Exception {
        when(envioService.existeCodigoRastreo("EXP-2026-1234")).thenReturn(true);

        mockMvc.perform(get("/api/v1/envios/check-tracking/EXP-2026-1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void checkTracking_CodigoLibre_RetornaFalse() throws Exception {
        when(envioService.existeCodigoRastreo("EXP-2026-9999")).thenReturn(false);

        mockMvc.perform(get("/api/v1/envios/check-tracking/EXP-2026-9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    void buscarPorCodigoRastreo_Existente_Retorna200() throws Exception {
        EnvioDTO respuesta = new EnvioDTO(
                1, "EXP-2026-1234", "Carlos Mora", "Paraiso, Cartago",
                new BigDecimal("3500.00"), "PENDIENTE", LocalDateTime.now(), List.of());

        when(envioService.buscarPorCodigoRastreo("EXP-2026-1234")).thenReturn(respuesta);

        mockMvc.perform(get("/api/v1/envios/rastreo/EXP-2026-1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinatario").value("Carlos Mora"));
    }

    @Test
    void buscarPorCodigoRastreo_Inexistente_Retorna404() throws Exception {
        when(envioService.buscarPorCodigoRastreo("EXP-2026-0000"))
                .thenThrow(new ResourceNotFoundException("Envío no encontrado"));

        mockMvc.perform(get("/api/v1/envios/rastreo/EXP-2026-0000"))
                .andExpect(status().isNotFound());
    }
}
