package it.unical.immobiliare.dto;

import java.time.Instant;

public record VisitaRequest(
        Instant dataVisita
) {
}