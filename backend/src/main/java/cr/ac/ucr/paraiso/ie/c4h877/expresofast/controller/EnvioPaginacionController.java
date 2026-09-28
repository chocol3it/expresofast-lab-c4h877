package cr.ac.ucr.paraiso.ie.c4h877.expresofast.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto.EnvioDTO;

@RestController
@RequestMapping("/api/v1/envios")
public class EnvioPaginacionController {

    private final EnvioService envioService;

    public EnvioPaginacionController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping
    public ResponseEntity<Page<EnvioDTO>> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(envioService.listarPaginado(page, size, sortBy, direction, busqueda, estado));
    }

    @GetMapping("/procedimiento/{estado}")
    public ResponseEntity<List<EnvioDTO>> listarViaStoredProcedure(@PathVariable String estado) {
        return ResponseEntity.ok(envioService.listarViaStoredProcedure(estado));
    }
}
