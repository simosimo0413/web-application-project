
package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.AstaDAO;
import it.unical.immobiliare.dto.AstaRequest;
import it.unical.immobiliare.dto.AstaResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class AstaService {

    private final AstaDAO astaDAO;

    public AstaService(AstaDAO astaDAO) {
        this.astaDAO = astaDAO;
    }

    public Long creaAsta(
            Long annuncioId,
            AstaRequest richiesta,
            Authentication authentication) {

        // 1. Verifica autenticazione.
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            throw new AccessDeniedException(
                "Devi effettuare il login"
            );
        }

        // 2. Verifica ruolo venditore.
        boolean venditore = authentication.getAuthorities()
            .stream()
            .anyMatch(autorita ->
                "ROLE_VENDITORE".equals(
                    autorita.getAuthority()
                )
            );

        if (!venditore) {
            throw new AccessDeniedException(
                "Solo i venditori possono creare aste"
            );
        }

        // 3. Verifica ID annuncio.
        if (annuncioId == null || annuncioId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID annuncio non valido"
            );
        }

        // 4. Verifica campi obbligatori.
        if (richiesta == null
                || richiesta.prezzoBase() == null
                || richiesta.inizio() == null
                || richiesta.fine() == null) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Prezzo base, inizio e fine sono obbligatori"
            );
        }

        BigDecimal prezzoBase = richiesta.prezzoBase();
        Instant inizio = richiesta.inizio();
        Instant fine = richiesta.fine();

        // 5. Verifica importo.
        if (prezzoBase.compareTo(BigDecimal.ZERO) <= 0
                || prezzoBase.scale() > 2
                || prezzoBase.compareTo(
                    new BigDecimal("9999999999.99")
                ) > 0) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Prezzo base non valido"
            );
        }

        // 6. Verifica date.
        if (!inizio.isAfter(Instant.now())
                || !fine.isAfter(inizio)) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Le date dell'asta non sono valide"
            );
        }

        // 7. Verifica proprietà e idoneità dell'annuncio.
        if (!astaDAO.annuncioIdoneoPerAsta(
                annuncioId,
                authentication.getName())) {

            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Annuncio non trovato o non idoneo all'asta"
            );
        }

        // 8. Inserimento con protezione dai duplicati.
        try {
            return astaDAO.inserisciAsta(
                annuncioId,
                prezzoBase,
                inizio,
                fine
            );
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Esiste già un'asta per questo annuncio",
                ex
            );
        }
    }

public AstaResponse recuperaAstaPerAnnuncio(Long annuncioId) {

    // 1. Verifica che l'ID sia valido.
    if (annuncioId == null || annuncioId <= 0) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "ID annuncio non valido"
        );
    }

    // 2. Cerca l'asta associata all'annuncio.
    return astaDAO.trovaPerAnnuncio(annuncioId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Nessuna asta associata a questo annuncio"
        ));
}

}
