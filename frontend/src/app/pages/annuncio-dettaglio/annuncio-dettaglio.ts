
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
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ContattoService } from '../../services/contatto';
import { AnnuncioDettaglio as AnnuncioDettaglioModel } from '../../models/annuncio-dettaglio';
import { AnnuncioService, ModificaAnnuncioRequest } from '../../services/annuncio';
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
  private router = inject(Router);
  private annuncioService = inject(AnnuncioService);
  private fotoService = inject(FotoService);
  private cdr = inject(ChangeDetectorRef);
  asta: Asta | null = null;
  caricamentoAsta = false;
  erroreAsta = false;

  prezzoBaseNuovaAsta: number | null = null;
  inizioNuovaAsta = '';
  fineNuovaAsta = '';

  creazioneAstaInCorso = false;
  astaCreata = false;
  erroreCreazioneAsta = '';

  riepilogoOfferte: OffertaRiepilogo | null = null;
  caricamentoRiepilogo = false;
  erroreRiepilogo = false;

  importoOfferta: number | null = null;
  invioOffertaInCorso = false;
  offertaInviata = false;
  erroreOfferta = '';


  annuncio: AnnuncioDettaglioModel | null = null;

  datiModifica: ModificaAnnuncioRequest = {
    titolo: '', descrizione: '', superficieMq: 0,
    indirizzo: '', citta: '', provincia: ''
  };
  nuovoPrezzo: number | null = null;
  modificaInCorso = false;
  ribassoInCorso = false;
  chiusuraInCorso = false;
  erroreGestione = '';
  successoGestione = '';

  get puoGestireAnnuncio(): boolean {
    const utente = this.authService.utenteCorrente();
    return !!(this.annuncio && utente && utente.ruolo === 'VENDITORE'
      && utente.id === this.annuncio.venditoreId);
  }

  private riempiModifica(annuncio: AnnuncioDettaglioModel): void {
    this.datiModifica = {
      titolo: annuncio.titolo,
      descrizione: annuncio.descrizione ?? '',
      superficieMq: annuncio.superficieMq,
      indirizzo: annuncio.indirizzo,
      citta: annuncio.citta,
      provincia: annuncio.provincia ?? ''
    };
  }

  private ricaricaDettaglio(id: number): void {
    this.annuncioService.getAnnuncioPerId(id).subscribe({
      next: dati => {
        this.annuncio = dati;
        this.riempiModifica(dati);
        this.cdr.markForCheck();
      },
      error: () => {
        this.erroreGestione = 'Aggiornamento salvato, ma impossibile ricaricare i dettagli.';
        this.cdr.markForCheck();
      }
    });
  }

  modificaAnnuncio(): void {
    if (!this.puoGestireAnnuncio || !this.annuncio || this.modificaInCorso) return;
    const dati = {
      ...this.datiModifica,
      titolo: this.datiModifica.titolo.trim(),
      descrizione: this.datiModifica.descrizione.trim(),
      indirizzo: this.datiModifica.indirizzo.trim(),
      citta: this.datiModifica.citta.trim()
    };
    if (!dati.titolo || !dati.descrizione || !dati.indirizzo || !dati.citta ||
        !Number.isFinite(dati.superficieMq) || dati.superficieMq <= 0) {
      this.erroreGestione = 'Compila correttamente tutti i campi obbligatori.';
      return;
    }
    this.modificaInCorso = true;
    this.erroreGestione = '';
    this.successoGestione = '';
    const id = this.annuncio.id;
    this.annuncioService.modifica(id, dati).subscribe({
      next: () => {
        this.modificaInCorso = false;
        this.successoGestione = 'Annuncio modificato correttamente.';
        this.ricaricaDettaglio(id);
        this.cdr.markForCheck();
      },
      error: err => {
        this.modificaInCorso = false;
        this.erroreGestione = this.messaggioErroreGestione(err.status);
        this.cdr.markForCheck();
      }
    });
  }

  ribassaPrezzo(): void {
    if (!this.puoGestireAnnuncio || !this.annuncio || this.ribassoInCorso) return;
    const prezzo = this.nuovoPrezzo;
    if (prezzo === null || !Number.isFinite(prezzo) || prezzo <= 0 ||
        prezzo >= this.annuncio.prezzo || prezzo > 9999999999.99 ||
        Math.round(prezzo * 100) / 100 !== prezzo) {
      this.erroreGestione = 'Il nuovo prezzo deve essere inferiore al prezzo attuale, con massimo due decimali.';
      return;
    }
    this.ribassoInCorso = true;
    this.erroreGestione = '';
    this.successoGestione = '';
    const id = this.annuncio.id;
    this.annuncioService.ribassaPrezzo(id, prezzo).subscribe({
      next: () => {
        this.ribassoInCorso = false;
        this.nuovoPrezzo = null;
        this.successoGestione = 'Prezzo ribassato correttamente.';
        this.ricaricaDettaglio(id);
        this.cdr.markForCheck();
      },
      error: err => {
        this.ribassoInCorso = false;
        this.erroreGestione = this.messaggioErroreGestione(err.status);
        this.cdr.markForCheck();
      }
    });
  }

  chiudiAnnuncio(): void {
    if (!this.puoGestireAnnuncio || !this.annuncio || this.chiusuraInCorso) return;
    if (!window.confirm('Vuoi ritirare questo annuncio? Non sarà più visibile nel catalogo.')) return;
    this.chiusuraInCorso = true;
    this.erroreGestione = '';
    this.successoGestione = '';
    this.annuncioService.chiudi(this.annuncio.id).subscribe({
      next: () => { void this.router.navigate(['/annunci']); },
      error: err => {
        this.chiusuraInCorso = false;
        this.erroreGestione = this.messaggioErroreGestione(err.status);
        this.cdr.markForCheck();
      }
    });
  }

  private messaggioErroreGestione(status: number): string {
    if (status === 401 || status === 403) return 'Non sei autorizzato a gestire questo annuncio.';
    if (status === 404) return 'Annuncio non trovato oppure non più attivo.';
    if (status === 400) return 'Controlla i dati inseriti.';
    if (status === 409) return 'Operazione non consentita: prezzo non valido oppure asta non terminata.';
    return 'Operazione non riuscita. Verifica lo stato dell’annuncio prima di riprovare.';
  }


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
        this.riempiModifica(dati);
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


