package it.unical.immobiliare.controller;

import it.unical.immobiliare.dao.AnnuncioDAO;
import it.unical.immobiliare.dto.AnnuncioCardDTO;
import it.unical.immobiliare.dto.AnnuncioDettaglioDTO;
import it.unical.immobiliare.dto.PubblicazioneAnnuncioRequest;
import it.unical.immobiliare.service.PubblicazioneAnnuncioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/annunci")
public class AnnuncioController {

    private final AnnuncioDAO annuncioDAO;
    private final PubblicazioneAnnuncioService pubblicazioneService;

    public AnnuncioController(
            AnnuncioDAO annuncioDAO,
            PubblicazioneAnnuncioService pubblicazioneService) {

        this.annuncioDAO = annuncioDAO;
        this.pubblicazioneService = pubblicazioneService;
    }

    @GetMapping
    public List<AnnuncioCardDTO> getAnnunci(
            @RequestParam(required = false) String citta,
            @RequestParam(required = false) BigDecimal prezzoMax,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String tipoContratto) {

        return annuncioDAO.trovaAnnunciAttivi(
                citta,
                prezzoMax,
                categoria,
                tipoContratto
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnuncioDettaglioDTO> getAnnuncioPerId(
            @PathVariable Long id) {

        return annuncioDAO.trovaAnnuncioPerId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> pubblicaAnnuncio(
            @Valid @RequestBody PubblicazioneAnnuncioRequest richiesta,
            Authentication authentication) {

        String emailVenditore = authentication.getName();

        Long annuncioId = pubblicazioneService.pubblica(
                richiesta,
                emailVenditore
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "id", annuncioId,
                        "messaggio", "Annuncio pubblicato correttamente"
                )
        );
    }
}