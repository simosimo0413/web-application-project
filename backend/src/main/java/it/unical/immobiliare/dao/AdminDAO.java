package it.unical.immobiliare.dao;

import it.unical.immobiliare.dto.AdminAnnuncioDTO;
import it.unical.immobiliare.dto.AdminUtenteDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdminDAO {
    private final JdbcTemplate jdbc;

    public AdminDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<AdminUtenteDTO> tuttiGliUtenti() {
        return jdbc.query("""
                SELECT id, nome, cognome, email, ruolo, bannato
                FROM utenti ORDER BY id
                """, (rs, n) -> new AdminUtenteDTO(
                rs.getLong("id"), rs.getString("nome"), rs.getString("cognome"),
                rs.getString("email"), rs.getString("ruolo"), rs.getBoolean("bannato")));
    }

    public List<AdminAnnuncioDTO> tuttiGliAnnunci() {
        return jdbc.query("""
                SELECT a.id, a.titolo, u.email AS venditore_email, a.prezzo, a.stato
                FROM annunci a JOIN utenti u ON u.id = a.venditore_id
                ORDER BY a.id DESC
                """, (rs, n) -> new AdminAnnuncioDTO(
                rs.getLong("id"), rs.getString("titolo"),
                rs.getString("venditore_email"), rs.getBigDecimal("prezzo"),
                rs.getString("stato")));
    }

    /** Per sicurezza gli account ADMIN non possono essere bannati da questa schermata. */
    public int cambiaBan(Long id, boolean bannato) {
        return jdbc.update("UPDATE utenti SET bannato = ? WHERE id = ? AND ruolo <> 'ADMIN'", bannato, id);
    }

    public int promuovi(Long id) {
        return jdbc.update("""
                UPDATE utenti SET ruolo = 'ADMIN'
                WHERE id = ? AND bannato = FALSE AND ruolo IN ('VENDITORE', 'ACQUIRENTE')
                """, id);
    }
}
