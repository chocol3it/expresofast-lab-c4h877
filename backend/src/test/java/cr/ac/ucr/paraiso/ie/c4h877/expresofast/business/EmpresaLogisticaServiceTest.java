package cr.ac.ucr.paraiso.ie.c4h877.expresofast.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        when(empresaRepository.findById(1)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> empresaService.obtener(1));
    }
}
