package cr.ac.ucr.paraiso.ie.c4h877.expresofast.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioResponseDTO;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin(origins = "*")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/optimizados")
    public ResponseEntity<List<EnvioResponseDTO>> getOptimizedShipments() {
        return ResponseEntity.ok(envioService.getOptimizedShipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponseDTO> obtenerEnvio(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerEnvio(id));
    }

    @PostMapping
    public ResponseEntity<EnvioResponseDTO> registrarEnvio(@Valid @RequestBody EnvioRequestDTO envioDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(envioService.crearEnvio(envioDTO));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> actualizarEstadoEnvio(@PathVariable Integer id,
            @Valid @RequestBody CambioEstadoDTO cambioDTO) {
        return ResponseEntity.ok(envioService.actualizarEstado(id, cambioDTO));
    }

    @GetMapping("/{id}/bitacora")
    public ResponseEntity<List<BitacoraResponseDTO>> obtenerBitacora(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerBitacora(id));
    }
}
