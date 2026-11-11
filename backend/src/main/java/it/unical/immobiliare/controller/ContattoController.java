package it.unical.immobiliare.controller;

import it.unical.immobiliare.dto.ContattoRequest;
import it.unical.immobiliare.service.ContattoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import it.unical.immobiliare.dto.ContattoResponse;
import java.util.List;

import java.util.Map;

@RestController
@RequestMapping("/api/contatti")
public class ContattoController {

    private final ContattoService contattoService;

    public ContattoController(ContattoService contattoService) {
        this.contattoService = contattoService;
    }

    @PostMapping("/annunci/{annuncioId}")
    public ResponseEntity<Map<String, Long>> inviaContatto(
            @PathVariable Long annuncioId,
            @RequestBody ContattoRequest richiesta,
            Authentication authentication) {

        Long contattoId = contattoService.inviaContatto(
            annuncioId,
            richiesta,
            authentication
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("id", contattoId));
    }
    @GetMapping("/ricevuti")
    public ResponseEntity<List<ContattoResponse>> recuperaMessaggiRicevuti(
            Authentication authentication) {

        List<ContattoResponse> messaggi =
            contattoService.recuperaMessaggiRicevuti(authentication);

        return ResponseEntity.ok(messaggi);
    }
}