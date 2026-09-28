package cr.ac.ucr.paraiso.ie.c4h877.expresofast.service;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Rol;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Usuario;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .authorities(usuario.getRoles().stream()
                        .map(Rol::getNombreRol)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()))
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}
