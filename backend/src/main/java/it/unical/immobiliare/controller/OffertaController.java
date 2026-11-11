
package it.unical.immobiliare.controller;

import it.unical.immobiliare.dto.OffertaRequest;
import it.unical.immobiliare.service.OffertaService;
import it.unical.immobiliare.dto.OffertaRiepilogoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/offerte")
public class OffertaController {

    private final OffertaService offertaService;

    public OffertaController(OffertaService offertaService) {
        this.offertaService = offertaService;
    }

    @PostMapping("/aste/{astaId}")
    public ResponseEntity<Map<String, Long>> presentaOfferta(
            @PathVariable Long astaId,
            @RequestBody OffertaRequest richiesta,
            Authentication authentication) {

        Long offertaId = offertaService.presentaOfferta(
            astaId,
            richiesta,
            authentication
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("id", offertaId));
    }


@GetMapping("/aste/{astaId}/riepilogo")
public ResponseEntity<OffertaRiepilogoResponse> recuperaRiepilogo(
        @PathVariable Long astaId) {

    OffertaRiepilogoResponse riepilogo =
        offertaService.recuperaRiepilogo(astaId);

    return ResponseEntity.ok(riepilogo);
}

}
