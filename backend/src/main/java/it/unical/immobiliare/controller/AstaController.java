
package it.unical.immobiliare.controller;

import it.unical.immobiliare.dto.AstaRequest;
import it.unical.immobiliare.service.AstaService;
import it.unical.immobiliare.dto.AstaResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/aste")
public class AstaController {

    private final AstaService astaService;

    public AstaController(AstaService astaService) {
        this.astaService = astaService;
    }

    @PostMapping("/annunci/{annuncioId}")
    public ResponseEntity<Map<String, Long>> creaAsta(
            @PathVariable Long annuncioId,
            @RequestBody AstaRequest richiesta,
            Authentication authentication) {

        Long astaId = astaService.creaAsta(
            annuncioId,
            richiesta,
            authentication
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("id", astaId));
    }


@GetMapping("/annunci/{annuncioId}")
public ResponseEntity<AstaResponse> recuperaAsta(
        @PathVariable Long annuncioId) {

    AstaResponse asta = astaService.recuperaAstaPerAnnuncio(
        annuncioId
    );

    return ResponseEntity.ok(asta);
}

}
