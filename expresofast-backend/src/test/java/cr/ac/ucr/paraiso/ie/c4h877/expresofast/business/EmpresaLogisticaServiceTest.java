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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class EmpresaLogisticaServiceTest {

    @Mock
    private EmpresaLogisticaRepository empresaRepository;

    @InjectMocks
    private EmpresaLogisticaService empresaService;

    @Test
    void registrarEmpresa_DatosValidos_RetornaEmpresa() {
        EmpresaLogistica empresa = new EmpresaLogistica();
        empresa.setNombre("ExpresoFast Central");
        empresa.setCedulaJuridica("3-101-998877");

        when(empresaRepository.existsByNombre(empresa.getNombre())).thenReturn(false);
        when(empresaRepository.existsByCedulaJuridica(empresa.getCedulaJuridica())).thenReturn(false);
        when(empresaRepository.save(empresa)).thenReturn(empresa);

        EmpresaLogistica resultado = empresaService.registrar(empresa);

        assertEquals(empresa, resultado);
        verify(empresaRepository).save(empresa);
    }

    @Test
    void registrarEmpresa_NombreDuplicado_LanzaExcepcion() {
        EmpresaLogistica empresa = new EmpresaLogistica();
        empresa.setNombre("ExpresoFast Central");
        empresa.setCedulaJuridica("3-101-998877");

        when(empresaRepository.existsByNombre(empresa.getNombre())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> empresaService.registrar(empresa));

        verify(empresaRepository, never()).save(any(EmpresaLogistica.class));
    }

    @Test
    void obtenerEmpresa_Inexistente_LanzaExcepcion() {
        when(empresaRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> empresaService.obtener(1));
    }

    @Test
    void registrarEmpresa_CedulaDuplicada_LanzaExcepcion() {
        EmpresaLogistica empresa = new EmpresaLogistica();
        empresa.setNombre("ExpresoFast Central");
        empresa.setCedulaJuridica("3-101-998877");

        when(empresaRepository.existsByNombre(empresa.getNombre())).thenReturn(false);
        when(empresaRepository.existsByCedulaJuridica(empresa.getCedulaJuridica())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> empresaService.registrar(empresa));

        verify(empresaRepository, never()).save(any(EmpresaLogistica.class));
    }

    @Test
    void listar_RetornaListaDeEmpresas() {
        EmpresaLogistica empresa = new EmpresaLogistica();
        empresa.setNombre("ExpresoFast Central");
        when(empresaRepository.findAll()).thenReturn(List.of(empresa));

        List<EmpresaLogistica> resultado = empresaService.listar();

        assertEquals(1, resultado.size());
        assertEquals("ExpresoFast Central", resultado.get(0).getNombre());
    }

    @Test
    void actualizarEmpresa_Existente_ActualizaYRetorna() {
        EmpresaLogistica existente = new EmpresaLogistica();
        existente.setId(1);
        existente.setNombre("Nombre Viejo");

        EmpresaLogistica datos = new EmpresaLogistica();
        datos.setNombre("Nombre Nuevo");
        datos.setCedulaJuridica("3-101-111111");
        datos.setTelefono("22223333");

        when(empresaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(empresaRepository.save(existente)).thenReturn(existente);

        ArgumentCaptor<EmpresaLogistica> captor = ArgumentCaptor.forClass(EmpresaLogistica.class);

        EmpresaLogistica resultado = empresaService.actualizar(1, datos);

        verify(empresaRepository).save(captor.capture());
        assertEquals("Nombre Nuevo", captor.getValue().getNombre());
        assertEquals("3-101-111111", captor.getValue().getCedulaJuridica());
        assertEquals(existente, resultado);
    }

    @Test
    void eliminarEmpresa_Existente_EliminaEmpresa() {
        when(empresaRepository.existsById(1)).thenReturn(true);

        empresaService.eliminar(1);

        verify(empresaRepository).deleteById(1);
    }

    @Test
    void eliminarEmpresa_Inexistente_LanzaExcepcion() {
        when(empresaRepository.existsById(99)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> empresaService.eliminar(99));

        verify(empresaRepository, never()).deleteById(any());
    }
}
