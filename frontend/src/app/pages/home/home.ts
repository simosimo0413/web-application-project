
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImmobileCard } from '../../components/immobile-card/immobile-card';
import { Annuncio } from '../../models/annuncio';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ImmobileCard],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home {

  annunci: Annuncio[] = [
    {
      id: 1,
      titolo: 'Appartamento con terrazzo',
      prezzo: 185000,
      prezzoPrecedente: 200000,
      superficieMq: 95,
      citta: 'Cosenza',
      categoria: 'Appartamento',
      tipoContratto: 'VENDITA',
      fotoPrincipale: null
    },
    {
      id: 2,
      titolo: 'Villa con giardino',
      prezzo: 320000,
      prezzoPrecedente: null,
      superficieMq: 180,
      citta: 'Rende',
      categoria: 'Villa',
      tipoContratto: 'VENDITA',
      fotoPrincipale: null
    },
    {
      id: 3,
      titolo: 'Bilocale in affitto',
      prezzo: 650,
      prezzoPrecedente: null,
      superficieMq: 60,
      citta: 'Catanzaro',
      categoria: 'Appartamento',
      tipoContratto: 'AFFITTO',
      fotoPrincipale: null
    }
  ];
}
