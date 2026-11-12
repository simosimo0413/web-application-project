package it.unical.immobiliare.config;

import it.unical.immobiliare.dao.UtenteDAO;
import it.unical.immobiliare.model.Utente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/** Impedisce l'uso di sessioni gia aperte da account appena bannati o promossi. */
@Component
public class AccountSessionInterceptor implements HandlerInterceptor {
    private final UtenteDAO utenti;

    public AccountSessionInterceptor(UtenteDAO utenti) {
        this.utenti = utenti;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
            throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return true;
        }
        Utente u = utenti.trovaPerEmail(auth.getName()).orElse(null);
        boolean valido = u != null && !u.isBannato() && auth.getAuthorities().stream()
                .anyMatch(a -> ("ROLE_" + u.getRuolo()).equals(a.getAuthority()));
        if (valido) {
            return true;
        }
        SecurityContextHolder.clearContext();
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessione non piu valida");
        return false;
    }
}
