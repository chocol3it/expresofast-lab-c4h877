package cr.ac.ucr.paraiso.ie.c4h877.expresofast.business;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Vehiculo;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    public EnvioService(EnvioRepository envioRepository, VehiculoRepository vehiculoRepository,
            ConductorRepository conductorRepository) {
        this.envioRepository = envioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
    }

    @Transactional(readOnly = true)
    public List<Envio> getOptimizedShipments() {
        return envioRepository.findAllOptimized();
    }

    @Transactional
    public Envio crearEnvio(Envio envio) {

        Vehiculo vehiculo = vehiculoRepository.findById(envio.getVehiculo().getId())
                .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado"));

        Conductor conductor = conductorRepository.findById(envio.getConductor().getId())
                .orElseThrow(() -> new EntityNotFoundException("Conductor no encontrado"));

        if (envio.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new IllegalArgumentException("El peso del envío excede la capacidad del vehículo");
        }

        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);
        if (envio.getEstadoEnvio() == null) {
            envio.setEstadoEnvio("PENDIENTE");
        }

        return envioRepository.save(envio);
    }

    @Transactional
    public Envio actualizarEstado(Integer id, String nuevoEstado) {

        if (id == null || nuevoEstado == null || nuevoEstado.isEmpty()) {
            throw new IllegalArgumentException("El ID del envío y el nuevo estado no pueden ser nulos o vacíos");
        }

        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Envío con ID: " + id + " no encontrado"));

        envio.setEstadoEnvio(nuevoEstado);

        return envio;
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

}
