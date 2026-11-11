
package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.OffertaDAO;
import it.unical.immobiliare.dao.UtenteDAO;
import it.unical.immobiliare.dto.OffertaRequest;
import it.unical.immobiliare.model.Utente;
import it.unical.immobiliare.dto.OffertaRiepilogoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
public class OffertaService {

    private final OffertaDAO offertaDAO;
    private final UtenteDAO utenteDAO;

    public OffertaService(
            OffertaDAO offertaDAO,
            UtenteDAO utenteDAO) {

        this.offertaDAO = offertaDAO;
        this.utenteDAO = utenteDAO;
    }

    @Transactional
    public Long presentaOfferta(
            Long astaId,
            OffertaRequest richiesta,
            Authentication authentication) {

        // 1. Verifica autenticazione.
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            throw new AccessDeniedException(
                "Devi effettuare il login"
            );
        }

        // 2. Verifica ruolo acquirente.
        boolean acquirente = authentication.getAuthorities()
            .stream()
            .anyMatch(autorita ->
                "ROLE_ACQUIRENTE".equals(
                    autorita.getAuthority()
                )
            );

        if (!acquirente) {
            throw new AccessDeniedException(
                "Solo gli acquirenti possono presentare offerte"
            );
        }

        // 3. Verifica identificativo dell'asta.
        if (astaId == null || astaId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID asta non valido"
            );
        }

        // 4. Verifica importo.
        if (richiesta == null || richiesta.importo() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Importo obbligatorio"
            );
        }

        BigDecimal importo = richiesta.importo();

        if (importo.compareTo(BigDecimal.ZERO) <= 0
                || importo.scale() > 2
                || importo.compareTo(
                    new BigDecimal("9999999999.99")
                ) > 0) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Importo non valido"
            );
        }

        // 5. Recupera l'ID dell'acquirente autenticato.
        Long offerenteId = utenteDAO
            .trovaPerEmail(authentication.getName())
            .map(Utente::getId)
            .orElseThrow(() -> new AccessDeniedException(
                "Utente autenticato non trovato"
            ));

        // 6. Blocca l'asta per gestire offerte simultanee.
        OffertaDAO.AstaBloccata asta = offertaDAO
            .bloccaAsta(astaId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Asta non trovata"
            ));

        // 7. Impedisce al proprietario di fare offerte.
        if (offerenteId.equals(asta.venditoreId())) {
            throw new AccessDeniedException(
                "Non puoi fare offerte sulla tua asta"
            );
        }

        // 8. Verifica che l'asta sia in corso.
        Instant ora = Instant.now();

        if (ora.isBefore(asta.inizio())
                || !ora.isBefore(asta.fine())) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "L'asta non è attualmente aperta"
            );
        }

        // 9. Recupera l'offerta massima.
        Optional<BigDecimal> massimo =
            offertaDAO.trovaOffertaMassima(astaId);

        if (massimo.isPresent()) {

            // Le offerte successive devono superare la massima.
            if (importo.compareTo(massimo.get()) <= 0) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "L'offerta deve superare quella attuale"
                );
            }

        } else {

            // La prima offerta deve raggiungere il prezzo base.
            if (importo.compareTo(asta.prezzoBase()) < 0) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'offerta è inferiore al prezzo base"
                );
            }
        }

        // 10. Registra l'offerta.
        return offertaDAO.inserisciOfferta(
            astaId,
            offerenteId,
            importo
        );
    }

public OffertaRiepilogoResponse recuperaRiepilogo(Long astaId) {

    // 1. Verifica che l'ID dell'asta sia valido.
    if (astaId == null || astaId <= 0) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "ID asta non valido"
        );
    }

    // 2. Recupera il riepilogo dal database.
    return offertaDAO.recuperaRiepilogo(astaId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Asta non trovata"
        ));
}

}
