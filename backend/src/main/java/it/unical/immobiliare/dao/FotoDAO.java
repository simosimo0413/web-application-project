package it.unical.immobiliare.dao;

import it.unical.immobiliare.dto.FotoResponse;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}