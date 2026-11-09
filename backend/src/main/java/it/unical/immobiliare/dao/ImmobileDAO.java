package it.unical.immobiliare.dao;

import it.unical.immobiliare.dto.PubblicazioneAnnuncioRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImmobileDAO {

    private final JdbcTemplate jdbcTemplate;

    public ImmobileDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long inserisciImmobile(PubblicazioneAnnuncioRequest richiesta) {

        String sql = """
            INSERT INTO immobili (
                categoria_id,
                superficie_mq,
                indirizzo,
                citta,
                provincia,
                cap,
                descrizione
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Long.class,
            richiesta.categoriaId(),
            richiesta.superficieMq(),
            richiesta.indirizzo(),
            richiesta.citta(),
            richiesta.provincia(),
            richiesta.cap(),
            richiesta.descrizione()
        );
    }
}