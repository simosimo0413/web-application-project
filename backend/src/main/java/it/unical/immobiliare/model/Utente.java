package it.unical.immobiliare.model;

import java.time.OffsetDateTime;

public class Utente {

    private Long id;
    private String nome;
    private String cognome;
    private String email;
    private String passwordHash;
    private String ruolo;
    private boolean bannato;
    private OffsetDateTime dataRegistrazione;

    public Utente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    public boolean isBannato() {
        return bannato;
    }

    public void setBannato(boolean bannato) {
        this.bannato = bannato;
    }

    public OffsetDateTime getDataRegistrazione() {
        return dataRegistrazione;
    }

    public void setDataRegistrazione(OffsetDateTime dataRegistrazione) {
        this.dataRegistrazione = dataRegistrazione;
    }
}