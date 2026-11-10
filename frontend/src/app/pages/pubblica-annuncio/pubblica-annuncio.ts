
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../services/auth';

import {
  PubblicazioneAnnuncioService,
  PubblicazioneAnnuncioRequest
} from '../../services/pubblicazione-annuncio';

import {
  CategorieService,
  Categoria
} from '../../services/categorie';

@Component({
  selector: 'app-pubblica-annuncio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pubblica-annuncio.html',
  styleUrl: './pubblica-annuncio.css'
})
export class PubblicaAnnuncio implements OnInit {

  readonly authService = inject(AuthService);

  private pubblicazioneService = inject(PubblicazioneAnnuncioService);
  private categorieService = inject(CategorieService);
  private cdr = inject(ChangeDetectorRef);

  categorie: Categoria[] = [];
  caricamentoCategorie = true;
  erroreCategorie = '';

  dati: PubblicazioneAnnuncioRequest = {
    titolo: '',
    tipoContratto: 'VENDITA',
    prezzo: 0,
    categoriaId: 0,
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

  ngOnInit(): void {
    this.categorieService.recuperaCategorie().subscribe({
      next: (risposta) => {
        this.categorie = risposta;
        this.caricamentoCategorie = false;

        if (risposta.length === 0) {
          this.erroreCategorie = 'Nessuna categoria disponibile.';
        }

        this.cdr.markForCheck();
      },
      error: () => {
        this.caricamentoCategorie = false;
        this.erroreCategorie =
          'Impossibile caricare le categorie immobiliari.';

        this.cdr.markForCheck();
      }
    });
  }

  pubblica(): void {

    if (this.caricamento) {
      return;
    }

    if (!this.isVenditore) {
      this.errore = 'Solo i venditori possono pubblicare annunci.';
      return;
    }

    if (!this.categorie.some(c => c.id === this.dati.categoriaId)) {
      this.errore = 'Seleziona una categoria valida.';
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
