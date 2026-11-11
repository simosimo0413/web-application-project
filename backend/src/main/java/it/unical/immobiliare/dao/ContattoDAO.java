package it.unical.immobiliare.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import it.unical.immobiliare.dto.ContattoResponse;
import java.time.OffsetDateTime;
import java.util.List;

import java.util.Optional;

@Repository
public class ContattoDAO {

    private final JdbcTemplate jdbcTemplate;

    public ContattoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long inserisciContatto(
            Long annuncioId,
            Long mittenteId,
            String oggetto,
            String messaggio) {

        String sql = """
            INSERT INTO contatti (
                annuncio_id,
                mittente_id,
                oggetto,
                messaggio
            )
            VALUES (?, ?, ?, ?)
            RETURNING id
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Long.class,
            annuncioId,
            mittenteId,
            oggetto,
            messaggio
        );
    }

    public Optional<Long> trovaUtenteIdPerEmail(String email) {

        String sql = """
            SELECT id
            FROM utenti
            WHERE email = ?
            """;
    
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> rs.getLong("id"),
            email
        ).stream().findFirst();
    }

    public boolean esisteAnnuncio(Long annuncioId) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM annunci
                WHERE id = ?
            )
            """;
    
        Boolean esiste = jdbcTemplate.queryForObject(
            sql,
            Boolean.class,
            annuncioId
        );
    
        return Boolean.TRUE.equals(esiste);
    }
    public List<ContattoResponse> trovaMessaggiRicevuti(
        String emailVenditore) {

    String sql = """
        SELECT
            c.id,
            c.annuncio_id,
            m.email AS email_mittente,
            c.oggetto,
            c.messaggio,
            c.data_invio
        FROM contatti c
        JOIN annunci a
            ON a.id = c.annuncio_id
        JOIN utenti v
            ON v.id = a.venditore_id
        JOIN utenti m
            ON m.id = c.mittente_id
        WHERE v.email = ?
        ORDER BY c.data_invio DESC, c.id DESC
        """;

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) -> new ContattoResponse(
            rs.getLong("id"),
            rs.getLong("annuncio_id"),
            rs.getString("email_mittente"),
            rs.getString("oggetto"),
            rs.getString("messaggio"),
            rs.getObject(
                "data_invio",
                OffsetDateTime.class
            ).toInstant()
        ),
        emailVenditore
    );
}

}
