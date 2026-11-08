package it.unical.immobiliare.dao;

import it.unical.immobiliare.dto.AnnuncioCardDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AnnuncioDAO {

    private final JdbcTemplate jdbcTemplate;

    public AnnuncioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AnnuncioCardDTO> trovaAnnunciAttivi(
            String citta,
            BigDecimal prezzoMax,
            String categoria,
            String tipoContratto) {

        StringBuilder sql = new StringBuilder("""
            SELECT
                a.id,
                a.titolo,
                a.prezzo,
                a.prezzo_precedente,
                i.superficie_mq,
                i.citta,
                c.nome AS categoria,
                a.tipo_contratto,
                f.url AS foto_principale
            FROM annunci a
            JOIN immobili i ON a.immobile_id = i.id
            JOIN categorie c ON i.categoria_id = c.id
            LEFT JOIN foto f
                ON f.immobile_id = i.id
                AND f.principale = TRUE
            WHERE a.stato = 'ATTIVO'
            """);

        List<Object> parametri = new ArrayList<>();

        if (citta != null && !citta.isBlank()) {
            sql.append(" AND LOWER(i.citta) LIKE LOWER(?)");
            parametri.add("%" + citta.trim() + "%");
        }

        if (prezzoMax != null) {
            sql.append(" AND a.prezzo <= ?");
            parametri.add(prezzoMax);
        }

        if (categoria != null && !categoria.isBlank()) {
            sql.append(" AND LOWER(c.nome) = LOWER(?)");
            parametri.add(categoria.trim());
        }

        if (tipoContratto != null && !tipoContratto.isBlank()) {
            sql.append(" AND a.tipo_contratto = ?");
            parametri.add(tipoContratto.trim().toUpperCase());
        }

        sql.append(" ORDER BY a.data_pubblicazione DESC, a.id DESC");

        return jdbcTemplate.query(
            sql.toString(),
            (rs, rowNum) -> new AnnuncioCardDTO(
                rs.getLong("id"),
                rs.getString("titolo"),
                rs.getBigDecimal("prezzo"),
                rs.getBigDecimal("prezzo_precedente"),
                rs.getBigDecimal("superficie_mq"),
                rs.getString("citta"),
                rs.getString("categoria"),
                rs.getString("tipo_contratto"),
                rs.getString("foto_principale")
            ),
            parametri.toArray()
        );
    }
}