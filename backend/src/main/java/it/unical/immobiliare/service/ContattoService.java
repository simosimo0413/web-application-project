package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.ContattoDAO;
import it.unical.immobiliare.dto.ContattoRequest;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import it.unical.immobiliare.dto.ContattoResponse;
import java.util.List;

@Service
public class ContattoService {

    private final ContattoDAO contattoDAO;

    public ContattoService(ContattoDAO contattoDAO) {
        this.contattoDAO = contattoDAO;
    }

    public Long inviaContatto(
            Long annuncioId,
            ContattoRequest richiesta,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            throw new AccessDeniedException(
                "Devi effettuare il login per inviare un messaggio"
            );
        }

        if (annuncioId == null || annuncioId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID annuncio non valido"
            );
        }

        if (!contattoDAO.esisteAnnuncio(annuncioId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Annuncio non trovato"
            );
        }

        if (richiesta == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Dati del messaggio mancanti"
            );
        }

        String oggetto = richiesta.oggetto() == null
            ? ""
            : richiesta.oggetto().trim();

        String messaggio = richiesta.messaggio() == null
            ? ""
            : richiesta.messaggio().trim();

        if (oggetto.isBlank() || messaggio.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Oggetto e messaggio sono obbligatori"
            );
        }

        if (oggetto.length() > 255) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "L'oggetto non può superare 255 caratteri"
            );
        }
        
        if (messaggio.length() > 5000) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Il messaggio non può superare 5000 caratteri"
            );
        }

        Long mittenteId = contattoDAO
            .trovaUtenteIdPerEmail(authentication.getName())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Utente autenticato non trovato"
                )
            );

        return contattoDAO.inserisciContatto(
            annuncioId,
            mittenteId,
            oggetto,
            messaggio
        );
    }
    public List<ContattoResponse> recuperaMessaggiRicevuti(
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
            "Solo i venditori possono consultare i messaggi ricevuti"
        );
    }

    // 3. Recupera i messaggi degli annunci del venditore.
    return contattoDAO.trovaMessaggiRicevuti(
        authentication.getName()
    );
}
}