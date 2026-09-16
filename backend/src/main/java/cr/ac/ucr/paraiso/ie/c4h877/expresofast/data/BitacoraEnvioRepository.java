package cr.ac.ucr.paraiso.ie.c4h877.expresofast.data;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.BitacoraEnvio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraEnvioRepository extends JpaRepository<BitacoraEnvio, Integer> {

    List<BitacoraEnvio> findByEnvio_IdOrderByFechaCambioDesc(Integer envioId);
}
