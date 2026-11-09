package it.unical.immobiliare.controller;

import it.unical.immobiliare.dto.RegistrazioneRequest;
import it.unical.immobiliare.service.RegistrazioneService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrazioneService registrazioneService;

    public AuthController(RegistrazioneService registrazioneService) {
        this.registrazioneService = registrazioneService;
    }

    @PostMapping("/registrazione")
    public ResponseEntity<Map<String, Object>> registrazione(
            @Valid @RequestBody RegistrazioneRequest richiesta) {

        try {
            Long id = registrazioneService.registra(richiesta);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "id", id,
                            "messaggio", "Registrazione completata"
                    ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("errore", e.getMessage()));
        }
    }
}