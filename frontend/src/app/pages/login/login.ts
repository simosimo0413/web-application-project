
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, LoginRequest } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {

  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  dati: LoginRequest = {
    email: '',
    password: ''
  };

  caricamento = false;
  successo = false;
  errore = '';

  nomeUtente = '';
  ruoloUtente = '';

  accedi(): void {
    if (this.caricamento) {
      return;
    }

    this.caricamento = true;
    this.successo = false;
    this.errore = '';

    this.authService.login(this.dati).subscribe({
      next: (risposta) => {
        this.caricamento = false;
        this.successo = true;

        this.nomeUtente = risposta.nome;
        this.ruoloUtente = risposta.ruolo;

        this.dati.password = '';

        this.cdr.markForCheck();
      },
      error: (err) => {
        this.caricamento = false;
        this.dati.password = '';

        if (err.status === 401) {
          this.errore = 'Email o password non corretti.';
        } else if (err.status === 403) {
          this.errore = 'Accesso non autorizzato. Verificare la protezione CSRF.';
        } else {
          this.errore = 'Accesso non riuscito. Riprova più tardi.';
        }

        this.cdr.markForCheck();
      }
    });
  }
}
