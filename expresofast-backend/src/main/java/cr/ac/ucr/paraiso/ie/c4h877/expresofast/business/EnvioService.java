package cr.ac.ucr.paraiso.ie.c4h877.expresofast.business;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.BitacoraEnvioRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.BitacoraEnvio;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Paquete;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvioService {

    private static final Set<String> ESTADOS_FINALES = Set.of("ENTREGADO", "CANCELADO");
    private static final Set<String> ESTADOS_NO_RETROCEDIBLES = Set.of("PENDIENTE", "EN_TRANSITO");

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;
    private final BitacoraEnvioRepository bitacoraEnvioRepository;

    public EnvioService(EnvioRepository envioRepository, VehiculoRepository vehiculoRepository,
            ConductorRepository conductorRepository, UsuarioRepository usuarioRepository,
            BitacoraEnvioRepository bitacoraEnvioRepository) {
        this.envioRepository = envioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.usuarioRepository = usuarioRepository;
        this.bitacoraEnvioRepository = bitacoraEnvioRepository;
    }

    @Transactional(readOnly = true)
    public List<EnvioResponseDTO> getOptimizedShipments() {
        return envioRepository.findAllOptimized().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EnvioResponseDTO obtenerEnvio(Integer id) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Envío con ID: " + id + " no encontrado"));
        return toResponseDTO(envio);
    }

    @Transactional
    public EnvioResponseDTO crearEnvio(EnvioRequestDTO envioDTO) {

        Vehiculo vehiculo = vehiculoRepository.findById(envioDTO.getVehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));

        Conductor conductor = conductorRepository.findById(envioDTO.getConductorId())
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));

        if (envioDTO.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new IllegalArgumentException("El peso del envío excede la capacidad del vehículo");
        }

        Envio envio = new Envio();
        envio.setCodigoRastreo(envioDTO.getCodigoRastreo());
        envio.setDireccionDestino(envioDTO.getDireccionDestino());
        envio.setPesoKg(envioDTO.getPesoKg());
        envio.setCosto(envioDTO.getCosto());
        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);
        envio.setEstadoEnvio("PENDIENTE");

        return toResponseDTO(envioRepository.save(envio));
    }

    @Transactional
    public EnvioResponseDTO actualizarEstado(Integer id, CambioEstadoDTO cambioDTO) {

        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envío con ID: " + id + " no encontrado"));

        String estadoAnterior = envio.getEstadoEnvio();
        String estadoNuevo = cambioDTO.getNuevoEstado();

        if (ESTADOS_FINALES.contains(estadoAnterior) && ESTADOS_NO_RETROCEDIBLES.contains(estadoNuevo)) {
            throw new InvalidStateTransitionException(
                    "Transición de estado no permitida para el envío [" + envio.getCodigoRastreo() + "]");
        }

        envio.setEstadoEnvio(estadoNuevo);

        Usuario usuarioActuante = usuarioActual();

        BitacoraEnvio bitacora = new BitacoraEnvio();
        bitacora.setEnvio(envio);
        bitacora.setEstadoAnterior(estadoAnterior);
        bitacora.setEstadoNuevo(estadoNuevo);
        bitacora.setFechaCambio(LocalDateTime.now());
        bitacora.setUsuario(usuarioActuante);
        bitacora.setObservaciones(cambioDTO.getObservaciones());
        bitacoraEnvioRepository.save(bitacora);

        return toResponseDTO(envio);
    }

    @Transactional(readOnly = true)
    public List<BitacoraResponseDTO> obtenerBitacora(Integer envioId) {
        if (!envioRepository.existsById(envioId)) {
            throw new ResourceNotFoundException("Envío con ID: " + envioId + " no encontrado");
        }
        return bitacoraEnvioRepository.findByEnvio_IdOrderByFechaCambioDesc(envioId).stream()
                .map(b -> new BitacoraResponseDTO(
                        b.getId(),
                        b.getEstadoAnterior(),
                        b.getEstadoNuevo(),
                        b.getFechaCambio(),
                        b.getUsuario().getUsername(),
                        b.getObservaciones()))
                .toList();
    }

    @Transactional
    public int actualizarEstadoPorVehiculo(Integer vehiculoId, String nuevoEstado) {
        if (vehiculoId == null) {
            throw new IllegalArgumentException("El ID del vehículo es obligatorio.");
        }
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo ni estar vacío.");
        }

        return envioRepository.updateEstadoByVehiculoId(nuevoEstado.trim(), vehiculoId);
    }

    @Transactional(readOnly = true)
    public Page<EnvioDTO> listarPaginado(int page, int size, String sortBy, String dir, String busqueda,
            String estado) {
        Sort.Direction direccion = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direccion, sortBy));

        String estadoFiltro = (estado == null || estado.isBlank()) ? null : estado.trim();
        String busquedaFiltro = (busqueda == null || busqueda.isBlank()) ? null : busqueda.trim();

        return envioRepository.buscarPaginado(estadoFiltro, busquedaFiltro, pageRequest)
                .map(this::toEnvioDTO);
    }

    @Transactional(readOnly = true)
    public List<EnvioDTO> listarViaStoredProcedure(String estado) {
        return envioRepository.obtenerEnviosPorEstado(estado).stream()
                .map(this::toEnvioDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EnvioDTO buscarPorCodigoRastreo(String codigoRastreo) {
        Envio envio = envioRepository.findByCodigoRastreo(codigoRastreo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Envío con código de rastreo: " + codigoRastreo + " no encontrado"));
        return toEnvioDTO(envio);
    }

    // Lab 10: registro simplificado de envíos desde la SPA Angular (sin vehículo/conductor).
    // Lab 11: el código de rastreo ahora lo escribe el operador (validado en tiempo real
    // contra /check-tracking) y el envío se guarda junto con su lista de paquetes (1:N)
    // en la misma transacción.
    @Transactional
    public EnvioDTO crearEnvioSimple(CrearEnvioDTO datos) {
        if (envioRepository.findByCodigoRastreo(datos.getCodigoRastreo()).isPresent()) {
            throw new DuplicateResourceException(
                    "El número de rastreo [" + datos.getCodigoRastreo() + "] ya está en uso");
        }

        Vehiculo vehiculo = vehiculoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No hay vehículos registrados"));
        Conductor conductor = conductorRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No hay conductores registrados"));

        Envio envio = new Envio();
        envio.setCodigoRastreo(datos.getCodigoRastreo());
        envio.setDestinatario(datos.getDestinatario());
        envio.setDireccionDestino(datos.getDireccionDestino());
        envio.setCosto(datos.getMontoFlete());
        envio.setPesoKg(sumarPesoPaquetes(datos.getPaquetes()));
        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);
        envio.setEstadoEnvio("PENDIENTE");
        envio.setPaquetes(datos.getPaquetes().stream()
                .map(p -> paqueteDesdeDTO(p, envio))
                .toList());

        return toEnvioDTO(envioRepository.save(envio));
    }

    // Lab 11: respaldo del validador asíncrono de Angular (GET /check-tracking/{codigo}).
    @Transactional(readOnly = true)
    public boolean existeCodigoRastreo(String codigoRastreo) {
        return envioRepository.findByCodigoRastreo(codigoRastreo).isPresent();
    }

    private Paquete paqueteDesdeDTO(PaqueteDTO dto, Envio envio) {
        Paquete paquete = new Paquete();
        paquete.setDescripcion(dto.descripcion());
        paquete.setPesoKg(dto.pesoKg());
        paquete.setEnvio(envio);
        return paquete;
    }

    private BigDecimal sumarPesoPaquetes(List<PaqueteDTO> paquetes) {
        return paquetes.stream()
                .map(PaqueteDTO::pesoKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Lab 10: actualización simple de estado desde la SPA Angular (sin bitácora).
    @Transactional
    public EnvioDTO actualizarEstadoSimple(Integer id, String nuevoEstado) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envío con ID: " + id + " no encontrado"));

        Set<String> estadosValidos = Set.of("PENDIENTE", "EN_TRANSITO", "ENTREGADO", "CANCELADO");
        if (!estadosValidos.contains(nuevoEstado)) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado);
        }

        envio.setEstadoEnvio(nuevoEstado);
        return toEnvioDTO(envioRepository.save(envio));
    }

    public double calcularTarifa(double pesoKg, double distanciaKm) {
        double tarifa = 1000.0 + (100.0 * pesoKg) + (100.0 * distanciaKm);

        if (pesoKg >= 100.0) {
            tarifa += 750.0;
        }

        return tarifa;
    }

    @Transactional
    public Envio cancelarEnvio(Integer envioId) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new ResourceNotFoundException("Envío con ID: " + envioId + " no encontrado"));

        if (envio.getEstadoEnvio().equals("EN_TRANSITO") || envio.getEstadoEnvio().equals("ENTREGADO")) {
            throw new InvalidStateTransitionException(
                    "No se puede cancelar un envío que ya está en tránsito o entregado.");
        } else {

            Usuario usuarioActuante = usuarioActual();

            BitacoraEnvio bitacora = new BitacoraEnvio();
            bitacora.setEnvio(envio);
            bitacora.setEstadoAnterior(envio.getEstadoEnvio());
            bitacora.setEstadoNuevo("CANCELADO");
            bitacora.setFechaCambio(LocalDateTime.now());
            bitacora.setUsuario(usuarioActuante);
            bitacora.setObservaciones("Cancelación del envío");
            bitacoraEnvioRepository.save(bitacora);

            envio.setEstadoEnvio("CANCELADO");

            return envioRepository.save(envio);
        }
    }

    private Usuario usuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private EnvioResponseDTO toResponseDTO(Envio envio) {
        return new EnvioResponseDTO(
                envio.getId(),
                envio.getCodigoRastreo(),
                envio.getDireccionDestino(),
                envio.getPesoKg(),
                envio.getCosto(),
                envio.getEstadoEnvio(),
                envio.getVehiculo() != null ? envio.getVehiculo().getPlaca() : null,
                envio.getConductor() != null
                        ? envio.getConductor().getNombre() + " " + envio.getConductor().getApellidos()
                        : null);
    }

    private EnvioDTO toEnvioDTO(Envio envio) {
        return new EnvioDTO(
                envio.getId(),
                envio.getCodigoRastreo(),
                envio.getDestinatario(),
                envio.getDireccionDestino(),
                envio.getCosto(),
                envio.getEstadoEnvio(),
                envio.getFechaCreacion(),
                envio.getPaquetes().stream()
                        .map(p -> new PaqueteDTO(p.getId(), p.getDescripcion(), p.getPesoKg()))
                        .toList());
    }
}
