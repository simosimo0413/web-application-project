package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.RecensioneDAO;
import it.unical.immobiliare.dto.RecensioneRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RecensioneServiceTest {

    private static final String EMAIL = "acquirente.test@example.com";
    private RecensioneDAO recensioni;
    private RecensioneService service;

    @BeforeEach
    void inizializza() {
        recensioni = mock(RecensioneDAO.class);
        service = new RecensioneService(recensioni);
    }

    private Authentication autenticato(String ruolo) {
        return UsernamePasswordAuthenticationToken.authenticated(
                EMAIL, null, List.of(new SimpleGrantedAuthority("ROLE_" + ruolo)));
    }

    @Test
    void venditoreNonPuoRecensire() {
        assertThrows(AccessDeniedException.class, () ->
                service.pubblicaRecensione(3L, new RecensioneRequest(5, "Ottimo"),
                        autenticato("VENDITORE")));
        verifyNoInteractions(recensioni);
    }

    @Test
    void votoFuoriIntervalloVieneRifiutato() {
        ResponseStatusException errore = assertThrows(ResponseStatusException.class, () ->
                service.pubblicaRecensione(3L, new RecensioneRequest(6, "Ottimo"),
                        autenticato("ACQUIRENTE")));
        assertEquals(HttpStatus.BAD_REQUEST, errore.getStatusCode());
        verifyNoInteractions(recensioni);
    }

    @Test
    void commentoVuotoVieneRifiutato() {
        ResponseStatusException errore = assertThrows(ResponseStatusException.class, () ->
                service.pubblicaRecensione(3L, new RecensioneRequest(4, "  "),
                        autenticato("ACQUIRENTE")));
        assertEquals(HttpStatus.BAD_REQUEST, errore.getStatusCode());
        verifyNoInteractions(recensioni);
    }

    @Test
    void visitaNonRecensibileVieneRifiutata() {
        when(recensioni.visitaRecensibile(3L, EMAIL)).thenReturn(false);
        ResponseStatusException errore = assertThrows(ResponseStatusException.class, () ->
                service.pubblicaRecensione(3L, new RecensioneRequest(4, "Buona visita"),
                        autenticato("ACQUIRENTE")));
        assertEquals(HttpStatus.NOT_FOUND, errore.getStatusCode());
        verify(recensioni, never()).inserisciRecensione(any(), any(), any());
    }

    @Test
    void recensioneDuplicataVieneRifiutata() {
        when(recensioni.visitaRecensibile(3L, EMAIL)).thenReturn(true);
        when(recensioni.esisteRecensione(3L)).thenReturn(true);
        ResponseStatusException errore = assertThrows(ResponseStatusException.class, () ->
                service.pubblicaRecensione(3L, new RecensioneRequest(4, "Buona visita"),
                        autenticato("ACQUIRENTE")));
        assertEquals(HttpStatus.CONFLICT, errore.getStatusCode());
        verify(recensioni, never()).inserisciRecensione(any(), any(), any());
    }

    @Test
    void recensioneValidaVieneInoltrataAlDaoConCommentoNormalizzato() {
        when(recensioni.visitaRecensibile(3L, EMAIL)).thenReturn(true);
        when(recensioni.esisteRecensione(3L)).thenReturn(false);
        when(recensioni.inserisciRecensione(3L, 5, "Ottima visita"))
                .thenReturn(8L);

        Long id = service.pubblicaRecensione(3L,
                new RecensioneRequest(5, "  Ottima visita  "),
                autenticato("ACQUIRENTE"));

        assertEquals(8L, id);
        verify(recensioni).inserisciRecensione(3L, 5, "Ottima visita");
    }
}
