package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.UtenteDAO;
import it.unical.immobiliare.model.Utente;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LoginService {

    private final UtenteDAO utenteDAO;
    private final PasswordEncoder passwordEncoder;

    public LoginService(
            UtenteDAO utenteDAO,
            PasswordEncoder passwordEncoder) {

        this.utenteDAO = utenteDAO;
        this.passwordEncoder = passwordEncoder;
    }

    public Utente verificaCredenziali(
            String email,
            String password) {

        String emailNormalizzata =
                email.trim().toLowerCase(Locale.ROOT);

        Utente utente = utenteDAO
                .trovaPerEmail(emailNormalizzata)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Credenziali non valide"
                    )
                );

        if (utente.isBannato()) {
            throw new IllegalArgumentException(
                "Credenziali non valide"
            );
        }

        if (!passwordEncoder.matches(
                password,
                utente.getPasswordHash())) {

            throw new IllegalArgumentException(
                "Credenziali non valide"
            );
        }

        return utente;
    }
}