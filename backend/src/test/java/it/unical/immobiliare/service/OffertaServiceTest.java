package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.OffertaDAO;
import it.unical.immobiliare.dao.UtenteDAO;
import it.unical.immobiliare.dto.OffertaRequest;
import it.unical.immobiliare.model.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OffertaServiceTest {

    private static final String EMAIL = "acquirente.test@example.com";
    private OffertaDAO offerte;
    private UtenteDAO utenti;
    private OffertaService service;

    @BeforeEach
    void inizializza() {
        offerte = mock(OffertaDAO.class);
        utenti = mock(UtenteDAO.class);
        service = new OffertaService(offerte, utenti);
    }

    private Authentication autenticato(String ruolo) {
        return UsernamePasswordAuthenticationToken.authenticated(
                EMAIL, null, List.of(new SimpleGrantedAuthority("ROLE_" + ruolo)));
    }

    private void preparaAcquirente(Long id) {
        Utente utente = new Utente();
        utente.setId(id);
        when(utenti.trovaPerEmail(EMAIL)).thenReturn(Optional.of(utente));
    }

    private void preparaAsta(Instant inizio, Instant fine) {
        when(offerte.bloccaAsta(3L)).thenReturn(Optional.of(
                new OffertaDAO.AstaBloccata(3L, 4L, 99L,
                        new BigDecimal("10000.00"), inizio, fine)));
    }

    @Test
    void venditoreNonPuoPresentareOfferte() {
        assertThrows(AccessDeniedException.class, () ->
                service.presentaOfferta(3L, new OffertaRequest(new BigDecimal("12000")),
                        autenticato("VENDITORE")));
        verifyNoInteractions(offerte, utenti);
    }

    @Test
    void importoZeroVieneRifiutato() {
        ResponseStatusException errore = assertThrows(ResponseStatusException.class, () ->
                service.presentaOfferta(3L, new OffertaRequest(BigDecimal.ZERO),
                        autenticato("ACQUIRENTE")));
        assertEquals(HttpStatus.BAD_REQUEST, errore.getStatusCode());
        verifyNoInteractions(offerte, utenti);
    }

    @Test
    void importoConTreDecimaliVieneRifiutato() {
        ResponseStatusException errore = assertThrows(ResponseStatusException.class, () ->
                service.presentaOfferta(3L, new OffertaRequest(new BigDecimal("12000.123")),
                        autenticato("ACQUIRENTE")));
        assertEquals(HttpStatus.BAD_REQUEST, errore.getStatusCode());
        verifyNoInteractions(offerte, utenti);
    }

    @Test
    void astaFuturaNonAccettaOfferte() {
        preparaAcquirente(7L);
        preparaAsta(Instant.now().plus(Duration.ofDays(2)),
                Instant.now().plus(Duration.ofDays(3)));

        ResponseStatusException errore = assertThrows(ResponseStatusException.class, () ->
                service.presentaOfferta(3L, new OffertaRequest(new BigDecimal("12000")),
                        autenticato("ACQUIRENTE")));
        assertEquals(HttpStatus.CONFLICT, errore.getStatusCode());
        verify(offerte, never()).inserisciOfferta(any(), any(), any());
    }

    @Test
    void astaTerminataNonAccettaOfferte() {
        preparaAcquirente(7L);
        preparaAsta(Instant.now().minus(Duration.ofDays(3)),
                Instant.now().minus(Duration.ofDays(2)));

        ResponseStatusException errore = assertThrows(ResponseStatusException.class, () ->
                service.presentaOfferta(3L, new OffertaRequest(new BigDecimal("12000")),
                        autenticato("ACQUIRENTE")));
        assertEquals(HttpStatus.CONFLICT, errore.getStatusCode());
        verify(offerte, never()).inserisciOfferta(any(), any(), any());
    }

    @Test
    void offertaPariAllaMassimaVieneRifiutata() {
        preparaAcquirente(7L);
        preparaAsta(Instant.now().minus(Duration.ofDays(1)),
                Instant.now().plus(Duration.ofDays(1)));
        when(offerte.trovaOffertaMassima(3L))
                .thenReturn(Optional.of(new BigDecimal("11000.00")));

        ResponseStatusException errore = assertThrows(ResponseStatusException.class, () ->
                service.presentaOfferta(3L, new OffertaRequest(new BigDecimal("11000.00")),
                        autenticato("ACQUIRENTE")));
        assertEquals(HttpStatus.CONFLICT, errore.getStatusCode());
        verify(offerte, never()).inserisciOfferta(any(), any(), any());
    }

    @Test
    void primaOffertaInferioreAlPrezzoBaseVieneRifiutata() {
        preparaAcquirente(7L);
        preparaAsta(Instant.now().minus(Duration.ofDays(1)),
                Instant.now().plus(Duration.ofDays(1)));
        when(offerte.trovaOffertaMassima(3L)).thenReturn(Optional.empty());

        ResponseStatusException errore = assertThrows(ResponseStatusException.class, () ->
                service.presentaOfferta(3L, new OffertaRequest(new BigDecimal("9999")),
                        autenticato("ACQUIRENTE")));
        assertEquals(HttpStatus.BAD_REQUEST, errore.getStatusCode());
        verify(offerte, never()).inserisciOfferta(any(), any(), any());
    }

    @Test
    void offertaValidaVieneInoltrataAlDao() {
        preparaAcquirente(7L);
        preparaAsta(Instant.now().minus(Duration.ofDays(1)),
                Instant.now().plus(Duration.ofDays(1)));
        when(offerte.trovaOffertaMassima(3L))
                .thenReturn(Optional.of(new BigDecimal("11000.00")));
        when(offerte.inserisciOfferta(3L, 7L, new BigDecimal("12000.00")))
                .thenReturn(42L);

        Long id = service.presentaOfferta(3L,
                new OffertaRequest(new BigDecimal("12000.00")),
                autenticato("ACQUIRENTE"));

        assertEquals(42L, id);
        verify(offerte).inserisciOfferta(3L, 7L, new BigDecimal("12000.00"));
    }
}
