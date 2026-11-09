package it.unical.immobiliare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrazioneRequest(

    @NotBlank(message = "Il nome è obbligatorio")
    String nome,

    @NotBlank(message = "Il cognome è obbligatorio")
    String cognome,

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Inserire un'email valida")
    String email,

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 8, message = "La password deve avere almeno 8 caratteri")
    String password,

    @NotBlank(message = "Il ruolo è obbligatorio")
    String ruolo

) {
}