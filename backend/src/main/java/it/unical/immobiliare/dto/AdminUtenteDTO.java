package it.unical.immobiliare.dto;

/** Dati pubblici del profilo per la sola amministrazione: nessun hash password. */
public record AdminUtenteDTO(
        Long id, String nome, String cognome, String email,
        String ruolo, boolean bannato) {}
