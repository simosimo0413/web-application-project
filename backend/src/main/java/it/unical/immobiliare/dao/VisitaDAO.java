package it.unical.immobiliare.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import java.util.Optional;

@Repository
public class VisitaDAO {

    private final JdbcTemplate jdbcTemplate;

    public VisitaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long inserisciVisita(
            Long annuncioId,
            Long acquirenteId,
            Instant dataVisita) {

        String sql = """
            INSERT INTO visite (
                annuncio_id,
                acquirente_id,
                data_visita
            )
            VALUES (?, ?, ?)
            RETURNING id
            """;

        OffsetDateTime dataOra = OffsetDateTime.ofInstant(
            dataVisita,
            ZoneOffset.UTC
        );

        return jdbcTemplate.queryForObject(
            sql,
            Long.class,
            annuncioId,
            acquirenteId,
            dataOra
        );
    }
    public Optional<Long> trovaAcquirenteIdPerEmail(String email) {

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
    public record VisitaRicevuta(
        Long id,
        Long annuncioId,
        String emailAcquirente,
        Instant dataVisita,
        boolean confermata
) {
}

    public List<VisitaRicevuta> trovaVisiteRicevute(
            String emailVenditore) {

        String sql = """
            SELECT
                vi.id,
                vi.annuncio_id,
                acq.email AS email_acquirente,
                vi.data_visita,
                vi.confermata
            FROM visite vi
            JOIN annunci a
                ON a.id = vi.annuncio_id
            JOIN utenti vend
                ON vend.id = a.venditore_id
            JOIN utenti acq
                ON acq.id = vi.acquirente_id
            WHERE vend.email = ?
            ORDER BY vi.data_visita ASC, vi.id ASC
            """;

        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new VisitaRicevuta(
                rs.getLong("id"),
                rs.getLong("annuncio_id"),
                rs.getString("email_acquirente"),
                rs.getObject(
                    "data_visita",
                    OffsetDateTime.class
                ).toInstant(),
                rs.getBoolean("confermata")
            ),
            emailVenditore
        );
    }
    public boolean confermaVisita(
        Long visitaId,
        String emailVenditore) {

    String sql = """
        UPDATE visite AS vi
        SET confermata = TRUE
        FROM annunci AS a
        JOIN utenti AS vend
            ON vend.id = a.venditore_id
        WHERE vi.id = ?
        AND vi.annuncio_id = a.id
        AND vend.email = ?
        AND vi.confermata = FALSE
        AND vi.data_visita > CURRENT_TIMESTAMP
        """;

    int righeAggiornate = jdbcTemplate.update(
        sql,
        visitaId,
        emailVenditore
    );

    return righeAggiornate == 1;
}

}