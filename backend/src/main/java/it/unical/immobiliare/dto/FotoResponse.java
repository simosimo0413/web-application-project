package it.unical.immobiliare.dto;

public record FotoResponse(
    Long id,
    String url,
    String descrizione,
    boolean principale
) {
}