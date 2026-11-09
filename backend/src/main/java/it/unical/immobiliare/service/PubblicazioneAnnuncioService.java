package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.AnnuncioDAO;
import it.unical.immobiliare.dao.ImmobileDAO;
import it.unical.immobiliare.dao.UtenteDAO;
import it.unical.immobiliare.dto.PubblicazioneAnnuncioRequest;
import it.unical.immobiliare.model.Utente;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PubblicazioneAnnuncioService {

    private final ImmobileDAO immobileDAO;
    private final AnnuncioDAO annuncioDAO;
    private final UtenteDAO utenteDAO;

    public PubblicazioneAnnuncioService(
            ImmobileDAO immobileDAO,
            AnnuncioDAO annuncioDAO,
            UtenteDAO utenteDAO) {

        this.immobileDAO = immobileDAO;
        this.annuncioDAO = annuncioDAO;
        this.utenteDAO = utenteDAO;
    }

    @Transactional
    public Long pubblica(
            PubblicazioneAnnuncioRequest richiesta,
            String emailAutenticata) {

        Utente venditore = utenteDAO.trovaPerEmail(emailAutenticata)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Utente non autorizzato"
                        )
                );

        if (venditore.isBannato()
                || !"VENDITORE".equals(venditore.getRuolo())) {

            throw new AccessDeniedException(
                    "Solo un venditore autorizzato può pubblicare annunci"
            );
        }

        Long immobileId = immobileDAO.inserisciImmobile(richiesta);

        return annuncioDAO.inserisciAnnuncio(
                immobileId,
                venditore.getId(),
                richiesta
        );
    }
}