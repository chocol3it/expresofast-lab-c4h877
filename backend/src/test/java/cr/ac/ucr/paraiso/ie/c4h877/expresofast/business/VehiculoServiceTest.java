package cr.ac.ucr.paraiso.ie.c4h877.expresofast.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.ResourceNotFoundException;

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

    @Test
    void listar_RetornaListaDeVehiculos() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("102938");
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculo));

        List<Vehiculo> resultado = vehiculoService.listar();

        assertEquals(1, resultado.size());
        assertEquals("102938", resultado.get(0).getPlaca());
    }

    @Test
    void obtener_VehiculoExistente_RetornaVehiculo() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));

        Vehiculo resultado = vehiculoService.obtener(1);

        assertEquals(1, resultado.getId());
    }

    @Test
    void obtener_VehiculoInexistente_LanzaExcepcion() {
        when(vehiculoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vehiculoService.obtener(99));
    }

    @Test
    void registrar_PlacaDisponible_GuardaVehiculo() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("102938");

        when(vehiculoRepository.existsByPlaca("102938")).thenReturn(false);
        when(vehiculoRepository.save(vehiculo)).thenReturn(vehiculo);

        Vehiculo resultado = vehiculoService.registrar(vehiculo);

        assertEquals(vehiculo, resultado);
        verify(vehiculoRepository).save(vehiculo);
    }

    @Test
    void eliminar_VehiculoExistente_EliminaVehiculo() {
        when(vehiculoRepository.existsById(1)).thenReturn(true);

        vehiculoService.eliminar(1);

        verify(vehiculoRepository).deleteById(1);
    }

    @Test
    void eliminar_VehiculoInexistente_LanzaExcepcion() {
        when(vehiculoRepository.existsById(99)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> vehiculoService.eliminar(99));

        verify(vehiculoRepository, never()).deleteById(any());
    }

    @Test
    void asignarConductor_ConductorInexistente_LanzaExcepcion() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vehiculoService.asignarConductor(1, 1));

        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }

    @Test
    void asignarConductor_ConductorActivo_AsignaYGuarda() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);
        Conductor conductor = new Conductor();
        conductor.setId(1);
        conductor.setEstado("ACTIVO");

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));
        when(vehiculoRepository.save(vehiculo)).thenReturn(vehiculo);

        Vehiculo resultado = vehiculoService.asignarConductor(1, 1);

        assertEquals(conductor, resultado.getConductor());
        verify(vehiculoRepository).save(vehiculo);
    }
}
