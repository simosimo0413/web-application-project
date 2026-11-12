package it.unical.immobiliare.dao;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Legge solo annunci ATTIVI posseduti dal venditore autenticato. */
@Repository
public class FacebookPromozioneDAO {
    private final JdbcTemplate jdbcTemplate;

    public FacebookPromozioneDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record AnnuncioPromuovibile(
            Long id, String titolo, String citta, String tipoContratto,
            BigDecimal prezzo, BigDecimal superficieMq) {}

    public Optional<AnnuncioPromuovibile> trovaAttivoDelVenditore(Long id, String email) {
        return jdbcTemplate.query("""
                SELECT a.id, a.titolo, a.tipo_contratto, a.prezzo,
                       i.citta, i.superficie_mq
                FROM annunci a
                JOIN immobili i ON i.id = a.immobile_id
                JOIN utenti u ON u.id = a.venditore_id
                WHERE a.id = ? AND a.stato = 'ATTIVO'
                  AND LOWER(u.email) = LOWER(?)
                  AND u.ruolo = 'VENDITORE' AND u.bannato = FALSE
                """, (rs, index) -> new AnnuncioPromuovibile(
                        rs.getLong("id"),
                        rs.getString("titolo"),
                        rs.getString("citta"),
                        rs.getString("tipo_contratto"),
                        rs.getBigDecimal("prezzo"),
                        rs.getBigDecimal("superficie_mq")),
                id, email).stream().findFirst();
    }
}
