
package it.unical.immobiliare.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import it.unical.immobiliare.dto.OffertaRiepilogoResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class OffertaDAO {

    private final JdbcTemplate jdbcTemplate;

    public OffertaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record AstaBloccata(
            Long id,
            Long annuncioId,
            Long venditoreId,
            BigDecimal prezzoBase,
            Instant inizio,
            Instant fine
    ) {
    }

    public Optional<AstaBloccata> bloccaAsta(Long astaId) {

        String sql = """
            SELECT
                ast.id,
                ast.annuncio_id,
                ann.venditore_id,
                ast.prezzo_base,
                ast.inizio,
                ast.fine
            FROM aste ast
            JOIN annunci ann
                ON ann.id = ast.annuncio_id
            WHERE ast.id = ? AND ann.stato = 'ATTIVO'
            FOR UPDATE OF ast, ann
            """;

        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new AstaBloccata(
                rs.getLong("id"),
                rs.getLong("annuncio_id"),
                rs.getLong("venditore_id"),
                rs.getBigDecimal("prezzo_base"),
                rs.getObject(
                    "inizio",
                    OffsetDateTime.class
                ).toInstant(),
                rs.getObject(
                    "fine",
                    OffsetDateTime.class
                ).toInstant()
            ),
            astaId
        ).stream().findFirst();
    }

public Optional<BigDecimal> trovaOffertaMassima(Long astaId) {

    String sql = """
        SELECT MAX(importo)
        FROM offerte
        WHERE asta_id = ?
        """;

    BigDecimal importoMassimo = jdbcTemplate.queryForObject(
        sql,
        BigDecimal.class,
        astaId
    );

    return Optional.ofNullable(importoMassimo);
}

public Long inserisciOfferta(
        Long astaId,
        Long offerenteId,
        BigDecimal importo) {

    String sql = """
        INSERT INTO offerte (
            asta_id,
            offerente_id,
            importo,
            data_offerta
        )
        VALUES (?, ?, ?, CURRENT_TIMESTAMP)
        RETURNING id
        """;

    return jdbcTemplate.queryForObject(
        sql,
        Long.class,
        astaId,
        offerenteId,
        importo
    );
}

public Optional<OffertaRiepilogoResponse> recuperaRiepilogo(
        Long astaId) {

    String sql = """
        SELECT
            a.id AS asta_id,
            MAX(o.importo) AS offerta_massima,
            COUNT(o.id) AS numero_offerte
        FROM aste a
        LEFT JOIN offerte o
            ON o.asta_id = a.id
        WHERE a.id = ?
        GROUP BY a.id
        """;

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) -> new OffertaRiepilogoResponse(
            rs.getLong("asta_id"),
            rs.getBigDecimal("offerta_massima"),
            rs.getLong("numero_offerte")
        ),
        astaId
    ).stream().findFirst();
}

}
