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

import java.util.List;

@Service
public class FotoService {

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
}