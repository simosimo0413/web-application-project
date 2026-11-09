package it.unical.immobiliare.controller;

import it.unical.immobiliare.dto.LoginRequest;
import it.unical.immobiliare.dto.RegistrazioneRequest;
import it.unical.immobiliare.model.Utente;
import it.unical.immobiliare.service.LoginService;
import it.unical.immobiliare.service.RegistrazioneService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrazioneService registrazioneService;
    private final LoginService loginService;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(
            RegistrazioneService registrazioneService,
            LoginService loginService) {

        this.registrazioneService = registrazioneService;
        this.loginService = loginService;
    }

    @PostMapping("/registrazione")
    public ResponseEntity<Map<String, Object>> registrazione(
            @Valid @RequestBody RegistrazioneRequest richiesta) {

        try {
            Long id = registrazioneService.registra(richiesta);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "id", id,
                            "messaggio", "Registrazione completata"
                    ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest richiesta,
            HttpServletRequest request,
            HttpServletResponse response) {

        Utente utente;

        try {
            utente = loginService.verificaCredenziali(
                    richiesta.email(),
                    richiesta.password()
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("errore", "Credenziali non valide"));
        }

        var autorita = List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + utente.getRuolo()
                )
        );

        var autenticazione =
                UsernamePasswordAuthenticationToken.authenticated(
                        utente.getEmail(),
                        null,
                        autorita
                );

        request.getSession(true);
        request.changeSessionId();

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(autenticazione);

        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                request,
                response
        );

        return ResponseEntity.ok(Map.of(
                "id", utente.getId(),
                "nome", utente.getNome(),
                "ruolo", utente.getRuolo(),
                "messaggio", "Accesso effettuato"
        ));
    }
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> utenteCorrente(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("errore", "Utente non autenticato"));
        }

        return ResponseEntity.ok(Map.of(
                "email", authentication.getName(),
                "ruolo", authentication.getAuthorities().iterator().next().getAuthority()
        ));
    }
}