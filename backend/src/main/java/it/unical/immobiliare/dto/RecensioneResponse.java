package it.unical.immobiliare.dto;

import java.time.Instant;

public record RecensioneResponse(
        Long id,
        Integer voto,
        String commento,
        Instant dataRecensione
) {
}