
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Annuncio } from '../../models/annuncio';
import { AnnuncioService } from '../../services/annuncio';
import { ImmobileCard } from '../../components/immobile-card/immobile-card';

@Component({
  selector: 'app-annunci',
  standalone: true,
  imports: [CommonModule, ImmobileCard],
  templateUrl: './annunci.html',
  styleUrl: './annunci.css'
})
export class Annunci implements OnInit {

  private annuncioService = inject(AnnuncioService);
  private cdr = inject(ChangeDetectorRef);

  annunci: Annuncio[] = [];
  caricamento = true;
  errore = false;

  ngOnInit(): void {
    this.caricaAnnunci();
  }


caricaAnnunci(): void {
  this.caricamento = true;
  this.errore = false;

  this.annuncioService.getAnnunci().subscribe({
    next: (dati) => {
      this.annunci = dati;
      this.caricamento = false;
      this.cdr.markForCheck();
    },
    error: (err) => {
      console.error('Errore caricamento annunci:', err);
      this.errore = true;
      this.caricamento = false;
      this.cdr.markForCheck();
    }
  });
}

}
