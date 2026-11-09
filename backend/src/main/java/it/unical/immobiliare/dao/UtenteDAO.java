package it.unical.immobiliare.dao;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UtenteDAO {

    private final JdbcTemplate jdbcTemplate;

    public UtenteDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean emailEsistente(String email) {
        String sql = """
            SELECT COUNT(*)
            FROM utenti
            WHERE LOWER(email) = LOWER(?)
            """;

        Long conteggio = jdbcTemplate.queryForObject(
            sql,
            Long.class,
            email
        );

        return conteggio != null && conteggio > 0;
    }

    public Long registraUtente(
            String nome,
            String cognome,
            String email,
            String passwordHash,
            String ruolo) {

        String sql = """
            INSERT INTO utenti
                (nome, cognome, email, password_hash, ruolo, bannato)
            VALUES (?, ?, ?, ?, ?, FALSE)
            RETURNING id
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Long.class,
            nome,
            cognome,
            email,
            passwordHash,
            ruolo
        );
    }
}