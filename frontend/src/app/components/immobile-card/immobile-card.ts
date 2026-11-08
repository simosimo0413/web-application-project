import { RouterLink } from '@angular/router';
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Annuncio } from '../../models/annuncio';

@Component({
  selector: 'app-immobile-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './immobile-card.html',
  styleUrl: './immobile-card.css'
})
export class ImmobileCard {
  @Input({ required: true }) annuncio!: Annuncio;
}
