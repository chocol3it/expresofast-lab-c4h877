package cr.ac.ucr.paraiso.ie.c4h877.expresofast.business;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.security.JwtTokenProvider;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponseDTO login(AuthRequestDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(), loginDTO.getPassword()));

        String token = tokenProvider.generarToken(authentication);
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .toList();

        return new AuthResponseDTO(token, authentication.getName(), roles,
                tokenProvider.getExpirationMs());
    }
}
