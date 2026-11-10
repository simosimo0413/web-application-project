package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.AnnuncioDAO;
import it.unical.immobiliare.dao.FotoDAO;
import it.unical.immobiliare.dto.FotoResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class FotoService {
    private static final Logger logger =
        LoggerFactory.getLogger(FotoService.class);

    private final AnnuncioDAO annuncioDAO;
    private final FotoDAO fotoDAO;
    private final FotoStorageService fotoStorageService;

    public FotoService(
            AnnuncioDAO annuncioDAO,
            FotoDAO fotoDAO,
            FotoStorageService fotoStorageService) {

        this.annuncioDAO = annuncioDAO;
        this.fotoDAO = fotoDAO;
        this.fotoStorageService = fotoStorageService;
    }

    public Long verificaProprietario(
            Long annuncioId,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
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
                "Solo i venditori possono caricare fotografie"
            );
        }

        return annuncioDAO.trovaImmobileIdSeProprietario(
            annuncioId,
            authentication.getName()
        ).orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Annuncio non trovato"
            )
        );
    }

    public FotoResponse caricaFoto(
            Long annuncioId,
            MultipartFile file,
            String descrizione,
            Authentication authentication) {

        Long immobileId = verificaProprietario(
            annuncioId,
            authentication
        );

        boolean principale = fotoDAO
            .trovaPerImmobile(immobileId)
            .isEmpty();

        String url = fotoStorageService.salva(file);

        try {

            return fotoDAO.inserisciFoto(
                immobileId,
                url,
                descrizione,
                principale
            );

        } catch (RuntimeException errore) {

            try {
                fotoStorageService.elimina(url);
            } catch (RuntimeException erroreEliminazione) {
                errore.addSuppressed(erroreEliminazione);
            }

            throw errore;
        }
    }
    public List<FotoResponse> recuperaFotoAnnuncio(Long annuncioId) {
        return fotoDAO.trovaPerAnnuncio(annuncioId);
    }
    @Transactional
    public void eliminaFoto(
            Long annuncioId,
            Long fotoId,
            Authentication authentication) {

        // 1. Verifica che il venditore sia proprietario dell'annuncio.
        verificaProprietario(annuncioId, authentication);

        // 2. Cerca la fotografia associata all'annuncio.
        FotoResponse fotografia = fotoDAO
            .trovaPerAnnuncioEId(annuncioId, fotoId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Fotografia non trovata"
                )
            );

        // 3. Elimina il record dal database.
        boolean eliminata = fotoDAO.eliminaPerAnnuncioEId(
            annuncioId,
            fotoId
        );

        if (!eliminata) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Fotografia non trovata"
            );
        }

        // 4. Se necessario, assegna una nuova foto principale.
        fotoDAO.assegnaPrincipaleSeAssente(annuncioId);

        // 5. Elimina il file fisico solo dopo il commit del database.
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    try {
                        fotoStorageService.elimina(fotografia.url());
                    } catch (RuntimeException errore) {
                        logger.error(
                            "Impossibile eliminare il file della foto {}: {}",
                            fotografia.id(),
                            fotografia.url(),
                            errore
                        );
                    }
                }
            }
        );
    }
}