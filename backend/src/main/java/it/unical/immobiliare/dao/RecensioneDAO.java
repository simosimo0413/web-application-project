package it.unical.immobiliare.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import it.unical.immobiliare.dto.RecensioneResponse;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class RecensioneDAO {

    private final JdbcTemplate jdbcTemplate;

    public RecensioneDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean visitaRecensibile(
            Long visitaId,
            String emailAcquirente) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM visite v
                JOIN utenti u
                    ON u.id = v.acquirente_id
                WHERE v.id = ?
                AND u.email = ?
                AND v.confermata = TRUE
                AND v.data_visita < CURRENT_TIMESTAMP
            )
            """;

        Boolean risultato = jdbcTemplate.queryForObject(
            sql,
            Boolean.class,
            visitaId,
            emailAcquirente
        );

        return Boolean.TRUE.equals(risultato);
    }

    public boolean esisteRecensione(Long visitaId) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM recensioni
                WHERE visita_id = ?
            )
            """;

        Boolean risultato = jdbcTemplate.queryForObject(
            sql,
            Boolean.class,
            visitaId
        );

        return Boolean.TRUE.equals(risultato);
    }

    public Long inserisciRecensione(
            Long visitaId,
            Integer voto,
            String commento) {

        String sql = """
            INSERT INTO recensioni (
                visita_id,
                voto,
                commento
            )
            VALUES (?, ?, ?)
            RETURNING id
            """;

        return jdbcTemplate.queryForObject(
            sql,
            Long.class,
            visitaId,
            voto,
            commento
        );
    }
    public List<RecensioneResponse> trovaRecensioniPerAnnuncio(
        Long annuncioId) {

        String sql = """
            SELECT
                r.id,
                r.voto,
                r.commento,
                r.data_recensione
            FROM recensioni r
            JOIN visite v
                ON v.id = r.visita_id
            WHERE v.annuncio_id = ?
            ORDER BY r.data_recensione DESC, r.id DESC
            """;

        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new RecensioneResponse(
                rs.getLong("id"),
                rs.getInt("voto"),
                rs.getString("commento"),
                rs.getObject(
                    "data_recensione",
                    OffsetDateTime.class
                ).toInstant()
            ),
            annuncioId
        );
    }
}