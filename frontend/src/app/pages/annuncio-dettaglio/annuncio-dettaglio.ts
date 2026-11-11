
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { Asta } from '../../models/asta';
import { AstaService } from '../../services/asta';
import { PrenotaVisita } from '../../components/prenota-visita/prenota-visita';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ContattoService } from '../../services/contatto';
import { AnnuncioDettaglio as AnnuncioDettaglioModel } from '../../models/annuncio-dettaglio';
import { AnnuncioService } from '../../services/annuncio';
import { OffertaRiepilogo } from '../../models/offerta-riepilogo';
import { OffertaService } from '../../services/offerta';
import { Recensione } from '../../models/recensione';
import { RecensioneService } from '../../services/recensione';
import { FotoService, Foto } from '../../services/foto';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-annuncio-dettaglio',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule,PrenotaVisita],
  templateUrl: './annuncio-dettaglio.html',
  styleUrl: './annuncio-dettaglio.css'
})
export class AnnuncioDettaglio implements OnInit {
  readonly authService = inject(AuthService);
  private offertaService = inject(OffertaService);
  private contattoService = inject(ContattoService);
  private recensioneService = inject(RecensioneService);
  private astaService = inject(AstaService);

oggettoContatto = '';
messaggioContatto = '';

invioContattoInCorso = false;
contattoInviato = false;
erroreContatto = '';

  private route = inject(ActivatedRoute);
  private annuncioService = inject(AnnuncioService);
  private fotoService = inject(FotoService);
  private cdr = inject(ChangeDetectorRef);
  asta: Asta | null = null;
  caricamentoAsta = false;
  erroreAsta = false;
  riepilogoOfferte: OffertaRiepilogo | null = null;
  caricamentoRiepilogo = false;
  erroreRiepilogo = false;

  importoOfferta: number | null = null;
  invioOffertaInCorso = false;
  offertaInviata = false;
  erroreOfferta = '';


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
        this.caricaAsta(id);
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


private caricaAsta(annuncioId: number): void {
  this.caricamentoAsta = true;
  this.erroreAsta = false;
  this.asta = null;

  this.astaService.recuperaPerAnnuncio(annuncioId).subscribe({
    next: (risposta) => {
  this.asta = risposta;
  this.caricamentoAsta = false;

  this.caricaRiepilogoOfferte(risposta.id);

  this.cdr.markForCheck();
},
    error: (err) => {
      this.caricamentoAsta = false;

      if (err.status !== 404) {
        console.error('Errore caricamento asta:', err);
        this.erroreAsta = true;
      }

      this.cdr.markForCheck();
    }
  });
}


private caricaRiepilogoOfferte(astaId: number): void {
  this.caricamentoRiepilogo = true;
  this.erroreRiepilogo = false;
  this.riepilogoOfferte = null;

  this.offertaService.recuperaRiepilogo(astaId).subscribe({
    next: (risposta) => {
      this.riepilogoOfferte = risposta;
      this.caricamentoRiepilogo = false;

      this.cdr.markForCheck();
    },
    error: (err) => {
      console.error(
        'Errore caricamento riepilogo offerte:',
        err
      );

      this.erroreRiepilogo = true;
      this.caricamentoRiepilogo = false;

      this.cdr.markForCheck();
    }
  });
}


get puoPresentareOfferta(): boolean {
  const utente = this.authService.utenteCorrente();

  return (
    utente?.ruolo === 'ACQUIRENTE' &&
    this.asta?.stato === 'IN_CORSO'
  );
}


inviaOfferta(): void {

  // 1. Verifica che l'utente possa partecipare.
  if (
    !this.puoPresentareOfferta ||
    !this.asta ||
    this.invioOffertaInCorso
  ) {
    return;
  }

  this.erroreOfferta = '';
  this.offertaInviata = false;

  const importo = this.importoOfferta;

  // 2. Verifica che l'importo sia valido.
  if (
    importo === null ||
    !Number.isFinite(importo) ||
    importo <= 0 ||
    importo > 9999999999.99 ||
    Math.round(importo * 100) / 100 !== importo
  ) {
    this.erroreOfferta =
      'Inserisci un importo valido, con massimo due decimali.';
    return;
  }

  // 3. Verifica che il riepilogo sia disponibile.
  if (!this.riepilogoOfferte) {
    this.erroreOfferta =
      'Attendi il caricamento delle offerte.';
    return;
  }

  const massimo = this.riepilogoOfferte.offertaMassima;

  // 4. Controlla la soglia minima.
  if (massimo !== null && importo <= massimo) {
    this.erroreOfferta =
      'Devi superare l’offerta più alta attuale.';
    return;
  }

  if (massimo === null && importo < this.asta.prezzoBase) {
    this.erroreOfferta =
      'L’offerta non può essere inferiore al prezzo base.';
    return;
  }

  // 5. Invia la richiesta al backend.
  const astaId = this.asta.id;

  this.invioOffertaInCorso = true;

  this.offertaService.presentaOfferta(
    astaId,
    importo
  ).subscribe({

    next: () => {
      this.invioOffertaInCorso = false;
      this.offertaInviata = true;
      this.erroreOfferta = '';
      this.importoOfferta = null;

      // Aggiorna offerta massima e numero di offerte.
      this.caricaRiepilogoOfferte(astaId);

      this.cdr.markForCheck();
    },

    error: (err) => {
      console.error('Errore invio offerta:', err);

      this.invioOffertaInCorso = false;

      if (err.status === 400) {
        this.erroreOfferta =
          'Importo non valido. Controlla la cifra inserita.';
      } else if (err.status === 401 || err.status === 403) {
        this.erroreOfferta =
          'Devi accedere come acquirente per presentare offerte.';
      } else if (err.status === 404) {
        this.erroreOfferta =
          'L’asta non è più disponibile.';
      } else if (err.status === 409) {
        this.erroreOfferta =
          'Offerta non accettata: l’asta potrebbe essere chiusa ' +
          'oppure potrebbe esserci un’offerta più alta.';

        this.caricaRiepilogoOfferte(astaId);
      } else {
        this.erroreOfferta =
          'Impossibile inviare l’offerta. Verifica prima ' +
          'di riprovare se è stata registrata.';
      }

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
