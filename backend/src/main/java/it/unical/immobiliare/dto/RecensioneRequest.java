package it.unical.immobiliare.dto;

public record RecensioneRequest(
        Integer voto,
        String commento
) {
}