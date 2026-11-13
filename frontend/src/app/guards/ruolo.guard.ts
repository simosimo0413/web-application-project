import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth';

type Ruolo = 'ADMIN' | 'VENDITORE' | 'ACQUIRENTE';

/**
 * Protegge la navigazione usando la sessione verificata dal backend.
 * Non sostituisce i controlli di autorizzazione presenti in Spring Boot.
 */
export function richiediRuolo(ruoloRichiesto: Ruolo): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    // Controllo asincrono anche al refresh: il signal Angular può essere ancora vuoto.
    return auth.ripristinaSessione().pipe(
      map(sessione => sessione.ruolo.replace(/^ROLE_/, '') === ruoloRichiesto
        ? true
        : router.createUrlTree(['/'])),
      catchError(() => of(router.createUrlTree(['/login'])))
    );
  };
}
