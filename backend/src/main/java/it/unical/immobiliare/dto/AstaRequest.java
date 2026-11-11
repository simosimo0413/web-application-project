
package it.unical.immobiliare.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AstaRequest(
        BigDecimal prezzoBase,
        Instant inizio,
        Instant fine
) {
}
