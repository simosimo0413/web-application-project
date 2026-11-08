
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ImmobileCard } from '../../components/immobile-card/immobile-card';
import { Annuncio } from '../../models/annuncio';
import { AnnuncioService } from '../../services/annuncio';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ImmobileCard],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {

  private annuncioService = inject(AnnuncioService);

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
      },
      error: (errore) => {
        console.error('Errore nel caricamento degli annunci:', errore);
        this.errore = true;
        this.caricamento = false;
      }
    });
  }
}
