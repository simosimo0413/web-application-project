
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { Annuncio } from '../../models/annuncio';
import { AnnuncioService, FiltriAnnunci } from '../../services/annuncio';
import { ImmobileCard } from '../../components/immobile-card/immobile-card';

@Component({
  selector: 'app-annunci',
  standalone: true,
  imports: [CommonModule, FormsModule, ImmobileCard],
  templateUrl: './annunci.html',
  styleUrl: './annunci.css'
})
export class Annunci implements OnInit {

  private annuncioService = inject(AnnuncioService);
  private cdr = inject(ChangeDetectorRef);
  private numeroRicerca = 0;

  annunci: Annuncio[] = [];

  filtri: FiltriAnnunci = {
    citta: '',
    prezzoMax: null as number | null,
    categoria: '',
    tipoContratto: '',
    ordinamento: 'RECENTI'
  };

  caricamento = true;
  errore = false;

  ngOnInit(): void {
    this.caricaAnnunci();
  }


caricaAnnunci(): void {
  const ricercaAttuale = ++this.numeroRicerca;
  this.caricamento = true;
  this.errore = false;

  this.annuncioService.getAnnunci(this.filtri).subscribe({
    next: (dati) => {
      if (ricercaAttuale !== this.numeroRicerca) return;
      this.annunci = dati;
      this.caricamento = false;
      this.cdr.markForCheck();
    },
    error: (err) => {
      if (ricercaAttuale !== this.numeroRicerca) return;
      console.error('Errore caricamento annunci:', err);
      this.errore = true;
      this.caricamento = false;
      this.cdr.markForCheck();
    }
  });
}

}
