
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../services/auth';
import {
  PubblicazioneAnnuncioService,
  PubblicazioneAnnuncioRequest
} from '../../services/pubblicazione-annuncio';

@Component({
  selector: 'app-pubblica-annuncio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pubblica-annuncio.html',
  styleUrl: './pubblica-annuncio.css'
})
export class PubblicaAnnuncio {

  readonly authService = inject(AuthService);

  private pubblicazioneService = inject(PubblicazioneAnnuncioService);
  private cdr = inject(ChangeDetectorRef);

  dati: PubblicazioneAnnuncioRequest = {
    titolo: '',
    tipoContratto: 'VENDITA',
    prezzo: 0,
    categoriaId: 1,
    superficieMq: 0,
    indirizzo: '',
    citta: '',
    provincia: '',
    cap: '',
    descrizione: ''
  };

  caricamento = false;
  successo = false;
  errore = '';
  annuncioId: number | null = null;

  get isVenditore(): boolean {
  const ruolo = this.authService.utenteCorrente()?.ruolo;

  return ruolo?.replace(/^ROLE_/, '') === 'VENDITORE';
}

  pubblica(): void {

    if (this.caricamento) {
      return;
    }

    if (!this.isVenditore) {
      this.errore = 'Solo i venditori possono pubblicare annunci.';
      return;
    }

    this.caricamento = true;
    this.successo = false;
    this.errore = '';

    this.pubblicazioneService.pubblica(this.dati).subscribe({
      next: (risposta) => {
        this.caricamento = false;
        this.successo = true;
        this.annuncioId = risposta.id;

        this.cdr.markForCheck();
      },
      error: (err) => {
        this.caricamento = false;

        if (err.status === 400) {
          this.errore = 'Controlla i dati inseriti nel modulo.';
        } else if (err.status === 401 || err.status === 403) {
          this.errore =
            'Pubblicazione non autorizzata. Verifica il tuo accesso.';
        } else {
          this.errore =
            'Impossibile pubblicare l\'annuncio. Riprova più tardi.';
        }

        this.cdr.markForCheck();
      }
    });
  }
}
