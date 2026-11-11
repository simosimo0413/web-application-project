package it.unical.immobiliare.controller;

import it.unical.immobiliare.dto.RecensioneRequest;
import it.unical.immobiliare.service.RecensioneService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import it.unical.immobiliare.dto.RecensioneResponse;
import java.util.List;

import java.util.Map;

@RestController
@RequestMapping("/api/recensioni")
public class RecensioneController {

    private final RecensioneService recensioneService;

    public RecensioneController(RecensioneService recensioneService) {
        this.recensioneService = recensioneService;
    }

    @PostMapping("/visite/{visitaId}")
    public ResponseEntity<Map<String, Long>> pubblicaRecensione(
            @PathVariable Long visitaId,
            @RequestBody RecensioneRequest richiesta,
            Authentication authentication) {

        Long recensioneId = recensioneService.pubblicaRecensione(
            visitaId,
            richiesta,
            authentication
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("id", recensioneId));
    }
    @GetMapping("/annunci/{annuncioId}")
    public ResponseEntity<List<RecensioneResponse>> recuperaRecensioniAnnuncio(
            @PathVariable Long annuncioId) {

        List<RecensioneResponse> recensioni =
            recensioneService.recuperaRecensioniAnnuncio(annuncioId);

        return ResponseEntity.ok(recensioni);
    }
}