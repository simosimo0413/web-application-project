
package it.unical.immobiliare.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AstaResponse(
        Long id,
        Long annuncioId,
        BigDecimal prezzoBase,
        Instant inizio,
        Instant fine,
        String stato
) {
}
