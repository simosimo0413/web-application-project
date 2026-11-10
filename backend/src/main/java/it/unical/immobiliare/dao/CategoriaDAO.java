package it.unical.immobiliare.dao;

import it.unical.immobiliare.dto.CategoriaResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoriaDAO {

    private final JdbcTemplate jdbcTemplate;

    public CategoriaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CategoriaResponse> trovaTutte() {

        String sql = """
            SELECT id, nome, descrizione
            FROM categorie
            ORDER BY nome
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new CategoriaResponse(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("descrizione")
            )
        );
    }
}