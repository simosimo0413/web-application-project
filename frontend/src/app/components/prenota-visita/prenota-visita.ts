
import { Component, Input, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { VisitaService } from '../../services/visita';

@Component({
  selector: 'app-prenota-visita',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './prenota-visita.html',
  styleUrl: './prenota-visita.css'
})
export class PrenotaVisita {

  private readonly visitaService = inject(VisitaService);
  private readonly cdr = inject(ChangeDetectorRef);

  // ID dell'annuncio ricevuto dalla pagina di dettaglio.
  @Input({ required: true }) annuncioId!: number;

  // Data e ora scelte dall'utente, nel suo fuso orario.
  dataOraLocale = '';

  // Data minima selezionabile nel modulo HTML.
  dataMinima = this.formattaDataLocale(new Date());

  // Stati dell'interfaccia.
  invioInCorso = false;
  richiestaInviata = false;
  errore = '';

  inviaRichiesta(): void {

    this.errore = '';
    this.richiestaInviata = false;

    // 1. Controlliamo che l'annuncio sia valido.
    if (!Number.isInteger(this.annuncioId) || this.annuncioId <= 0) {
      this.errore = 'Annuncio non valido.';
      return;
    }

    // 2. Controlliamo che sia stata scelta una data.
    if (!this.dataOraLocale) {
      this.errore = 'Seleziona una data e un orario.';
      return;
    }

    // 3. Convertiamo la data locale in un oggetto Date.
    const dataSelezionata = new Date(this.dataOraLocale);

    if (Number.isNaN(dataSelezionata.getTime())) {
      this.errore = 'La data selezionata non è valida.';
      return;
    }

    // 4. Impediamo la richiesta di visite nel passato.
    if (dataSelezionata.getTime() <= Date.now()) {
      this.errore = 'La data della visita deve essere futura.';
      return;
    }

    // 5. Convertiamo l'orario in ISO 8601 (UTC).
    const dataVisita = dataSelezionata.toISOString();

    this.invioInCorso = true;

    // 6. Inviamo la richiesta al backend Spring Boot.
    this.visitaService.richiediVisita(
      this.annuncioId,
      { dataVisita }
    ).subscribe({

      next: () => {
        this.invioInCorso = false;
        this.richiestaInviata = true;
        this.dataOraLocale = '';

        this.cdr.markForCheck();
      },

      error: (erroreHttp: HttpErrorResponse) => {
        this.invioInCorso = false;

        if (erroreHttp.status === 401 || erroreHttp.status === 403) {
          this.errore =
            'Per richiedere una visita devi accedere come ACQUIRENTE.';
        } else if (erroreHttp.status === 404) {
          this.errore = 'Annuncio non trovato.';
        } else if (erroreHttp.status === 400) {
          this.errore =
            'Richiesta non valida. Controlla la data selezionata.';
        } else {
          this.errore =
            'Impossibile richiedere la visita. Riprova più tardi.';
        }

        this.cdr.markForCheck();
      }
    });
  }

  // Formatta la data per un input HTML di tipo datetime-local.
  private formattaDataLocale(data: Date): string {

    const anno = data.getFullYear();
    const mese = String(data.getMonth() + 1).padStart(2, '0');
    const giorno = String(data.getDate()).padStart(2, '0');

    const ore = String(data.getHours()).padStart(2, '0');
    const minuti = String(data.getMinutes()).padStart(2, '0');

    return `${anno}-${mese}-${giorno}T${ore}:${minuti}`;
  }
}
