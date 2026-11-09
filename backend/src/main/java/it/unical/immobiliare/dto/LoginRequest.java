package it.unical.immobiliare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Inserire un'email valida")
    String email,

    @NotBlank(message = "La password è obbligatoria")
    String password

) {
}