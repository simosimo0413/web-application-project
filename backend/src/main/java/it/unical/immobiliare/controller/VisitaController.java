package it.unical.immobiliare.controller;

import it.unical.immobiliare.dto.VisitaRequest;
import it.unical.immobiliare.service.VisitaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import it.unical.immobiliare.dao.VisitaDAO.VisitaRicevuta;
import it.unical.immobiliare.dao.VisitaDAO.VisitaAcquirente;
import java.util.List;

import java.util.Map;

@RestController
@RequestMapping("/api/visite")
public class VisitaController {

    private final VisitaService visitaService;

    public VisitaController(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    @PostMapping("/annunci/{annuncioId}")
    public ResponseEntity<Map<String, Long>> richiediVisita(
            @PathVariable Long annuncioId,
            @RequestBody VisitaRequest richiesta,
            Authentication authentication) {

        Long visitaId = visitaService.richiediVisita(
            annuncioId,
            richiesta,
            authentication
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("id", visitaId));
    }
    @GetMapping("/ricevute")
    public ResponseEntity<List<VisitaRicevuta>> recuperaVisiteRicevute(
            Authentication authentication) {

        List<VisitaRicevuta> visite =
            visitaService.recuperaVisiteRicevute(authentication);

        return ResponseEntity.ok(visite);
    }

    @PatchMapping("/{visitaId}/conferma")
    public ResponseEntity<Void> confermaVisita(
            @PathVariable Long visitaId,
            Authentication authentication) {

        visitaService.confermaVisita(
            visitaId,
            authentication
        );

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/mie")
    public ResponseEntity<List<VisitaAcquirente>> recuperaVisiteAcquirente(
            Authentication authentication) {

        List<VisitaAcquirente> visite =
            visitaService.recuperaVisiteAcquirente(authentication);

        return ResponseEntity.ok(visite);
    }
}