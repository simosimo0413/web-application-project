package it.unical.immobiliare.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Dati modificabili dal venditore; il ribasso del prezzo ha un endpoint separato. */
public record ModificaAnnuncioRequest(
        @NotBlank @Size(max = 200) String titolo,
        @NotBlank @Size(max = 10000) String descrizione,
        @NotNull @DecimalMin(value = "0", inclusive = false)
        @DecimalMax("99999999.99") BigDecimal superficieMq,
        @NotBlank @Size(max = 255) String indirizzo,
        @NotBlank @Size(max = 120) String citta,
        @Size(max = 100) String provincia
) {}
