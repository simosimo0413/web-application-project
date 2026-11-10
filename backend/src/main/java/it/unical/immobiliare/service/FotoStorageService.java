package it.unical.immobiliare.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FotoStorageService {

    private static final long DIMENSIONE_MASSIMA = 5L * 1024 * 1024;

    private static final String FORMATO_NOME_FILE =
        "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}[.](jpg|png|webp)";

    private final Path cartellaFoto = Path.of(
        "uploads", "immobili"
    ).toAbsolutePath().normalize();

    public String salva(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Seleziona una fotografia"
            );
        }

        if (file.getSize() > DIMENSIONE_MASSIMA) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fotografia non puÃ² superare 5 MB"
            );
        }

        try {
            String estensione = rilevaEstensione(file);

            Files.createDirectories(cartellaFoto);

            String nomeFile = UUID.randomUUID()
                + "." + estensione;

            Path destinazione = cartellaFoto.resolve(nomeFile);

            try (InputStream contenuto = file.getInputStream()) {
                Files.copy(contenuto, destinazione);
            }

            return "/api/foto/file/" + nomeFile;

        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Errore durante il salvataggio della fotografia",
                e
            );
        }
    }

    public void elimina(String url) {

        String prefisso = "/api/foto/file/";

        if (url == null || !url.startsWith(prefisso)) {
            throw new IllegalArgumentException(
                "URL della fotografia non valido"
            );
        }

        String nomeFile = url.substring(prefisso.length());

        if (!nomeFile.matches(FORMATO_NOME_FILE)) {
            throw new IllegalArgumentException(
                "Nome della fotografia non valido"
            );
        }

        Path percorso = cartellaFoto.resolve(nomeFile).normalize();

        if (!percorso.startsWith(cartellaFoto)) {
            throw new IllegalArgumentException(
                "Percorso della fotografia non valido"
            );
        }

        try {
            Files.deleteIfExists(percorso);

        } catch (IOException e) {
            throw new IllegalStateException(
                "Impossibile eliminare la fotografia",
                e
            );
        }
    }

    public Path trovaFile(String nomeFile) {

        if (nomeFile == null
                || !nomeFile.matches(FORMATO_NOME_FILE)) {

            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Fotografia non trovata"
            );
        }

        Path percorso = cartellaFoto.resolve(nomeFile).normalize();

        if (!percorso.startsWith(cartellaFoto)
                || !Files.isRegularFile(percorso)
                || !Files.isReadable(percorso)) {

            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Fotografia non trovata"
            );
        }

        return percorso;
    }

    private String rilevaEstensione(MultipartFile file)
            throws IOException {

        byte[] intestazione;

        try (InputStream contenuto = file.getInputStream()) {
            intestazione = contenuto.readNBytes(12);
        }

        if (intestazione.length >= 3
                && (intestazione[0] & 0xFF) == 0xFF
                && (intestazione[1] & 0xFF) == 0xD8
                && (intestazione[2] & 0xFF) == 0xFF) {

            return "jpg";
        }

        if (intestazione.length >= 8
                && (intestazione[0] & 0xFF) == 0x89
                && intestazione[1] == 0x50
                && intestazione[2] == 0x4E
                && intestazione[3] == 0x47
                && intestazione[4] == 0x0D
                && intestazione[5] == 0x0A
                && intestazione[6] == 0x1A
                && intestazione[7] == 0x0A) {

            return "png";
        }

        if (intestazione.length >= 12
                && intestazione[0] == 'R'
                && intestazione[1] == 'I'
                && intestazione[2] == 'F'
                && intestazione[3] == 'F'
                && intestazione[8] == 'W'
                && intestazione[9] == 'E'
                && intestazione[10] == 'B'
                && intestazione[11] == 'P') {

            return "webp";
        }

        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Formato non supportato. Usa JPG, PNG o WEBP"
        );
    }
}