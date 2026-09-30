package cr.ac.ucr.paraiso.ie.c4h877.expresofast.business;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaLogisticaService {

    private final EmpresaLogisticaRepository empresaRepository;

    public EmpresaLogisticaService(EmpresaLogisticaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<EmpresaLogistica> listar() {
        return empresaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EmpresaLogistica obtener(Integer id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa logística no encontrada"));
    }

    @Transactional
    public EmpresaLogistica registrar(EmpresaLogistica empresa) {
        if (empresaRepository.existsByNombre(empresa.getNombre())) {
            throw new DuplicateResourceException("Ya existe una empresa con ese nombre");
        }
        if (empresaRepository.existsByCedulaJuridica(empresa.getCedulaJuridica())) {
            throw new DuplicateResourceException("Ya existe una empresa con esa cédula jurídica");
        }
        return empresaRepository.save(empresa);
    }

    @Transactional
    public EmpresaLogistica actualizar(Integer id, EmpresaLogistica datos) {
        EmpresaLogistica empresa = obtener(id);
        empresa.setNombre(datos.getNombre());
        empresa.setCedulaJuridica(datos.getCedulaJuridica());
        empresa.setTelefono(datos.getTelefono());
        empresa.setFechaRegistro(datos.getFechaRegistro());
        return empresaRepository.save(empresa);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa logística no encontrada");
        }
        empresaRepository.deleteById(id);
    }
}
