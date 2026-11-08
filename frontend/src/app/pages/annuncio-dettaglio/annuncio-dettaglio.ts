
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AnnuncioDettaglio as AnnuncioDettaglioModel } from '../../models/annuncio-dettaglio';
import { AnnuncioService } from '../../services/annuncio';

@Component({
  selector: 'app-annuncio-dettaglio',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './annuncio-dettaglio.html',
  styleUrl: './annuncio-dettaglio.css'
})
export class AnnuncioDettaglio implements OnInit {

  private route = inject(ActivatedRoute);
  private annuncioService = inject(AnnuncioService);
  private cdr = inject(ChangeDetectorRef);

  annuncio: AnnuncioDettaglioModel | null = null;
  caricamento = true;
  errore = false;

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
}
