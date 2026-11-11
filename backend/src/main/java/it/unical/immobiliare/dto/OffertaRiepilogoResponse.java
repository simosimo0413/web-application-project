
package it.unical.immobiliare.dto;

import java.math.BigDecimal;

public record OffertaRiepilogoResponse(
        Long astaId,
        BigDecimal offertaMassima,
        Long numeroOfferte
) {
}
