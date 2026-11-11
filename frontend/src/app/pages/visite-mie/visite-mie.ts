import {
    ChangeDetectorRef,
    Component,
    OnInit,
    inject
  } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { VisitaAcquirente } from '../../models/visita-acquirente';
import { VisiteAcquirenteService } from '../../services/visite-acquirente';
import { RecensioneService } from '../../services/recensione';
import { FormsModule } from '@angular/forms';

  @Component({
    selector: 'app-visite-mie',
    standalone: true,
    imports: [CommonModule, RouterLink, FormsModule],
    templateUrl: './visite-mie.html',
    styleUrl: './visite-mie.css'
  })
  export class VisiteMie implements OnInit {

    private visiteService = inject(VisiteAcquirenteService);
    private recensioneService = inject(RecensioneService);
    private cdr = inject(ChangeDetectorRef);

    visite: VisitaAcquirente[] = [];
    caricamento = true;
    errore = '';
    visitaSelezionataId: number | null = null;

    votoRecensione = 0;
    commentoRecensione = '';

    invioRecensioneInCorso = false;
    erroreRecensione = '';
    successoRecensione = '';

    ngOnInit(): void {
      this.visiteService.recuperaLeMieVisite().subscribe({
        next: (risposta) => {
          this.visite = risposta;
          this.caricamento = false;
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Errore caricamento visite:', err);

          this.caricamento = false;

          if (err.status === 401 || err.status === 403) {
            this.errore =
              'Devi accedere come acquirente per visualizzare le tue visite.';
          } else {
            this.errore =
              'Impossibile caricare le visite. Riprova più tardi.';
          }

          this.cdr.markForCheck();
        }
    });

    }
    apriModuloRecensione(visita: VisitaAcquirente): void {

        if (!visita.recensibile || this.invioRecensioneInCorso) {
            return;
        }
        this.visitaSelezionataId = visita.id;
        this.votoRecensione = 0;
        this.commentoRecensione = '';
        this.erroreRecensione = '';
        this.successoRecensione = '';
    }

    chiudiModuloRecensione(): void {

    if (this.invioRecensioneInCorso) {
        return;
    }

    this.visitaSelezionataId = null;
    this.votoRecensione = 0;
    this.commentoRecensione = '';
    this.erroreRecensione = '';
    this.successoRecensione = '';
    }

    inviaRecensione(): void {
        if (this.invioRecensioneInCorso) {
          return;
        }

        this.erroreRecensione = '';
        this.successoRecensione = '';

        const visitaId = this.visitaSelezionataId;

        const visita = this.visite.find(
          elemento => elemento.id === visitaId
        );

        if (!visita || !visita.recensibile) {
          this.erroreRecensione =
            'Questa visita non può essere recensita.';
          return;
        }

        if (
          !Number.isInteger(this.votoRecensione) ||
          this.votoRecensione < 1 ||
          this.votoRecensione > 5
        ) {
          this.erroreRecensione =
            'Seleziona un voto da 1 a 5 stelle.';
          return;
        }

        const commento = this.commentoRecensione.trim();

        if (!commento || commento.length > 5000) {
          this.erroreRecensione =
            'Inserisci un commento da 1 a 5000 caratteri.';
          return;
        }

        this.invioRecensioneInCorso = true;

        this.recensioneService.pubblicaRecensione(
          visita.id,
          this.votoRecensione,
          commento
        ).subscribe({

          next: () => {
            this.invioRecensioneInCorso = false;

            this.visite = this.visite.map(elemento =>
              elemento.id === visita.id
                ? {
                    ...elemento,
                    recensita: true,
                    recensibile: false
                  }
                : elemento
            );

            this.visitaSelezionataId = null;
            this.votoRecensione = 0;
            this.commentoRecensione = '';

            this.successoRecensione =
              'Recensione pubblicata con successo!';

            this.cdr.markForCheck();
          },

          error: (err) => {
            console.error('Errore pubblicazione recensione:', err);

            this.invioRecensioneInCorso = false;

            if (err.status === 400) {
              this.erroreRecensione =
                'Controlla il voto e il commento inseriti.';
            } else if (err.status === 401 || err.status === 403) {
              this.erroreRecensione =
                'Devi accedere come acquirente per pubblicare una recensione.';
            } else if (err.status === 404) {
              this.erroreRecensione =
                'Visita non trovata o non ancora recensibile.';
            } else if (err.status === 409) {
              this.erroreRecensione =
                'Questa visita è già stata recensita.';
            } else {
              this.erroreRecensione =
                'Impossibile pubblicare la recensione. Riprova.';
            }

            this.cdr.markForCheck();
          }
        });
      }

}