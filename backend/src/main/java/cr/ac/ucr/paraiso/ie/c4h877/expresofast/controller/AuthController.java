package cr.ac.ucr.paraiso.ie.c4h877.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.business.AuthService;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.AuthResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO loginDTO) {
        return ResponseEntity.ok(authService.login(loginDTO));
    }
}
