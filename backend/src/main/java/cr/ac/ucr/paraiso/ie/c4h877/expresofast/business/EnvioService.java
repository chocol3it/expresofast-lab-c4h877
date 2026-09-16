package cr.ac.ucr.paraiso.ie.c4h877.expresofast.business;

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
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
}
