package cr.ac.ucr.paraiso.ie.c4h877.expresofast.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.CrearEnvioDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioDTO;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioResponseDTO;

import jakarta.validation.Valid;

// Lab 10: controlador unico para la SPA Angular. Abierto sin login (ver SecurityConfig).
// La consola legado (legacy/vanilla-js-console) usaba /api/envios con JWT/roles y queda
// archivada tal cual, sin apuntar a este backend actualizado.
@RestController
@RequestMapping("/api/v1/envios")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    // Lab 10: lista de todos los envíos, para EnvioListComponent.
    @GetMapping
    public ResponseEntity<List<EnvioDTO>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(
                envioService.listarPaginado(page, size, sortBy, direction, busqueda, estado).getContent());
    }

    // Lab 9: listado vía stored procedure SP_OBTENER_ENVIOS_POR_ESTADO.
    @GetMapping("/procedimiento/{estado}")
    public ResponseEntity<List<EnvioDTO>> listarViaStoredProcedure(@PathVariable String estado) {
        return ResponseEntity.ok(envioService.listarViaStoredProcedure(estado));
    }

    // Lab 10: búsqueda por código de rastreo, para EnvioTrackingComponent.
    @GetMapping("/rastreo/{codigo}")
    public ResponseEntity<EnvioDTO> buscarPorCodigoRastreo(@PathVariable String codigo) {
        return ResponseEntity.ok(envioService.buscarPorCodigoRastreo(codigo));
    }

    // Lab 6: listado optimizado con JOIN FETCH (vehículo + conductor).
    @GetMapping("/optimizados")
    public ResponseEntity<List<EnvioResponseDTO>> getOptimizedShipments() {
        return ResponseEntity.ok(envioService.getOptimizedShipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponseDTO> obtenerEnvio(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerEnvio(id));
    }

    @PostMapping
    public ResponseEntity<EnvioDTO> registrarEnvio(@Valid @RequestBody CrearEnvioDTO envioDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(envioService.crearEnvioSimple(envioDTO));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioDTO> actualizarEstadoEnvio(@PathVariable Integer id,
            @Valid @RequestBody CambioEstadoDTO cambioDTO) {
        return ResponseEntity.ok(envioService.actualizarEstadoSimple(id, cambioDTO.getNuevoEstado()));
    }

    @GetMapping("/{id}/bitacora")
    public ResponseEntity<List<BitacoraResponseDTO>> obtenerBitacora(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerBitacora(id));
    }
}
