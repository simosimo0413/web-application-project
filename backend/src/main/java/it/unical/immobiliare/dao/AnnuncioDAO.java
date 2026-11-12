package it.unical.immobiliare.dao;

import it.unical.immobiliare.dto.AnnuncioCardDTO;
import it.unical.immobiliare.dto.OrdinamentoAnnunci;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import it.unical.immobiliare.dto.AnnuncioDettaglioDTO;
import it.unical.immobiliare.dto.PubblicazioneAnnuncioRequest;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Optional;

@Repository
public class AnnuncioDAO implements AnnuncioLetturaDAO {

    private final JdbcTemplate jdbcTemplate;

    public AnnuncioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AnnuncioCardDTO> trovaAnnunciAttivi(
            String citta,
            BigDecimal prezzoMax,
            String categoria,
            String tipoContratto,
            OrdinamentoAnnunci ordinamento) {

        StringBuilder sql = new StringBuilder("""
            SELECT
                a.id,
                a.venditore_id,
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

        // Ordinamento consentito solo attraverso valori enumerati.
        String orderBy = switch (ordinamento) {
            case PREZZO_ASC -> "a.prezzo ASC, a.id DESC";
            case PREZZO_DESC -> "a.prezzo DESC, a.id DESC";
            case SUPERFICIE_ASC -> "i.superficie_mq ASC, a.id DESC";
            case SUPERFICIE_DESC -> "i.superficie_mq DESC, a.id DESC";
            case RECENTI -> "a.data_pubblicazione DESC, a.id DESC";
        };
        sql.append(" ORDER BY ").append(orderBy);

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
    
    @Override
    public Optional<AnnuncioDettaglioDTO> trovaAnnuncioPerId(Long id) {

        String sql = """
            SELECT
                a.id,
                a.venditore_id,
                a.titolo,
                i.descrizione,
                a.prezzo,
                a.prezzo_precedente,
                i.superficie_mq,
                i.indirizzo,
                i.citta,
                i.provincia,
                i.latitudine,
                i.longitudine,
                c.nome AS categoria,
                a.tipo_contratto,
                f.url AS foto_principale
            FROM annunci a
            JOIN immobili i ON a.immobile_id = i.id
            JOIN categorie c ON i.categoria_id = c.id
            LEFT JOIN foto f
                ON f.immobile_id = i.id
                AND f.principale = TRUE
            WHERE a.id = ?
            AND a.stato = 'ATTIVO'
            """;
    
        try {
            AnnuncioDettaglioDTO dettaglio = jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new AnnuncioDettaglioDTO(
                    rs.getLong("id"),
                    rs.getString("titolo"),
                    rs.getString("descrizione"),
                    rs.getBigDecimal("prezzo"),
                    rs.getBigDecimal("prezzo_precedente"),
                    rs.getBigDecimal("superficie_mq"),
                    rs.getString("indirizzo"),
                    rs.getString("citta"),
                    rs.getString("provincia"),
                    rs.getString("categoria"),
                    rs.getString("tipo_contratto"),
                    rs.getString("foto_principale"),
                    rs.getLong("venditore_id"),
                    rs.getBigDecimal("latitudine"),
                    rs.getBigDecimal("longitudine")
                ),
                id
            );
    
            return Optional.ofNullable(dettaglio);
    
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    public Long inserisciAnnuncio(
        Long immobileId,
        Long venditoreId,
        PubblicazioneAnnuncioRequest richiesta) {

    String sql = """
        INSERT INTO annunci (
            immobile_id,
            venditore_id,
            titolo,
            tipo_contratto,
            prezzo
        )
        VALUES (?, ?, ?, ?, ?)
        RETURNING id
        """;

    return jdbcTemplate.queryForObject(
        sql,
        Long.class,
        immobileId,
        venditoreId,
        richiesta.titolo(),
        richiesta.tipoContratto(),
        richiesta.prezzo()
        );
    }
    public Optional<Long> trovaImmobileIdSeProprietario(
        Long annuncioId,
        String emailVenditore) {

    String sql = """
        SELECT a.immobile_id
        FROM annunci a
        JOIN utenti u ON a.venditore_id = u.id
        WHERE a.id = ?
        AND u.email = ?
        AND a.stato = 'ATTIVO'
        """;

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) -> rs.getLong("immobile_id"),
        annuncioId,
        emailVenditore
    ).stream().findFirst();
}

    public record AnnuncioGestibile(Long immobileId, BigDecimal prezzo) {}

    /** Blocca l'annuncio durante un'operazione del proprietario, evitando ribassi concorrenti. */
    public Optional<AnnuncioGestibile> bloccaAttivoSeProprietario(Long annuncioId, String email) {
        String sql = """
                SELECT a.immobile_id, a.prezzo
                FROM annunci a
                JOIN utenti u ON u.id = a.venditore_id
                WHERE a.id = ? AND LOWER(u.email) = LOWER(?) AND a.stato = 'ATTIVO'
                FOR UPDATE OF a
                """;
        return jdbcTemplate.query(sql,
                (rs, n) -> new AnnuncioGestibile(
                        rs.getLong("immobile_id"), rs.getBigDecimal("prezzo")),
                annuncioId, email).stream().findFirst();
    }

    /** Un amministratore puo bloccare e gestire qualsiasi annuncio attivo. */
    public Optional<AnnuncioGestibile> bloccaAttivoPerId(Long annuncioId) {
        return jdbcTemplate.query("""
                SELECT immobile_id, prezzo FROM annunci
                WHERE id = ? AND stato = 'ATTIVO'
                FOR UPDATE
                """, (rs, n) -> new AnnuncioGestibile(
                rs.getLong("immobile_id"), rs.getBigDecimal("prezzo")), annuncioId)
                .stream().findFirst();
    }

    public void aggiornaTitolo(Long annuncioId, String titolo) {
        jdbcTemplate.update("UPDATE annunci SET titolo = ? WHERE id = ?", titolo, annuncioId);
    }

    public void ribassaPrezzo(Long annuncioId, BigDecimal nuovoPrezzo) {
        jdbcTemplate.update("""
                UPDATE annunci
                SET prezzo_precedente = prezzo, prezzo = ?
                WHERE id = ? AND stato = 'ATTIVO'
                """, nuovoPrezzo, annuncioId);
    }

    /** Non chiudere un annuncio che ha un'asta programmata o in corso. */
    public boolean haAstaNonTerminata(Long annuncioId) {
        Boolean esiste = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM aste
                    WHERE annuncio_id = ? AND fine > CURRENT_TIMESTAMP
                )
                """, Boolean.class, annuncioId);
        return Boolean.TRUE.equals(esiste);
    }

    public void chiudiAnnuncio(Long annuncioId) {
        jdbcTemplate.update("UPDATE annunci SET stato = 'CHIUSO' WHERE id = ? AND stato = 'ATTIVO'", annuncioId);
    }

}