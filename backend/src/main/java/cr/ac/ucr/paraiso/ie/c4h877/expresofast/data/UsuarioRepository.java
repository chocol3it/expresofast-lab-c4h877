package cr.ac.ucr.paraiso.ie.c4h877.expresofast.data;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsername(String username);
}
