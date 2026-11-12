package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.AnnuncioDAO;
import it.unical.immobiliare.dao.ImmobileDAO;
import it.unical.immobiliare.dao.UtenteDAO;
import it.unical.immobiliare.dto.ModificaAnnuncioRequest;
import it.unical.immobiliare.dto.RibassoPrezzoRequest;
import it.unical.immobiliare.model.Utente;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class GestioneAnnuncioService {

    private final AnnuncioDAO annuncioDAO;
    private final ImmobileDAO immobileDAO;
    private final UtenteDAO utenteDAO;

    public GestioneAnnuncioService(AnnuncioDAO annuncioDAO,
                                  ImmobileDAO immobileDAO,
                                  UtenteDAO utenteDAO) {
        this.annuncioDAO = annuncioDAO;
        this.immobileDAO = immobileDAO;
        this.utenteDAO = utenteDAO;
    }

    private String venditoreAutorizzato(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName())
                || auth.getAuthorities().stream().noneMatch(a ->
                        "ROLE_VENDITORE".equals(a.getAuthority()))) {
            throw new AccessDeniedException("Accesso riservato ai venditori");
        }
        Utente utente = utenteDAO.trovaPerEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Utente non trovato"));
        if (utente.isBannato() || !"VENDITORE".equals(utente.getRuolo())) {
            throw new AccessDeniedException("Venditore non autorizzato");
        }
        return utente.getEmail();
    }

    private AnnuncioDAO.AnnuncioGestibile bloccaProprioAnnuncio(Long id, Authentication auth) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID annuncio non valido");
        }
        String email = venditoreAutorizzato(auth);
        return annuncioDAO.bloccaAttivoSeProprietario(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Annuncio attivo non trovato"));
    }

    @Transactional
    public void modifica(Long id, ModificaAnnuncioRequest dati, Authentication auth) {
        AnnuncioDAO.AnnuncioGestibile annuncio = bloccaProprioAnnuncio(id, auth);
        if (dati.superficieMq().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Massimo due decimali per la superficie");
        }
        annuncioDAO.aggiornaTitolo(id, dati.titolo().trim());
        immobileDAO.aggiornaDati(annuncio.immobileId(), dati);
    }

    @Transactional
    public void ribassa(Long id, RibassoPrezzoRequest dati, Authentication auth) {
        AnnuncioDAO.AnnuncioGestibile annuncio = bloccaProprioAnnuncio(id, auth);
        BigDecimal prezzo = dati.nuovoPrezzo();
        if (prezzo.scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Massimo due decimali");
        }
        if (prezzo.compareTo(annuncio.prezzo()) >= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Il nuovo prezzo deve essere inferiore a quello attuale");
        }
        if (annuncioDAO.haAstaNonTerminata(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Non puoi cambiare il prezzo durante un'asta non terminata");
        }
        annuncioDAO.ribassaPrezzo(id, prezzo);
    }

    @Transactional
    public void chiudi(Long id, Authentication auth) {
        bloccaProprioAnnuncio(id, auth);
        if (annuncioDAO.haAstaNonTerminata(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Non puoi chiudere un annuncio con un'asta non terminata");
        }
        // Chiusura logica: preserva visite, offerte, recensioni e altri riferimenti.
        annuncioDAO.chiudiAnnuncio(id);
    }
}
