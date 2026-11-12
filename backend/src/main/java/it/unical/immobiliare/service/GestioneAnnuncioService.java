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

    /** L'amministratore puo gestire tutti gli annunci, il venditore solo i propri. */
    private AnnuncioDAO.AnnuncioGestibile bloccaAnnuncioGestibile(Long id, Authentication auth) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID annuncio non valido");
        }
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Devi effettuare l'accesso");
        }
        Utente utente = utenteDAO.trovaPerEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Utente non trovato"));
        String ruolo = utente.getRuolo();
        boolean ruoloValido = auth.getAuthorities().stream()
                .anyMatch(a -> ("ROLE_" + ruolo).equals(a.getAuthority()));
        if (utente.isBannato() || !ruoloValido) {
            throw new AccessDeniedException("Sessione non autorizzata");
        }
        if ("ADMIN".equals(ruolo)) {
            return annuncioDAO.bloccaAttivoPerId(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Annuncio attivo non trovato"));
        }
        if ("VENDITORE".equals(ruolo)) {
            return annuncioDAO.bloccaAttivoSeProprietario(id, utente.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Annuncio attivo non trovato"));
        }
        throw new AccessDeniedException("Accesso riservato ad amministratori e venditori");
    }

    @Transactional
    public void modifica(Long id, ModificaAnnuncioRequest dati, Authentication auth) {
        AnnuncioDAO.AnnuncioGestibile annuncio = bloccaAnnuncioGestibile(id, auth);
        if (dati.superficieMq().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Massimo due decimali per la superficie");
        }
        annuncioDAO.aggiornaTitolo(id, dati.titolo().trim());
        immobileDAO.aggiornaDati(annuncio.immobileId(), dati);
    }

    @Transactional
    public void ribassa(Long id, RibassoPrezzoRequest dati, Authentication auth) {
        AnnuncioDAO.AnnuncioGestibile annuncio = bloccaAnnuncioGestibile(id, auth);
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
        bloccaAnnuncioGestibile(id, auth);
        if (annuncioDAO.haAstaNonTerminata(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Non puoi chiudere un annuncio con un'asta non terminata");
        }
        // Chiusura logica: preserva visite, offerte, recensioni e altri riferimenti.
        annuncioDAO.chiudiAnnuncio(id);
    }
}
