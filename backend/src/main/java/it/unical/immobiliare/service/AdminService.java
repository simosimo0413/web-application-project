package it.unical.immobiliare.service;

import it.unical.immobiliare.dao.AdminDAO;
import it.unical.immobiliare.dao.UtenteDAO;
import it.unical.immobiliare.dto.AdminAnnuncioDTO;
import it.unical.immobiliare.dto.AdminUtenteDTO;
import it.unical.immobiliare.model.Utente;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminService {
    private final AdminDAO adminDAO;
    private final UtenteDAO utenteDAO;

    public AdminService(AdminDAO adminDAO, UtenteDAO utenteDAO) {
        this.adminDAO = adminDAO;
        this.utenteDAO = utenteDAO;
    }

    private Utente adminAutorizzato(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() ||
                auth.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new AccessDeniedException("Solo un amministratore puo eseguire questa operazione");
        }
        Utente admin = utenteDAO.trovaPerEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Account inesistente"));
        if (admin.isBannato() || !"ADMIN".equals(admin.getRuolo())) {
            throw new AccessDeniedException("Account non autorizzato");
        }
        return admin;
    }

    public List<AdminUtenteDTO> utenti(Authentication auth) {
        adminAutorizzato(auth);
        return adminDAO.tuttiGliUtenti();
    }

    public List<AdminAnnuncioDTO> annunci(Authentication auth) {
        adminAutorizzato(auth);
        return adminDAO.tuttiGliAnnunci();
    }

    @Transactional
    public void impostaBan(Long id, boolean bannato, Authentication auth) {
        Utente admin = adminAutorizzato(auth);
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID non valido");
        }
        if (admin.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Non puoi bannare te stesso");
        }
        if (adminDAO.cambiaBan(id, bannato) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Utente inesistente oppure amministratore protetto");
        }
    }

    @Transactional
    public void promuovi(Long id, Authentication auth) {
        adminAutorizzato(auth);
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID non valido");
        }
        if (adminDAO.promuovi(id) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Utente inesistente, bannato o gia amministratore");
        }
    }
}
