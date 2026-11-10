
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

import { FotoService, Foto } from '../../services/foto';

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
  private fotoService = inject(FotoService);
  private cdr = inject(ChangeDetectorRef);

  annuncio: AnnuncioDettaglioModel | null = null;

  caricamento = true;
  errore = false;

  foto: Foto[] = [];
  caricamentoFoto = false;
  erroreFoto = false;

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

        this.caricaFoto(id);

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
}
