package it.unical.immobiliare.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RibassoPrezzoRequest(
        @NotNull @DecimalMin(value = "0", inclusive = false)
        @DecimalMax("9999999999.99") BigDecimal nuovoPrezzo
) {}
