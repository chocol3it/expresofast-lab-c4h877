package cr.ac.ucr.paraiso.ie.c4h877.expresofast.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.BitacoraEnvioRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.BitacoraEnvio;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.CrearEnvioDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.PaqueteDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private ConductorRepository conductorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BitacoraEnvioRepository bitacoraEnvioRepository;

    @InjectMocks
    private EnvioService envioService;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void crearEnvio_DatosValidos_RetornaEnvioDTO() {
        EnvioRequestDTO solicitud = new EnvioRequestDTO();
        solicitud.setCodigoRastreo("EXP-1234");
        solicitud.setDireccionDestino("Paraiso, Cartago");
        solicitud.setPesoKg(new BigDecimal("5.00"));
        solicitud.setCosto(new BigDecimal("3500.00"));
        solicitud.setVehiculoId(1);
        solicitud.setConductorId(1);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setPlaca("102938");
        vehiculo.setCapacidadKg(new BigDecimal("500.00"));

        Conductor conductor = new Conductor();
        conductor.setId(1);
        conductor.setNombre("Carlos");
        conductor.setApellidos("Mora V.");

        when(vehiculoRepository.findById(1))
                .thenReturn(Optional.of(vehiculo));

        when(conductorRepository.findById(1))
                .thenReturn(Optional.of(conductor));

        when(envioRepository.save(any(Envio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EnvioResponseDTO resultado = envioService.crearEnvio(solicitud);

        ArgumentCaptor<Envio> captor = ArgumentCaptor.forClass(Envio.class);

        verify(envioRepository).save(captor.capture());

        Envio envioGuardado = captor.getValue();

        assertEquals("PENDIENTE", envioGuardado.getEstadoEnvio());

        assertEquals("EXP-1234", envioGuardado.getCodigoRastreo());
        assertEquals(vehiculo, envioGuardado.getVehiculo());
        assertEquals(conductor, envioGuardado.getConductor());


        assertEquals("EXP-1234", resultado.getCodigoRastreo());
        assertEquals("102938", resultado.getPlacaVehiculo());
        assertEquals("Carlos Mora V.", resultado.getNombreConductor());

    }

    @Test
    void crearEnvio_VehiculoSinCapacidad_LanzaExcepcion() {
        EnvioRequestDTO solicitud = new EnvioRequestDTO();
        solicitud.setCodigoRastreo("EXP-1234");
        solicitud.setDireccionDestino("Paraiso, Cartago");
        solicitud.setPesoKg(new BigDecimal("11.00"));
        solicitud.setCosto(new BigDecimal("3500.00"));
        solicitud.setVehiculoId(1);
        solicitud.setConductorId(1);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setCapacidadKg(new BigDecimal("10.00"));
        Conductor conductor = new Conductor();

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));

        assertThrows(IllegalArgumentException.class,
                () -> envioService.crearEnvio(solicitud));

        verify(envioRepository, never()).save(org.mockito.ArgumentMatchers.any(Envio.class));
    }

    @Test
    void actualizarEstado_TransicionInvalida_LanzaExcepcion() {
        Envio envio = new Envio(
                1,
                "EXP-1234",
                "Paraiso, Cartago",
                new BigDecimal("5.00"),
                new BigDecimal("3500.00"),
                "ENTREGADO",
                null,
                null);

        CambioEstadoDTO cambioEstado = new CambioEstadoDTO();
        cambioEstado.setNuevoEstado("EN_TRANSITO");

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));

        assertThrows(InvalidStateTransitionException.class,
                () -> envioService.actualizarEstado(1, cambioEstado));

        assertEquals("ENTREGADO", envio.getEstadoEnvio());
        verify(bitacoraEnvioRepository, never()).save(any());
        verify(usuarioRepository, never()).findByUsername(any());
    }

    @Test
    void cancelarEnvio_EnvioEnTransito_LanzaExcepcion() {

        Envio envio = new Envio(
                1,
                "ABC123",
                "Paraiso, Cartago",
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                "EN_TRANSITO",
                null,
                null);

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));

        assertThrows(InvalidStateTransitionException.class,
                () -> envioService.cancelarEnvio(1));

    }


    //Ignorar, pruebas de practica
    @Test
    void testCancelarEnvioEnvioPendienteCambiaEstadoACancelado() {
        Envio envio = new Envio(
                1,
                "ABC123",
                "Paraiso, Cartago",
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                "PENDIENTE",
                null,
                null);
        Usuario usuario = new Usuario();
        Authentication authentication = mock(Authentication.class);

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));
        when(authentication.getName()).thenReturn("operador1");
        when(usuarioRepository.findByUsername("operador1")).thenReturn(Optional.of(usuario));
        when(envioRepository.save(envio)).thenReturn(envio);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Envio resultado = envioService.cancelarEnvio(1);

        assertEquals("CANCELADO", resultado.getEstadoEnvio());
        verify(bitacoraEnvioRepository).save(org.mockito.ArgumentMatchers.any());
        verify(envioRepository).save(envio);
    }

    
    @Test 
    void cancelarEnvio_EnvioPendiente_RegistraBitacoraCorrectamente(){
        
        Envio envio = new Envio(
                1,
                "EXP-1234",
                "Paraiso, Cartago",
                new BigDecimal("5.00"),
                new BigDecimal("3500.00"),
                "PENDIENTE",
                null,
                null);

        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("operador1");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("operador1");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));
        when(usuarioRepository.findByUsername("operador1")).thenReturn(Optional.of(usuario));
        when(envioRepository.save(envio)).thenReturn(envio);

        envioService.cancelarEnvio(1);

        ArgumentCaptor<BitacoraEnvio> captor = ArgumentCaptor.forClass(BitacoraEnvio.class);
        verify(bitacoraEnvioRepository).save(captor.capture());

        BitacoraEnvio bitacoraGuardada = captor.getValue();

        assertEquals(envio, bitacoraGuardada.getEnvio());
        assertEquals("CANCELADO", bitacoraGuardada.getEstadoNuevo());
        assertEquals("PENDIENTE", bitacoraGuardada.getEstadoAnterior());
        assertEquals(usuario, bitacoraGuardada.getUsuario());
    }

    @ParameterizedTest
    @CsvSource({
            "5.0, 10.0, 2500.0",
            "15.0, 50.0, 7500.0",
            "100.0, 2.5, 12000.0"
    })
    @DisplayName("Calcula la tarifa base y aplica recargo para cargas pesadas")
    void calcularTarifa_CasosVariados_CalculaCorrectamente(
            double pesoKg, double distanciaKm, double tarifaEsperada) {
        double tarifaCalculada = envioService.calcularTarifa(pesoKg, distanciaKm);

        assertEquals(tarifaEsperada, tarifaCalculada, 0.01);
    }

    @Test
    void obtenerEnvio_Existente_RetornaDTO() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("102938");
        Conductor conductor = new Conductor();
        conductor.setNombre("Carlos");
        conductor.setApellidos("Mora V.");

        Envio envio = new Envio(1, "EXP-1234", "Paraiso, Cartago",
                new BigDecimal("5.00"), new BigDecimal("3500.00"), "PENDIENTE", vehiculo, conductor);

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));

        EnvioResponseDTO resultado = envioService.obtenerEnvio(1);

        assertEquals("EXP-1234", resultado.getCodigoRastreo());
        assertEquals("102938", resultado.getPlacaVehiculo());
        assertEquals("Carlos Mora V.", resultado.getNombreConductor());
    }

    @Test
    void obtenerEnvio_Inexistente_LanzaExcepcion() {
        when(envioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.obtenerEnvio(99));
    }

    @Test
    void crearEnvio_VehiculoInexistente_LanzaExcepcion() {
        EnvioRequestDTO solicitud = new EnvioRequestDTO();
        solicitud.setVehiculoId(1);
        solicitud.setConductorId(1);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.crearEnvio(solicitud));

        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void crearEnvio_ConductorInexistente_LanzaExcepcion() {
        EnvioRequestDTO solicitud = new EnvioRequestDTO();
        solicitud.setVehiculoId(1);
        solicitud.setConductorId(1);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(new Vehiculo()));
        when(conductorRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.crearEnvio(solicitud));

        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void obtenerBitacora_EnvioExistente_RetornaLista() {
        Usuario usuario = new Usuario();
        usuario.setUsername("operador1");

        BitacoraEnvio bitacora = new BitacoraEnvio();
        bitacora.setId(1);
        bitacora.setEstadoAnterior("PENDIENTE");
        bitacora.setEstadoNuevo("CANCELADO");
        bitacora.setUsuario(usuario);

        when(envioRepository.existsById(1)).thenReturn(true);
        when(bitacoraEnvioRepository.findByEnvio_IdOrderByFechaCambioDesc(1))
                .thenReturn(List.of(bitacora));

        List<BitacoraResponseDTO> resultado = envioService.obtenerBitacora(1);

        assertEquals(1, resultado.size());
        assertEquals("CANCELADO", resultado.get(0).getEstadoNuevo());
        assertEquals("operador1", resultado.get(0).getUsuario());
    }

    @Test
    void obtenerBitacora_EnvioInexistente_LanzaExcepcion() {
        when(envioRepository.existsById(99)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.obtenerBitacora(99));
    }

    @Test
    void actualizarEstadoPorVehiculo_VehiculoIdNulo_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> envioService.actualizarEstadoPorVehiculo(null, "EN_TRANSITO"));
    }

    @Test
    void actualizarEstadoPorVehiculo_EstadoVacio_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> envioService.actualizarEstadoPorVehiculo(1, "  "));
    }

    @Test
    void actualizarEstadoPorVehiculo_DatosValidos_RetornaFilasActualizadas() {
        when(envioRepository.updateEstadoByVehiculoId("EN_TRANSITO", 1)).thenReturn(3);

        int filas = envioService.actualizarEstadoPorVehiculo(1, "EN_TRANSITO");

        assertEquals(3, filas);
    }

    @Test
    void cancelarEnvio_EnvioInexistente_LanzaExcepcion() {
        when(envioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.cancelarEnvio(99));
    }

    @Test
    void getOptimizedShipments_RetornaListaDTO() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("102938");
        Conductor conductor = new Conductor();
        conductor.setNombre("Carlos");
        conductor.setApellidos("Mora V.");

        Envio envio = new Envio(1, "EXP-1234", "Paraiso, Cartago",
                new BigDecimal("5.00"), new BigDecimal("3500.00"), "PENDIENTE", vehiculo, conductor);

        when(envioRepository.findAllOptimized()).thenReturn(List.of(envio));

        List<EnvioResponseDTO> resultado = envioService.getOptimizedShipments();

        assertEquals(1, resultado.size());
        assertEquals("EXP-1234", resultado.get(0).getCodigoRastreo());
    }

    @Test
    void listarPaginado_SinFiltros_RetornaPageDeDTO() {
        Envio envio = new Envio(1, "EXP-9001", "Paraiso, Cartago",
                new BigDecimal("5.00"), new BigDecimal("4500.00"), "PENDIENTE", null, null);
        envio.setDestinatario("Maria Jimenez");

        PageRequest solicitud = PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "fechaCreacion"));
        Page<Envio> pagina = new PageImpl<>(List.of(envio), solicitud, 1);
        when(envioRepository.buscarPaginado(null, null, solicitud))
                .thenReturn(pagina);

        Page<EnvioDTO> resultado = envioService.listarPaginado(0, 5, "fechaCreacion", "desc", null, null);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("EXP-9001", resultado.getContent().get(0).codigoRastreo());
        assertEquals("Maria Jimenez", resultado.getContent().get(0).destinatario());
    }

    @Test
    void listarPaginado_ConBusquedaYEstado_FiltraEnElRepositorio() {
        when(envioRepository.buscarPaginado(eq("PENDIENTE"), eq("maria"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        envioService.listarPaginado(0, 5, "costo", "asc", "  maria  ", " PENDIENTE ");

        verify(envioRepository).buscarPaginado(eq("PENDIENTE"), eq("maria"), any());
    }

    @Test
    void listarViaStoredProcedure_RetornaListaDeDTO() {
        Envio envio = new Envio(1, "EXP-9002", "Orosi, Cartago",
                new BigDecimal("8.00"), new BigDecimal("3200.00"), "ENTREGADO", null, null);
        envio.setDestinatario("Jose Fernandez");

        when(envioRepository.obtenerEnviosPorEstado("ENTREGADO")).thenReturn(List.of(envio));

        List<EnvioDTO> resultado = envioService.listarViaStoredProcedure("ENTREGADO");

        assertEquals(1, resultado.size());
        assertEquals("ENTREGADO", resultado.get(0).estado());
        assertEquals("Jose Fernandez", resultado.get(0).destinatario());
    }

    @Test
    void crearEnvioSimple_DatosValidos_GuardaEnvioConPaquetes() {
        CrearEnvioDTO solicitud = new CrearEnvioDTO();
        solicitud.setCodigoRastreo("EXP-2026-1234");
        solicitud.setDestinatario("Carlos Mora");
        solicitud.setDireccionDestino("Paraiso, Cartago");
        solicitud.setMontoFlete(new BigDecimal("3500.00"));
        solicitud.setPaquetes(List.of(
                new PaqueteDTO(null, "Caja de libros", new BigDecimal("2.50")),
                new PaqueteDTO(null, "Caja de ropa", new BigDecimal("1.20"))));

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("102938");
        Conductor conductor = new Conductor();

        when(envioRepository.findByCodigoRastreo("EXP-2026-1234")).thenReturn(Optional.empty());
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculo));
        when(conductorRepository.findAll()).thenReturn(List.of(conductor));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EnvioDTO resultado = envioService.crearEnvioSimple(solicitud);

        ArgumentCaptor<Envio> captor = ArgumentCaptor.forClass(Envio.class);
        verify(envioRepository).save(captor.capture());

        Envio envioGuardado = captor.getValue();
        assertEquals("EXP-2026-1234", envioGuardado.getCodigoRastreo());
        assertEquals(2, envioGuardado.getPaquetes().size());
        assertEquals(envioGuardado, envioGuardado.getPaquetes().get(0).getEnvio());
        assertEquals(new BigDecimal("3.70"), envioGuardado.getPesoKg());

        assertEquals("EXP-2026-1234", resultado.codigoRastreo());
        assertEquals(2, resultado.paquetes().size());
    }

    @Test
    void crearEnvioSimple_CodigoRastreoDuplicado_LanzaExcepcion() {
        CrearEnvioDTO solicitud = new CrearEnvioDTO();
        solicitud.setCodigoRastreo("EXP-2026-1234");
        solicitud.setPaquetes(List.of(new PaqueteDTO(null, "Caja", new BigDecimal("1.00"))));

        when(envioRepository.findByCodigoRastreo("EXP-2026-1234"))
                .thenReturn(Optional.of(new Envio()));

        assertThrows(DuplicateResourceException.class,
                () -> envioService.crearEnvioSimple(solicitud));

        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void existeCodigoRastreo_CodigoExistente_RetornaTrue() {
        when(envioRepository.findByCodigoRastreo("EXP-2026-1234"))
                .thenReturn(Optional.of(new Envio()));

        assertEquals(true, envioService.existeCodigoRastreo("EXP-2026-1234"));
    }

    @Test
    void existeCodigoRastreo_CodigoLibre_RetornaFalse() {
        when(envioRepository.findByCodigoRastreo("EXP-2026-9999")).thenReturn(Optional.empty());

        assertEquals(false, envioService.existeCodigoRastreo("EXP-2026-9999"));
    }

    @Test
    void actualizarEstado_TransicionValida_RegistraBitacoraYRetornaDTO() {
        Envio envio = new Envio(1, "EXP-1234", "Paraiso, Cartago",
                new BigDecimal("5.00"), new BigDecimal("3500.00"), "PENDIENTE", null, null);

        CambioEstadoDTO cambioEstado = new CambioEstadoDTO();
        cambioEstado.setNuevoEstado("EN_TRANSITO");
        cambioEstado.setObservaciones("Sale de bodega");

        Usuario usuario = new Usuario();
        usuario.setUsername("operador1");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("operador1");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));
        when(usuarioRepository.findByUsername("operador1")).thenReturn(Optional.of(usuario));

        EnvioResponseDTO resultado = envioService.actualizarEstado(1, cambioEstado);

        assertEquals("EN_TRANSITO", resultado.getEstadoEnvio());
        verify(bitacoraEnvioRepository).save(any());
    }

    @Test
    void buscarPorCodigoRastreo_Existente_RetornaDTO() {
        Envio envio = new Envio(1, "EXP-2026-1234", "Paraiso, Cartago",
                new BigDecimal("5.00"), new BigDecimal("3500.00"), "PENDIENTE", null, null);
        envio.setDestinatario("Carlos Mora");

        when(envioRepository.findByCodigoRastreo("EXP-2026-1234")).thenReturn(Optional.of(envio));

        EnvioDTO resultado = envioService.buscarPorCodigoRastreo("EXP-2026-1234");

        assertEquals("Carlos Mora", resultado.destinatario());
    }

    @Test
    void buscarPorCodigoRastreo_Inexistente_LanzaExcepcion() {
        when(envioRepository.findByCodigoRastreo("EXP-0000")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.buscarPorCodigoRastreo("EXP-0000"));
    }

    @Test
    void actualizarEstadoSimple_EstadoValido_ActualizaYRetornaDTO() {
        Envio envio = new Envio(1, "EXP-2026-1234", "Paraiso, Cartago",
                new BigDecimal("5.00"), new BigDecimal("3500.00"), "PENDIENTE", null, null);

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));
        when(envioRepository.save(envio)).thenReturn(envio);

        EnvioDTO resultado = envioService.actualizarEstadoSimple(1, "EN_TRANSITO");

        assertEquals("EN_TRANSITO", resultado.estado());
    }

    @Test
    void actualizarEstadoSimple_EstadoInvalido_LanzaExcepcion() {
        Envio envio = new Envio(1, "EXP-2026-1234", "Paraiso, Cartago",
                new BigDecimal("5.00"), new BigDecimal("3500.00"), "PENDIENTE", null, null);

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));

        assertThrows(IllegalArgumentException.class,
                () -> envioService.actualizarEstadoSimple(1, "DESCONOCIDO"));

        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void actualizarEstadoSimple_EnvioInexistente_LanzaExcepcion() {
        when(envioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.actualizarEstadoSimple(99, "EN_TRANSITO"));
    }
}
