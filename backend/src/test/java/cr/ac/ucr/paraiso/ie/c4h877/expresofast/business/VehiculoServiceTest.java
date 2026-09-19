package cr.ac.ucr.paraiso.ie.c4h877.expresofast.business;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.InvalidStateTransitionException;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private ConductorRepository conductorRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    @Test
    void registrarVehiculo_PlacaDuplicada_LanzaExcepcion() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("102938");

        when(vehiculoRepository.existsByPlaca("102938")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> vehiculoService.registrar(vehiculo));

        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }

    @Test
    void asignarConductor_ConductorInactivo_LanzaExcepcion() {
        Vehiculo vehiculo = new Vehiculo();
        Conductor conductor = new Conductor();
        conductor.setEstado("INACTIVO");

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));

        assertThrows(InvalidStateTransitionException.class,
                () -> vehiculoService.asignarConductor(1, 1));

        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }
}
