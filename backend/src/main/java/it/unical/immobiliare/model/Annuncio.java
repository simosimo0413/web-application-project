package it.unical.immobiliare.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Annuncio {

    private Long id;
    private Immobile immobile;
    private Utente venditore;
    private String titolo;
    private String tipoContratto;
    private BigDecimal prezzo;
    private BigDecimal prezzoPrecedente;
    private String stato;
    private OffsetDateTime dataPubblicazione;

    public Annuncio() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Immobile getImmobile() {
        return immobile;
    }

    public void setImmobile(Immobile immobile) {
        this.immobile = immobile;
    }

    public Utente getVenditore() {
        return venditore;
    }

    public void setVenditore(Utente venditore) {
        this.venditore = venditore;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getTipoContratto() {
        return tipoContratto;
    }

    public void setTipoContratto(String tipoContratto) {
        this.tipoContratto = tipoContratto;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public BigDecimal getPrezzoPrecedente() {
        return prezzoPrecedente;
    }

    public void setPrezzoPrecedente(BigDecimal prezzoPrecedente) {
        this.prezzoPrecedente = prezzoPrecedente;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public OffsetDateTime getDataPubblicazione() {
        return dataPubblicazione;
    }

    public void setDataPubblicazione(OffsetDateTime dataPubblicazione) {
        this.dataPubblicazione = dataPubblicazione;
    }
}