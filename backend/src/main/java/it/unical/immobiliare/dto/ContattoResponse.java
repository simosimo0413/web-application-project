package it.unical.immobiliare.dto;

import java.time.Instant;

public record ContattoResponse(
        Long id,
        Long annuncioId,
        String emailMittente,
        String oggetto,
        String messaggio,
        Instant dataInvio
) {
}