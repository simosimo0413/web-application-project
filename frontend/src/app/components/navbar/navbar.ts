
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {

  readonly authService = inject(AuthService);

  readonly uscitaInCorso = signal(false);
  readonly erroreLogout = signal('');

  esci(): void {
    if (this.uscitaInCorso()) {
      return;
    }

    this.uscitaInCorso.set(true);
    this.erroreLogout.set('');

    this.authService.logout().subscribe({
      next: () => {
        this.uscitaInCorso.set(false);
      },
      error: () => {
        this.uscitaInCorso.set(false);
        this.erroreLogout.set(
          'Impossibile effettuare il logout. Riprova.'
        );
      }
    });
  }
}
