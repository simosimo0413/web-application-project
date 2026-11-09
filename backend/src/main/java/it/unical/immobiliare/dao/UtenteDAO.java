package it.unical.immobiliare.dao;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import it.unical.immobiliare.model.Utente;
import org.springframework.dao.EmptyResultDataAccessException;

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
    public Optional<Utente> trovaPerEmail(String email) {

        String sql = """
            SELECT
                id,
                nome,
                cognome,
                email,
                password_hash,
                ruolo,
                bannato,
                data_registrazione
            FROM utenti
            WHERE LOWER(email) = LOWER(?)
            """;
    
        try {
            Utente utente = jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {
                    Utente u = new Utente();
    
                    u.setId(rs.getLong("id"));
                    u.setNome(rs.getString("nome"));
                    u.setCognome(rs.getString("cognome"));
                    u.setEmail(rs.getString("email"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setRuolo(rs.getString("ruolo"));
                    u.setBannato(rs.getBoolean("bannato"));
    
                    var data = rs.getObject(
                        "data_registrazione",
                        java.time.OffsetDateTime.class
                    );
                    u.setDataRegistrazione(data);
    
                    return u;
                },
                email
            );
    
            return Optional.ofNullable(utente);
    
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}