get puoCreareAsta(): boolean {
  const utente = this.authService.utenteCorrente();

  return (
    utente?.ruolo === 'VENDITORE' &&
    this.annuncio !== null &&
    utente.id === this.annuncio.venditoreId &&
    this.annuncio.tipoContratto === 'VENDITA' &&
    !this.caricamentoAsta &&
    !this.erroreAsta &&
    this.asta === null
  );
}

get puoPresentareOfferta(): boolean {
  const utente = this.authService.utenteCorrente();

  return (
    utente?.ruolo === 'ACQUIRENTE' &&
    this.asta?.stato === 'IN_CORSO'
  );
}


creaAsta(): void {

  // 1. Verifica che il venditore possa creare l'asta.
  if (
    !this.puoCreareAsta ||
    !this.annuncio ||
    this.creazioneAstaInCorso
  ) {
    return;
  }

  this.erroreCreazioneAsta = '';
  this.astaCreata = false;

  const prezzoBase = this.prezzoBaseNuovaAsta;

  // 2. Controlla il prezzo base.
  if (
    prezzoBase === null ||
    !Number.isFinite(prezzoBase) ||
    prezzoBase <= 0 ||
    prezzoBase > 9999999999.99 ||
    Math.round(prezzoBase * 100) / 100 !== prezzoBase
  ) {
    this.erroreCreazioneAsta =
      'Inserisci un prezzo base valido, con massimo due decimali.';
    return;
  }

  // 3. Controlla che entrambe le date siano presenti.
  if (!this.inizioNuovaAsta || !this.fineNuovaAsta) {
    this.erroreCreazioneAsta =
      'Inserisci la data di inizio e quella di scadenza.';
    return;
  }

  const inizio = new Date(this.inizioNuovaAsta);
  const fine = new Date(this.fineNuovaAsta);

  // 4. Controlla che le date siano valide.
  if (
    !Number.isFinite(inizio.getTime()) ||
    !Number.isFinite(fine.getTime())
  ) {
    this.erroreCreazioneAsta =
      'Le date inserite non sono valide.';
    return;
  }

  // 5. L'asta deve iniziare nel futuro.
  if (inizio.getTime() <= Date.now()) {
    this.erroreCreazioneAsta =
      'La data di inizio deve essere futura.';
    return;
  }

  // 6. La scadenza deve essere successiva all'inizio.
  if (fine.getTime() <= inizio.getTime()) {
    this.erroreCreazioneAsta =
      'La scadenza deve essere successiva alla data di inizio.';
    return;
  }

  const annuncioId = this.annuncio.id;

  // 7. Invia la richiesta al backend.
  this.creazioneAstaInCorso = true;

  this.astaService.creaPerAnnuncio(
    annuncioId,
    {
      prezzoBase,
      inizio: inizio.toISOString(),
      fine: fine.toISOString()
    }
  ).subscribe({

    next: () => {
      this.creazioneAstaInCorso = false;
      this.astaCreata = true;
      this.erroreCreazioneAsta = '';

      this.prezzoBaseNuovaAsta = null;
      this.inizioNuovaAsta = '';
      this.fineNuovaAsta = '';

      // Recupera l'asta appena creata.
      this.caricaAsta(annuncioId);

      this.cdr.markForCheck();
    },

    error: (err) => {
      console.error('Errore creazione asta:', err);

      this.creazioneAstaInCorso = false;

      if (err.status === 400) {
        this.erroreCreazioneAsta =
          'Controlla il prezzo base e le date inserite.';
      } else if (err.status === 401 || err.status === 403) {
        this.erroreCreazioneAsta =
          'Non sei autorizzato a creare questa asta.';
      } else if (err.status === 404) {
        this.erroreCreazioneAsta =
          'Annuncio non disponibile o non appartenente al venditore.';
      } else if (err.status === 409) {
        this.erroreCreazioneAsta =
          'Esiste già un’asta per questo annuncio.';

        this.caricaAsta(annuncioId);
      } else {
        this.erroreCreazioneAsta =
          'Impossibile creare l’asta. Verifica che non sia già stata registrata prima di riprovare.';
      }

      this.cdr.markForCheck();
    }
  });
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
