
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { PrenotaVisita } from '../../components/prenota-visita/prenota-visita';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ContattoService } from '../../services/contatto';
import { AnnuncioDettaglio as AnnuncioDettaglioModel } from '../../models/annuncio-dettaglio';
import { AnnuncioService } from '../../services/annuncio';

import { Recensione } from '../../models/recensione';
import { RecensioneService } from '../../services/recensione';
import { FotoService, Foto } from '../../services/foto';

@Component({
  selector: 'app-annuncio-dettaglio',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule,PrenotaVisita],
  templateUrl: './annuncio-dettaglio.html',
  styleUrl: './annuncio-dettaglio.css'
})
export class AnnuncioDettaglio implements OnInit {
  private contattoService = inject(ContattoService);
  private recensioneService = inject(RecensioneService);

oggettoContatto = '';
messaggioContatto = '';

invioContattoInCorso = false;
contattoInviato = false;
erroreContatto = '';

  private route = inject(ActivatedRoute);
  private annuncioService = inject(AnnuncioService);
  private fotoService = inject(FotoService);
  private cdr = inject(ChangeDetectorRef);

  annuncio: AnnuncioDettaglioModel | null = null;

  caricamento = true;
  errore = false;

  foto: Foto[] = [];
  caricamentoFoto = false;
  erroreFoto = false;
  recensioni: Recensione[] = [];
  caricamentoRecensioni = false;
  erroreRecensioni = false;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isSafeInteger(id) || id <= 0) {
      this.caricamento = false;
      this.errore = true;
      return;
    }

    this.annuncioService.getAnnuncioPerId(id).subscribe({
      next: (dati) => {
        this.annuncio = dati;
        this.caricamento = false;

        this.caricaFoto(id);
        this.caricaRecensioni(id);
        this.cdr.markForCheck();

      },
      error: (err) => {
        console.error('Errore caricamento dettaglio:', err);
        this.errore = true;
        this.caricamento = false;
        this.cdr.markForCheck();
      }
    });
  }

  private caricaFoto(annuncioId: number): void {

    this.caricamentoFoto = true;
    this.erroreFoto = false;

    this.fotoService.recuperaFotoAnnuncio(annuncioId).subscribe({
      next: (risposta) => {
        this.foto = risposta;
        this.caricamentoFoto = false;

        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Errore caricamento fotografie:', err);
        this.erroreFoto = true;
        this.caricamentoFoto = false;

        this.cdr.markForCheck();
      }
    });
  }

  private caricaRecensioni(annuncioId: number): void {
    this.caricamentoRecensioni = true;
    this.erroreRecensioni = false;

    this.recensioneService
      .recuperaRecensioniAnnuncio(annuncioId)
      .subscribe({
        next: (risposta) => {
          this.recensioni = risposta;
          this.caricamentoRecensioni = false;

          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error(
            'Errore caricamento recensioni:',
            err
          );

          this.erroreRecensioni = true;
          this.caricamentoRecensioni = false;

          this.cdr.markForCheck();
        }
      });
  }

inviaContatto(): void {
  const annuncioId = Number(
    this.route.snapshot.paramMap.get('id')
  );

  if (
    !this.annuncio ||
    !Number.isSafeInteger(annuncioId) ||
    annuncioId <= 0 ||
    this.invioContattoInCorso
  ) {
    return;
  }

  const oggetto = this.oggettoContatto.trim();
  const messaggio = this.messaggioContatto.trim();

  this.erroreContatto = '';
  this.contattoInviato = false;

  if (!oggetto || !messaggio) {
    this.erroreContatto =
      'Inserisci sia l’oggetto sia il messaggio.';
    return;
  }

  if (oggetto.length > 255 || messaggio.length > 5000) {
    this.erroreContatto =
      'L’oggetto può contenere massimo 255 caratteri e il messaggio massimo 5000.';
    return;
  }

  this.invioContattoInCorso = true;

  this.contattoService.inviaContatto(
    annuncioId,
    { oggetto, messaggio }
  ).subscribe({
    next: () => {
      this.invioContattoInCorso = false;
      this.contattoInviato = true;

      this.oggettoContatto = '';
      this.messaggioContatto = '';

      this.cdr.markForCheck();
    },

    error: (err) => {
      console.error('Errore invio contatto:', err);

      this.invioContattoInCorso = false;

      if (err.status === 401 || err.status === 403) {
        this.erroreContatto =
          'Devi effettuare il login per inviare un messaggio.';
      } else if (err.status === 400) {
        this.erroreContatto =
          'Controlla i dati inseriti nel messaggio.';
      } else if (err.status === 404) {
        this.erroreContatto =
          'L’annuncio non è più disponibile.';
      } else {
        this.erroreContatto =
          'Impossibile inviare il messaggio. Riprova.';
      }

      this.cdr.markForCheck();
    }
  });
}

}
