
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { FotoService, Foto } from '../../services/foto';

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

private fotoService = inject(FotoService);

fotoSelezionata: File | null = null;
descrizioneFoto = '';
fotoInEliminazioneId: number | null = null;

fotoCaricate: Foto[] = [];

caricamentoFoto = false;
erroreFoto = '';


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


selezionaFoto(evento: Event): void {
  const input = evento.target as HTMLInputElement;

  this.fotoSelezionata = input.files?.[0] ?? null;
  input.value = '';

  this.erroreFoto = '';

  if (
    this.fotoSelezionata &&
    this.fotoSelezionata.size > 5 * 1024 * 1024
  ) {
    this.fotoSelezionata = null;
    this.erroreFoto = 'La fotografia non può superare 5 MB.';
  }
}

caricaFoto(): void {

  if (
    this.annuncioId === null ||
    this.fotoSelezionata === null ||
    this.caricamentoFoto
  ) {
    return;
  }

  this.caricamentoFoto = true;
  this.erroreFoto = '';

  this.fotoService.caricaFoto(
    this.annuncioId,
    this.fotoSelezionata,
    this.descrizioneFoto
  ).subscribe({
    next: (fotografia) => {
      this.fotoCaricate.push(fotografia);

      this.fotoSelezionata = null;
      this.descrizioneFoto = '';
      this.caricamentoFoto = false;

      this.cdr.markForCheck();
    },
    error: (err) => {
      this.caricamentoFoto = false;

      if (err.status === 400 || err.status === 413) {
        this.erroreFoto =
          'Fotografia non valida. Usa JPG, PNG o WEBP fino a 5 MB.';
      } else if (err.status === 401 ||
                 err.status === 403 ||
                 err.status === 404) {
        this.erroreFoto =
          'Non sei autorizzato a caricare foto su questo annuncio.';
      } else {
        this.erroreFoto =
          'Errore durante il caricamento della fotografia.';
      }

      this.cdr.markForCheck();
    }
  });
}


eliminaFoto(fotografia: Foto): void {
  const annuncioId = this.annuncioId;

  if (
    annuncioId === null ||
    this.fotoInEliminazioneId !== null
  ) {
    return;
  }

  const conferma = window.confirm(
    'Sei sicuro di voler eliminare questa fotografia?'
  );

  if (!conferma) {
    return;
  }

  this.fotoInEliminazioneId = fotografia.id;
  this.erroreFoto = '';

  this.fotoService.eliminaFoto(
    annuncioId,
    fotografia.id
  ).subscribe({
    next: () => {
      this.fotoCaricate = this.fotoCaricate.filter(
        foto => foto.id !== fotografia.id
      );

      this.fotoInEliminazioneId = null;
      this.cdr.markForCheck();
    },

    error: (err) => {
      console.error(
        'Errore eliminazione fotografia:',
        err
      );

      if (err.status === 401 || err.status === 403) {
        this.erroreFoto =
          'Non sei autorizzato a eliminare questa fotografia.';
      } else if (err.status === 404) {
        this.erroreFoto =
          'La fotografia non è stata trovata.';
      } else {
        this.erroreFoto =
          'Non è stato possibile eliminare la fotografia.';
      }

      this.fotoInEliminazioneId = null;
      this.cdr.markForCheck();
    }
  });
}

}
