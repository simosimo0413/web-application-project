
package it.unical.immobiliare.dto;

import java.math.BigDecimal;

public record AnnuncioDettaglioDTO(
    Long id,
    String titolo,
    String descrizione,
    BigDecimal prezzo,
    BigDecimal prezzoPrecedente,
    BigDecimal superficieMq,
    String indirizzo,
    String citta,
    String provincia,
    String categoria,
    String tipoContratto,
    String fotoPrincipale,
    Long venditoreId
) {
}
