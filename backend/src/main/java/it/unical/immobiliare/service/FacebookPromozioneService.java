package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.FacebookPromozioneDAO;
import it.unical.immobiliare.dao.FacebookPromozioneDAO.AnnuncioPromuovibile;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/** Pubblica messaggi sulla Pagina Facebook configurata tramite Meta Graph API.
 * Nessun token viene inviato al browser, stampato nei log o salvato nel database.
 */
@Service
public class FacebookPromozioneService {
    private static final String GRAPH_API = "https://graph.facebook.com/v26.0/";

    private final FacebookPromozioneDAO dao;
    private final RestClient client;
    private final String pageId;
    private final String pageToken;
    private final String publicBaseUrl;

    public FacebookPromozioneService(
            FacebookPromozioneDAO dao,
            @Value("${FACEBOOK_PAGE_ID:}") String pageId,
            @Value("${FACEBOOK_PAGE_ACCESS_TOKEN:}") String pageToken,
            @Value("${FACEBOOK_PUBLIC_BASE_URL:}") String publicBaseUrl) {
        this.dao = dao;
        this.pageId = pageId.trim();
        this.pageToken = pageToken.trim();
        this.publicBaseUrl = publicBaseUrl.trim();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(12));
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    public record StatoFacebook(boolean configurato, String urlPubblico) {}
    public record EsitoPubblicazione(String postId) {}

    /** Verifica anche che chi richiede lo stato sia proprietario dell'annuncio. */
    public StatoFacebook stato(Long annuncioId, Authentication auth) {
        verificaProprietario(annuncioId, auth);
        return new StatoFacebook(configurato(), urlPubblico(annuncioId).orElse(null));
    }

    public EsitoPubblicazione pubblica(Long annuncioId, Authentication auth) {
        AnnuncioPromuovibile annuncio = verificaProprietario(annuncioId, auth);
        if (!configurato()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Promozione Facebook non configurata");
        }

        String messaggio = "Annuncio immobiliare: " + annuncio.titolo() + "\n"
                + "Citta: " + annuncio.citta() + "\n"
                + "Contratto: " + annuncio.tipoContratto() + "\n"
                + "Prezzo: " + annuncio.prezzo().toPlainString() + " EUR\n"
                + "Superficie: " + annuncio.superficieMq().toPlainString() + " m²";

        MultiValueMap<String, String> campi = new LinkedMultiValueMap<>();
        campi.add("access_token", pageToken);
        campi.add("message", messaggio);
        urlPubblico(annuncioId).ifPresent(url -> campi.add("link", url));

        try {
            @SuppressWarnings("rawtypes")
            Map risultato = client.post()
                    .uri(GRAPH_API + pageId + "/feed")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(campi)
                    .retrieve()
                    .body(Map.class);

            Object id = risultato == null ? null : risultato.get("id");
            if (!(id instanceof String valore) || valore.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Facebook non ha confermato la pubblicazione");
            }
            return new EsitoPubblicazione(valore);
        } catch (RestClientException exception) {
            // Non esporre risposte Meta: potrebbero contenere informazioni riservate.
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Pubblicazione Facebook non riuscita: verifica i permessi della Pagina");
        }
    }

    private boolean configurato() {
        return pageId.matches("[0-9]{4,30}") && !pageToken.isBlank();
    }

    private AnnuncioPromuovibile verificaProprietario(Long id, Authentication auth) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID annuncio non valido");
        }
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream()
                .noneMatch(a -> "ROLE_VENDITORE".equals(a.getAuthority()))) {
            throw new AccessDeniedException("Accesso riservato ai venditori");
        }
        return dao.trovaAttivoDelVenditore(id, auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Annuncio attivo del venditore non trovato"));
    }

    /** Non condividere link localhost o indirizzi HTTP: Facebook non potrebbe aprirli. */
    private Optional<String> urlPubblico(Long id) {
        if (publicBaseUrl.isBlank()) return Optional.empty();
        try {
            URI base = URI.create(publicBaseUrl);
            if (!"https".equalsIgnoreCase(base.getScheme()) || base.getHost() == null
                    || base.getHost().equalsIgnoreCase("localhost")
                    || base.getHost().equals("127.0.0.1")
                    || base.getRawUserInfo() != null || base.getRawQuery() != null
                    || base.getRawFragment() != null) {
                return Optional.empty();
            }
            return Optional.of(publicBaseUrl.replaceAll("/+$", "") + "/annunci/" + id);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
