package cr.ac.ucr.paraiso.ie.c4h877.expresofast.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Envio;

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
    public ResponseEntity<List<Envio>> getOptimizedShipments() {
        List<Envio> optimizedShipments = envioService.getOptimizedShipments();
        return ResponseEntity.ok(optimizedShipments);
    }

    @PostMapping
    public ResponseEntity<Envio> registrarEnvio(@RequestBody Envio envio) {
        Envio nuevoEnvio = envioService.crearEnvio(envio);
        return ResponseEntity.ok(nuevoEnvio);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Envio> actualizarEstadoEnvio(@PathVariable Integer id, @RequestBody CambioEstadoRequest request) {
        Envio envioActualizado = envioService.actualizarEstado(id, request.estadoEnvio());
        return ResponseEntity.ok(envioActualizado);
    }

    public record CambioEstadoRequest(String estadoEnvio) {
    }

}
