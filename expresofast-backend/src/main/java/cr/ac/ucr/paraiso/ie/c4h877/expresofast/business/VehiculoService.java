package cr.ac.ucr.paraiso.ie.c4h877.expresofast.business;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository,
            ConductorRepository conductorRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> listar() {
        return vehiculoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehiculo obtener(Integer id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
    }

    @Transactional
    public Vehiculo registrar(Vehiculo vehiculo) {
        if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
            throw new DuplicateResourceException("Ya existe un vehículo con la placa indicada");
        }
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo actualizar(Integer id, Vehiculo datos) {
        Vehiculo vehiculo = obtener(id);
        vehiculo.setPlaca(datos.getPlaca());
        vehiculo.setCapacidadKg(datos.getCapacidadKg());
        vehiculo.setEstado(datos.getEstado());
        vehiculo.setEmpresa(datos.getEmpresa());
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehículo no encontrado");
        }
        vehiculoRepository.deleteById(id);
    }

    @Transactional
    public Vehiculo asignarConductor(Integer vehiculoId, Integer conductorId) {
        Vehiculo vehiculo = obtener(vehiculoId);
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));

        if (!"ACTIVO".equals(conductor.getEstado())) {
            throw new InvalidStateTransitionException(
                    "No se puede asignar un conductor inactivo");
        }

        vehiculo.setConductor(conductor);
        return vehiculoRepository.save(vehiculo);
    }
}
