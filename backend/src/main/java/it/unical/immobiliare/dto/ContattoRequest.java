package it.unical.immobiliare.dto;

public record ContattoRequest(
        String oggetto,
        String messaggio
) {
}