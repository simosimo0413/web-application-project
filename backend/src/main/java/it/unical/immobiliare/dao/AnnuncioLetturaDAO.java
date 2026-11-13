package it.unical.immobiliare.dao;

import it.unical.immobiliare.dto.AnnuncioCardDTO;
import it.unical.immobiliare.dto.AnnuncioDettaglioDTO;
import it.unical.immobiliare.dto.OrdinamentoAnnunci;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Contratto per la lettura degli annunci.
 * L'implementazione reale interroga PostgreSQL; un Proxy può aggiungere
 * comportamenti trasversali senza modificare il risultato delle query.
 */
public interface AnnuncioLetturaDAO {

    List<AnnuncioCardDTO> trovaAnnunciAttivi(
            String citta,
            BigDecimal prezzoMax,
            String categoria,
            String tipoContratto,
            OrdinamentoAnnunci ordinamento);

    Optional<AnnuncioDettaglioDTO> trovaAnnuncioPerId(Long id);
}
