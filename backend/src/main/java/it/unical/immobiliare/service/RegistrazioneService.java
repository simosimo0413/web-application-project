package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.UtenteDAO;
import it.unical.immobiliare.dto.RegistrazioneRequest;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RegistrazioneService {

    private final UtenteDAO utenteDAO;
    private final PasswordEncoder passwordEncoder;

    public RegistrazioneService(
            UtenteDAO utenteDAO,
            PasswordEncoder passwordEncoder) {

        this.utenteDAO = utenteDAO;
        this.passwordEncoder = passwordEncoder;
    }

    public Long registra(RegistrazioneRequest richiesta) {

        String ruolo = richiesta.ruolo()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!ruolo.equals("ACQUIRENTE")
                && !ruolo.equals("VENDITORE")) {
            throw new IllegalArgumentException(
                    "Ruolo non valido per la registrazione"
            );
        }

        String email = richiesta.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (utenteDAO.emailEsistente(email)) {
            throw new IllegalArgumentException(
                    "Email già registrata"
            );
        }

        String passwordHash =
                passwordEncoder.encode(richiesta.password());

        try {
            return utenteDAO.registraUtente(
                    richiesta.nome().trim(),
                    richiesta.cognome().trim(),
                    email,
                    passwordHash,
                    ruolo
            );

        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException(
                    "Email già registrata"
            );
        }
    }
}