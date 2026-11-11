
package it.unical.immobiliare.dao;
import it.unical.immobiliare.dto.AstaResponse;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AstaDAO {

    private final JdbcTemplate jdbcTemplate;

    public AstaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean annuncioIdoneoPerAsta(
            Long annuncioId,
            String emailVenditore) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM annunci a
                JOIN utenti u
                    ON u.id = a.venditore_id
                WHERE a.id = ?
                  AND u.email = ?
                  AND a.stato = 'ATTIVO'
                  AND a.tipo_contratto = 'VENDITA'
            )
            """;

        Boolean risultato = jdbcTemplate.queryForObject(
            sql,
            Boolean.class,
            annuncioId,
            emailVenditore
        );

        return Boolean.TRUE.equals(risultato);
    }


public Long inserisciAsta(
        Long annuncioId,
        BigDecimal prezzoBase,
        Instant inizio,
        Instant fine) {

    String sql = """
        INSERT INTO aste (
            annuncio_id,
            prezzo_base,
            inizio,
            fine
        )
        VALUES (?, ?, ?, ?)
        RETURNING id
        """;

    OffsetDateTime dataInizio =
        OffsetDateTime.ofInstant(inizio, ZoneOffset.UTC);

    OffsetDateTime dataFine =
        OffsetDateTime.ofInstant(fine, ZoneOffset.UTC);

    return jdbcTemplate.queryForObject(
        sql,
        Long.class,
        annuncioId,
        prezzoBase,
        dataInizio,
        dataFine
    );
}

public Optional<AstaResponse> trovaPerAnnuncio(Long annuncioId) {

    String sql = """
        SELECT
            id,
            annuncio_id,
            prezzo_base,
            inizio,
            fine,
            CASE
                WHEN CURRENT_TIMESTAMP < inizio
                    THEN 'PROGRAMMATA'
                WHEN CURRENT_TIMESTAMP >= fine
                    THEN 'TERMINATA'
                ELSE 'IN_CORSO'
            END AS stato
        FROM aste
        WHERE annuncio_id = ?
        """;

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) -> new AstaResponse(
            rs.getLong("id"),
            rs.getLong("annuncio_id"),
            rs.getBigDecimal("prezzo_base"),
            rs.getObject(
                "inizio",
                OffsetDateTime.class
            ).toInstant(),
            rs.getObject(
                "fine",
                OffsetDateTime.class
            ).toInstant(),
            rs.getString("stato")
        ),
        annuncioId
    ).stream().findFirst();
}

}
