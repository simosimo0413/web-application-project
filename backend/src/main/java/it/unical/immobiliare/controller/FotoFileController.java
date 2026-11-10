package it.unical.immobiliare.controller;

import it.unical.immobiliare.service.FotoStorageService;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
public class FotoFileController {

    private final FotoStorageService fotoStorageService;

    public FotoFileController(FotoStorageService fotoStorageService) {
        this.fotoStorageService = fotoStorageService;
    }

    @GetMapping("/api/foto/file/{nomeFile}")
    public ResponseEntity<Resource> visualizzaFoto(
            @PathVariable String nomeFile) {

        Path percorso = fotoStorageService.trovaFile(nomeFile);

        MediaType tipo;

        if (nomeFile.endsWith(".png")) {
            tipo = MediaType.IMAGE_PNG;
        } else if (nomeFile.endsWith(".webp")) {
            tipo = MediaType.parseMediaType("image/webp");
        } else {
            tipo = MediaType.IMAGE_JPEG;
        }

        Resource immagine = new FileSystemResource(percorso);

        return ResponseEntity.ok()
            .contentType(tipo)
            .body(immagine);
    }
}