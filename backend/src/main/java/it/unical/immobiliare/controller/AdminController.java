package it.unical.immobiliare.controller;

import it.unical.immobiliare.dto.AdminAnnuncioDTO;
import it.unical.immobiliare.dto.AdminBanRequest;
import it.unical.immobiliare.dto.AdminUtenteDTO;
import it.unical.immobiliare.service.AdminService;
import it.unical.immobiliare.service.GestioneAnnuncioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;
    private final GestioneAnnuncioService gestione;

    public AdminController(AdminService admin, GestioneAnnuncioService gestione) {
        this.admin = admin;
        this.gestione = gestione;
    }

    @GetMapping("/utenti")
    public List<AdminUtenteDTO> utenti(Authentication auth) {
        return admin.utenti(auth);
    }

    @PatchMapping("/utenti/{id}/ban")
    public ResponseEntity<Void> ban(@PathVariable Long id, @Valid @RequestBody AdminBanRequest request,
                                    Authentication auth) {
        admin.impostaBan(id, request.bannato(), auth);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/utenti/{id}/promuovi")
    public ResponseEntity<Void> promuovi(@PathVariable Long id, Authentication auth) {
        admin.promuovi(id, auth);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/annunci")
    public List<AdminAnnuncioDTO> annunci(Authentication auth) {
        return admin.annunci(auth);
    }

    @PatchMapping("/annunci/{id}/chiudi")
    public ResponseEntity<Void> chiudi(@PathVariable Long id, Authentication auth) {
        // Stessi controlli su aste e transazione della gestione venditore.
        gestione.chiudi(id, auth);
        return ResponseEntity.noContent().build();
    }
}
