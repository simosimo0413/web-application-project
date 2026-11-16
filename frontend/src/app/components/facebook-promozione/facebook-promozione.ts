import { ChangeDetectorRef, Component, Input, OnInit, inject } from '@angular/core';
import { FacebookPromozioneService, StatoFacebook } from '../../services/facebook-promozione';

@Component({
  selector: 'app-facebook-promozione',
  standalone: true,
  templateUrl: './facebook-promozione.html',
  styleUrl: './facebook-promozione.css'
})
export class FacebookPromozione implements OnInit {
  @Input({ required: true }) annuncioId!: number;

  private readonly service = inject(FacebookPromozioneService);
  private readonly cdr = inject(ChangeDetectorRef);

  caricamento = true;
  inPubblicazione = false;
  stato: StatoFacebook | null = null;
  errore = '';
  postId = '';

  ngOnInit(): void {
    this.service.stato(this.annuncioId).subscribe({
      next: stato => {
        this.stato = stato;
        this.caricamento = false;
        this.cdr.markForCheck();
      },
      error: errore => {
        this.caricamento = false;
        this.errore = errore.status === 403 || errore.status === 401
          ? 'Accesso non autorizzato alla promozione.'
          : 'Impossibile verificare la configurazione Facebook.';
        this.cdr.markForCheck();
      }
    });
  }

  get condivisioneFacebook(): string | null {
    return this.stato?.urlPubblico
      ? `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(this.stato.urlPubblico)}`
      : null;
  }

  pubblica(): void {
    if (!this.stato?.configurato || this.inPubblicazione) return;
    const testo = this.stato.urlPubblico
      ? 'Pubblicare questo annuncio sulla Pagina Facebook configurata?'
      : 'Pubblicare sulla Pagina Facebook solo titolo, prezzo e caratteristiche? Il sito locale non ha un link pubblico.';
    if (!window.confirm(testo)) return;

    this.inPubblicazione = true;
    this.errore = '';
    this.postId = '';
    this.service.pubblica(this.annuncioId).subscribe({
      next: esito => {
        this.inPubblicazione = false;
        this.postId = esito.postId;
        this.cdr.markForCheck();
      },
      error: err => {
        this.inPubblicazione = false;
        if (err.status === 401 || err.status === 403) {
          this.errore = 'Non sei autorizzato a promuovere questo annuncio.';
        } else if (err.status === 404) {
          this.errore = 'L’annuncio non è attivo oppure non ti appartiene.';
        } else if (err.status === 503) {
          this.errore = 'Facebook non è ancora configurato nel backend.';
        } else {
          this.errore = 'Facebook non ha confermato la pubblicazione. Verifica il token e i permessi della Pagina.';
        }
        this.cdr.markForCheck();
      }
    });
  }
}
