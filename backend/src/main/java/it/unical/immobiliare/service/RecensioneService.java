package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.RecensioneDAO;
import it.unical.immobiliare.dto.RecensioneRequest;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import it.unical.immobiliare.dto.RecensioneResponse;
import java.util.List;

@Service
public class RecensioneService {

    private final RecensioneDAO recensioneDAO;

    public RecensioneService(RecensioneDAO recensioneDAO) {
        this.recensioneDAO = recensioneDAO;
    }

    public Long pubblicaRecensione(
            Long visitaId,
            RecensioneRequest richiesta,
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
                "Solo gli acquirenti possono pubblicare recensioni"
            );
        }
        if (visitaId == null || visitaId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID visita non valido"
            );
        }
        if (richiesta == null
                || richiesta.voto() == null
                || richiesta.voto() < 1
                || richiesta.voto() > 5) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Il voto deve essere compreso tra 1 e 5"
            );
        }
        if (richiesta.commento() == null
                || richiesta.commento().isBlank()) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Il commento è obbligatorio"
            );
        }

        String commento = richiesta.commento().trim();

        if (commento.length() > 5000) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Il commento non può superare 5000 caratteri"
            );
        }

        boolean recensibile = recensioneDAO.visitaRecensibile(
            visitaId,
            authentication.getName()
        );

        if (!recensibile) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Visita non trovata o non recensibile"
            );
        }
        if (recensioneDAO.esisteRecensione(visitaId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Questa visita è già stata recensita"
            );
        }
        try {
            return recensioneDAO.inserisciRecensione(
                visitaId,
                richiesta.voto(),
                commento
            );

        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Questa visita è già stata recensita",
                ex
            );
        }
    }
    public List<RecensioneResponse> recuperaRecensioniAnnuncio(
        Long annuncioId) {

        if (annuncioId == null || annuncioId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID annuncio non valido"
            );
        }

        return recensioneDAO.trovaRecensioniPerAnnuncio(
            annuncioId
        );
    }
}