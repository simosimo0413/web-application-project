package it.unical.immobiliare.dto;

import java.math.BigDecimal;

/** Riepilogo anche degli annunci non piu visibili nel catalogo pubblico. */
public record AdminAnnuncioDTO(
        Long id, String titolo, String venditoreEmail,
        BigDecimal prezzo, String stato) {}
