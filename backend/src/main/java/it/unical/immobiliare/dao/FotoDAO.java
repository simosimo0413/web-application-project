package it.unical.immobiliare.dao;

import it.unical.immobiliare.dto.FotoResponse;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FotoDAO {

    private final JdbcTemplate jdbcTemplate;

    public FotoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public FotoResponse inserisciFoto(
            Long immobileId,
            String url,
            String descrizione,
            boolean principale) {

        String sql = """
            INSERT INTO foto (
                immobile_id,
                url,
                descrizione,
                principale
            )
            VALUES (?, ?, ?, ?)
            RETURNING id
            """;

        Long id = jdbcTemplate.queryForObject(
            sql,
            Long.class,
            immobileId,
            url,
            descrizione,
            principale
        );

        return new FotoResponse(
            id,
            url,
            descrizione,
            principale
        );
    }

    public List<FotoResponse> trovaPerImmobile(Long immobileId) {

        String sql = """
            SELECT id, url, descrizione, principale
            FROM foto
            WHERE immobile_id = ?
            ORDER BY principale DESC, id ASC
            """;

        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new FotoResponse(
                rs.getLong("id"),
                rs.getString("url"),
                rs.getString("descrizione"),
                rs.getBoolean("principale")
            ),
            immobileId
        );
    }
    public List<FotoResponse> trovaPerAnnuncio(Long annuncioId) {

        String sql = """
            SELECT
                f.id,
                f.url,
                f.descrizione,
                f.principale
            FROM foto f
            JOIN annunci a ON a.immobile_id = f.immobile_id
            WHERE a.id = ?
            ORDER BY f.principale DESC, f.id ASC
            """;
    
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new FotoResponse(
                rs.getLong("id"),
                rs.getString("url"),
                rs.getString("descrizione"),
                rs.getBoolean("principale")
            ),
            annuncioId
        );
    }
    public Optional<FotoResponse> trovaPerAnnuncioEId(
        Long annuncioId,
        Long fotoId) {

    String sql = """
        SELECT
            f.id,
            f.url,
            f.descrizione,
            f.principale
        FROM foto f
        JOIN annunci a ON a.immobile_id = f.immobile_id
        WHERE a.id = ?
          AND f.id = ?
        """;

    List<FotoResponse> fotografie = jdbcTemplate.query(
        sql,
        (rs, rowNum) -> new FotoResponse(
            rs.getLong("id"),
            rs.getString("url"),
            rs.getString("descrizione"),
            rs.getBoolean("principale")
        ),
        annuncioId,
        fotoId
    );

        return fotografie.stream().findFirst();
    }
    public boolean eliminaPerAnnuncioEId(
        Long annuncioId,
        Long fotoId) {

    String sql = """
        DELETE FROM foto f
        USING annunci a
        WHERE f.immobile_id = a.immobile_id
        AND a.id = ?
        AND f.id = ?
        """;

    int righeEliminate = jdbcTemplate.update(
        sql,
        annuncioId,
        fotoId
    );

        return righeEliminate == 1;
    }
    public void assegnaPrincipaleSeAssente(Long annuncioId) {

        String sql = """
            UPDATE foto f
            SET principale = TRUE
            WHERE f.id = (
                SELECT f2.id
                FROM foto f2
                JOIN annunci a
                    ON a.immobile_id = f2.immobile_id
                WHERE a.id = ?
                ORDER BY f2.id ASC
                LIMIT 1
            )
            AND NOT EXISTS (
                SELECT 1
                FROM foto fp
                WHERE fp.immobile_id = f.immobile_id
                AND fp.principale = TRUE
            )
            """;
        jdbcTemplate.update(sql, annuncioId);
    }
}