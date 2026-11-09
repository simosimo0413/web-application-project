
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, RegistrazioneRequest } from '../../services/auth';

@Component({
  selector: 'app-registrazione',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './registrazione.html',
  styleUrl: './registrazione.css'
})
export class Registrazione {

  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  dati: RegistrazioneRequest = {
    nome: '',
    cognome: '',
    email: '',
    password: '',
    ruolo: 'ACQUIRENTE'
  };

  caricamento = false;
  successo = false;
  errore = '';

  registra(): void {
    if (this.caricamento) {
      return;
    }

    this.caricamento = true;
    this.successo = false;
    this.errore = '';

    this.authService.registra(this.dati).subscribe({
      next: () => {
        this.caricamento = false;
        this.successo = true;
        this.dati.password = '';
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.caricamento = false;

        if (err.status === 400) {
          this.errore =
            err.error?.errore || 'Controlla i dati inseriti.';
        } else if (err.status === 403) {
          this.errore =
            'Richiesta non autorizzata. Verificare la protezione CSRF.';
        } else {
          this.errore =
            'Registrazione non riuscita. Riprova più tardi.';
        }

        this.cdr.markForCheck();
      }
    });
  }
}
