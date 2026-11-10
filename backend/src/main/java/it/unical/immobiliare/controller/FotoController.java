package it.unical.immobiliare.controller;

import it.unical.immobiliare.dto.FotoResponse;
import it.unical.immobiliare.service.FotoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/annunci/{annuncioId}/foto")
public class FotoController {

    private final FotoService fotoService;

    public FotoController(FotoService fotoService) {
        this.fotoService = fotoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FotoResponse> caricaFoto(
            @PathVariable Long annuncioId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "descrizione", required = false)
            String descrizione,
            Authentication authentication) {

        FotoResponse risposta = fotoService.caricaFoto(
            annuncioId,
            file,
            descrizione,
            authentication
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(risposta);
    }
    @GetMapping
    public List<FotoResponse> recuperaFoto(
            @PathVariable Long annuncioId) {

        return fotoService.recuperaFotoAnnuncio(annuncioId);
    }
}