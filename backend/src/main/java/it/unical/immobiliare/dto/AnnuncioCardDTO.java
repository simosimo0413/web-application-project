package it.unical.immobiliare.dto;

import java.math.BigDecimal;

public record AnnuncioCardDTO(
    Long id,
    String titolo,
    BigDecimal prezzo,
    BigDecimal prezzoPrecedente,
    BigDecimal superficieMq,
    String citta,
    String categoria,
    String tipoContratto,
    String fotoPrincipale
) {
}