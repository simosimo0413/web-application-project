
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import {
  ContattoService,
  ContattoRicevuto
} from '../../services/contatto';

@Component({
  selector: 'app-messaggi-ricevuti',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './messaggi-ricevuti.html',
  styleUrl: './messaggi-ricevuti.css'
})
export class MessaggiRicevuti implements OnInit {

  private contattoService = inject(ContattoService);
  private cdr = inject(ChangeDetectorRef);

  messaggi: ContattoRicevuto[] = [];

  caricamento = true;
  errore = '';

  ngOnInit(): void {
    this.caricaMessaggi();
  }

  caricaMessaggi(): void {
    this.caricamento = true;
    this.errore = '';

    this.contattoService.recuperaMessaggiRicevuti().subscribe({
      next: (risposta) => {
        this.messaggi = risposta;
        this.caricamento = false;

        this.cdr.markForCheck();
      },

      error: (err) => {
        console.error('Errore caricamento messaggi:', err);

        this.caricamento = false;

        if (err.status === 401 || err.status === 403) {
          this.errore =
            'Accesso riservato ai venditori autenticati.';
        } else {
          this.errore =
            'Non è stato possibile recuperare i messaggi.';
        }

        this.cdr.markForCheck();
      }
    });
  }
}
