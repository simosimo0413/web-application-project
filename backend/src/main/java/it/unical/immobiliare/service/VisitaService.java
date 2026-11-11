package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.AnnuncioDAO;
import it.unical.immobiliare.dao.VisitaDAO;
import it.unical.immobiliare.dto.VisitaRequest;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import it.unical.immobiliare.dao.VisitaDAO.VisitaRicevuta;
import java.util.List;

import java.time.Instant;

@Service
public class VisitaService {

    private final VisitaDAO visitaDAO;
    private final AnnuncioDAO annuncioDAO;

    public VisitaService(
            VisitaDAO visitaDAO,
            AnnuncioDAO annuncioDAO) {

        this.visitaDAO = visitaDAO;
        this.annuncioDAO = annuncioDAO;
    }

    public Long richiediVisita(
            Long annuncioId,
            VisitaRequest richiesta,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            throw new AccessDeniedException(
                "Devi effettuare il login"
            );
        }

        boolean acquirente = authentication.getAuthorities()
            .stream()
            .anyMatch(autorita ->
                "ROLE_ACQUIRENTE".equals(
                    autorita.getAuthority()
                )
            );

        if (!acquirente) {
            throw new AccessDeniedException(
                "Solo gli acquirenti possono richiedere una visita"
            );
        }

        if (annuncioId == null || annuncioId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID annuncio non valido"
            );
        }

        if (!visitaDAO.esisteAnnuncio(annuncioId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Annuncio non trovato"
            );
        }

        if (richiesta == null || richiesta.dataVisita() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La data della visita Ã¨ obbligatoria"
            );
        }

        Instant dataVisita = richiesta.dataVisita();

        if (!dataVisita.isAfter(Instant.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La data della visita deve essere futura"
            );
        }

        if (annuncioDAO.trovaImmobileIdSeProprietario(
                annuncioId,
                authentication.getName()
            ).isPresent()) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Non puoi richiedere una visita per un tuo annuncio"
            );
        }

        Long acquirenteId = visitaDAO
            .trovaAcquirenteIdPerEmail(authentication.getName())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Utente autenticato non trovato"
                )
            );

        return visitaDAO.inserisciVisita(
            annuncioId,
            acquirenteId,
            dataVisita
        );
    }
    public List<VisitaRicevuta> recuperaVisiteRicevute(
        Authentication authentication) {

    // 1. Verifica che l'utente sia autenticato.
    if (authentication == null
            || !authentication.isAuthenticated()
            || "anonymousUser".equals(authentication.getName())) {

        throw new AccessDeniedException(
            "Devi effettuare il login"
        );
    }

    // 2. Verifica che l'utente sia un venditore.
    boolean venditore = authentication.getAuthorities()
        .stream()
        .anyMatch(autorita ->
            "ROLE_VENDITORE".equals(
                autorita.getAuthority()
            )
        );

    if (!venditore) {
        throw new AccessDeniedException(
            "Solo i venditori possono visualizzare le visite ricevute"
        );
    }

    return visitaDAO.trovaVisiteRicevute(
        authentication.getName()
        );
    }
    public void confermaVisita(
        Long visitaId,
        Authentication authentication) {

    if (authentication == null
            || !authentication.isAuthenticated()
            || "anonymousUser".equals(authentication.getName())) {

        throw new AccessDeniedException(
            "Devi effettuare il login"
        );
    }

    boolean venditore = authentication.getAuthorities()
        .stream()
        .anyMatch(autorita ->
            "ROLE_VENDITORE".equals(
                autorita.getAuthority()
            )
        );

    if (!venditore) {
        throw new AccessDeniedException(
            "Solo i venditori possono confermare le visite"
        );
    }

    if (visitaId == null || visitaId <= 0) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "ID visita non valido"
        );
    }

    boolean confermata = visitaDAO.confermaVisita(
        visitaId,
        authentication.getName()
    );

    if (!confermata) {
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Visita non trovata o non confermabile"
        );
    }
}

}