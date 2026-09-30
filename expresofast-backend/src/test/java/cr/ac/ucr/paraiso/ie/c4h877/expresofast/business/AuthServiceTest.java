package cr.ac.ucr.paraiso.ie.c4h877.expresofast.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_CredencialesValidas_RetornaTokenYRolesFiltrados() {
        AuthRequestDTO solicitud = new AuthRequestDTO();
        solicitud.setUsername("operador1");
        solicitud.setPassword("clave123");

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_OPERADOR"),
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("SCOPE_read"));

        Authentication authentication = mock(Authentication.class);
        doReturn(authorities).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn("operador1");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generarToken(authentication)).thenReturn("token-abc");
        when(tokenProvider.getExpirationMs()).thenReturn(3600000L);

        AuthResponseDTO resultado = authService.login(solicitud);

        assertEquals("token-abc", resultado.getToken());
        assertEquals("operador1", resultado.getUsername());
        assertEquals(List.of("ROLE_OPERADOR", "ROLE_ADMIN"), resultado.getRoles());
        assertEquals(3600000L, resultado.getExpirationTime());
        assertEquals("Bearer", resultado.getType());
    }

    @Test
    void login_CredencialesInvalidas_PropagaExcepcion() {
        AuthRequestDTO solicitud = new AuthRequestDTO();
        solicitud.setUsername("operador1");
        solicitud.setPassword("incorrecta");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThrows(BadCredentialsException.class, () -> authService.login(solicitud));
    }
}
