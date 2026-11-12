package it.unical.immobiliare.controller;

import it.unical.immobiliare.service.FacebookPromozioneService;
import it.unical.immobiliare.service.FacebookPromozioneService.EsitoPubblicazione;
import it.unical.immobiliare.service.FacebookPromozioneService.StatoFacebook;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/annunci/{annuncioId}/facebook")
public class FacebookPromozioneController {
    private final FacebookPromozioneService service;

    public FacebookPromozioneController(FacebookPromozioneService service) {
        this.service = service;
    }

    @GetMapping("/stato")
    public StatoFacebook stato(@PathVariable Long annuncioId, Authentication auth) {
        return service.stato(annuncioId, auth);
    }

    @PostMapping("/pubblica")
    public EsitoPubblicazione pubblica(@PathVariable Long annuncioId, Authentication auth) {
        return service.pubblica(annuncioId, auth);
    }
}
