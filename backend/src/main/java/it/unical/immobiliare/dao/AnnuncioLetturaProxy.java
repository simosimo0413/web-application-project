package it.unical.immobiliare.dao;

import it.unical.immobiliare.dto.AnnuncioCardDTO;
import it.unical.immobiliare.dto.AnnuncioDettaglioDTO;
import it.unical.immobiliare.dto.OrdinamentoAnnunci;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Smart Proxy del DAO di lettura: misura la durata delle interrogazioni.
 * Il controller usa questo Proxy; le query JDBC restano nel DAO reale.
 * Non memorizza i dati e non registra parametri o dati personali nei log.
 */
@Component
@Primary
public class AnnuncioLetturaProxy implements AnnuncioLetturaDAO {

    private static final Logger LOG = LoggerFactory.getLogger(AnnuncioLetturaProxy.class);
    private static final long SOGLIA_LENTA_MS = 250;

    private final AnnuncioDAO daoReale;

    public AnnuncioLetturaProxy(AnnuncioDAO daoReale) {
        this.daoReale = daoReale;
    }

    @Override
    public List<AnnuncioCardDTO> trovaAnnunciAttivi(
            String citta,
            BigDecimal prezzoMax,
            String categoria,
            String tipoContratto,
            OrdinamentoAnnunci ordinamento) {
        return misura("trovaAnnunciAttivi", () ->
                daoReale.trovaAnnunciAttivi(citta, prezzoMax, categoria, tipoContratto, ordinamento));
    }

    @Override
    public Optional<AnnuncioDettaglioDTO> trovaAnnuncioPerId(Long id) {
        return misura("trovaAnnuncioPerId", () -> daoReale.trovaAnnuncioPerId(id));
    }

    private <T> T misura(String nomeOperazione, Supplier<T> operazione) {
        long inizio = System.nanoTime();
        try {
            return operazione.get();
        } finally {
            long durataMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inizio);
            if (durataMs >= SOGLIA_LENTA_MS) {
                LOG.warn("Query annunci lenta: {} ha richiesto {} ms", nomeOperazione, durataMs);
            } else {
                LOG.debug("Query annunci: {} ha richiesto {} ms", nomeOperazione, durataMs);
            }
        }
    }
}
