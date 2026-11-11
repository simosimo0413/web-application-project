
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

import {
  VisitaService,
  VisitaRicevuta
} from '../../services/visita';

@Component({
  selector: 'app-visite-ricevute',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './visite-ricevute.html',
  styleUrl: './visite-ricevute.css'
})
export class VisiteRicevute implements OnInit {

  private readonly visitaService = inject(VisitaService);
  private readonly cdr = inject(ChangeDetectorRef);

  visite: VisitaRicevuta[] = [];

  caricamento = true;
  erroreCaricamento = '';

  visitaInConfermaId: number | null = null;

  erroreConferma = '';
  erroreConfermaId: number | null = null;
  messaggioConferma = '';

  ngOnInit(): void {
    this.caricaVisite();
  }

  caricaVisite(): void {
    this.caricamento = true;
    this.erroreCaricamento = '';

    this.visitaService.recuperaVisiteRicevute().subscribe({
      next: (risposta) => {
        this.visite = risposta;
        this.caricamento = false;

        this.cdr.markForCheck();
      },

      error: (err: HttpErrorResponse) => {
        console.error('Errore caricamento visite:', err);

        this.caricamento = false;

        if (err.status === 401 || err.status === 403) {
          this.erroreCaricamento =
            'Devi accedere come VENDITORE per visualizzare le visite.';
        } else {
          this.erroreCaricamento =
            'Impossibile caricare le visite ricevute.';
        }

        this.cdr.markForCheck();
      }
    });
  }

  confermaVisita(visita: VisitaRicevuta): void {

    if (
      visita.confermata ||
      this.visitaInConfermaId !== null ||
      this.visitaPassata(visita.dataVisita)
    ) {
      return;
    }

    this.visitaInConfermaId = visita.id;

    this.erroreConferma = '';
    this.erroreConfermaId = null;
    this.messaggioConferma = '';

    this.visitaService.confermaVisita(visita.id).subscribe({
      next: () => {

        // Aggiorniamo lo stato della visita nell'interfaccia.
        this.visite = this.visite.map(v =>
          v.id === visita.id
            ? { ...v, confermata: true }
            : v
        );

        this.visitaInConfermaId = null;

        this.messaggioConferma =
          `La visita #${visita.id} è stata confermata!`;

        this.cdr.markForCheck();
      },

      error: (err: HttpErrorResponse) => {
        console.error('Errore conferma visita:', err);

        this.visitaInConfermaId = null;
        this.erroreConfermaId = visita.id;

        if (err.status === 401 || err.status === 403) {
          this.erroreConferma =
            'Non sei autorizzato a confermare questa visita.';
        } else if (err.status === 404) {
          this.erroreConferma =
            'La visita non è disponibile o non può più essere confermata.';
        } else {
          this.erroreConferma =
            'Impossibile confermare la visita. Riprova.';
        }

        this.cdr.markForCheck();
      }
    });
  }

  visitaPassata(dataVisita: string): boolean {
    return new Date(dataVisita).getTime() <= Date.now();
  }
}
