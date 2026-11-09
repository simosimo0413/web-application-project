package it.unical.immobiliare.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PubblicazioneAnnuncioRequest(

    @NotBlank
    String titolo,

    @NotBlank
    @Pattern(regexp = "VENDITA|AFFITTO")
    String tipoContratto,

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal prezzo,

    @NotNull
    @Positive
    Long categoriaId,

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal superficieMq,

    @NotBlank
    String indirizzo,

    @NotBlank
    String citta,

    String provincia,

    String cap,

    @NotBlank
    String descrizione

) {}