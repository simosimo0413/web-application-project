import { AnnuncioDettaglio } from '../models/annuncio-dettaglio';
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { Annuncio } from '../models/annuncio';

export interface FiltriAnnunci {
  citta: string;
  prezzoMax: number | null;
  categoria: string;
  tipoContratto: string;
  ordinamento: 'RECENTI' | 'PREZZO_ASC' | 'PREZZO_DESC' | 'SUPERFICIE_ASC' | 'SUPERFICIE_DESC';
}

export interface ModificaAnnuncioRequest {
  titolo: string;
  descrizione: string;
  superficieMq: number;
  indirizzo: string;
  citta: string;
  provincia: string;
}

@Injectable({
  providedIn: 'root'
})
export class AnnuncioService {

  private http = inject(HttpClient);

  getAnnunci(filtri?: FiltriAnnunci): Observable<Annuncio[]> {


    let params = new HttpParams();

    if (filtri?.citta.trim()) {
      params = params.set('citta', filtri.citta.trim());
    }

    if (filtri?.prezzoMax != null) {
      params = params.set('prezzoMax', filtri.prezzoMax.toString());
    }

    if (filtri?.categoria) {
      params = params.set('categoria', filtri.categoria);
    }

    if (filtri?.tipoContratto) {
      params = params.set('tipoContratto', filtri.tipoContratto);
    }

    if (filtri?.ordinamento) {
      params = params.set('ordinamento', filtri.ordinamento);
    }

    return this.http.get<Annuncio[]>('/api/annunci', { params });
  }

    getAnnuncioPerId(id: number): Observable<AnnuncioDettaglio> {
    return this.http.get<AnnuncioDettaglio>(`/api/annunci/${id}`);
  }

  modifica(id: number, dati: ModificaAnnuncioRequest): Observable<void> {
    return this.http.get('/api/auth/csrf').pipe(
      switchMap(() => this.http.patch<void>(`/api/annunci/${id}`, dati))
    );
  }

  ribassaPrezzo(id: number, nuovoPrezzo: number): Observable<void> {
    return this.http.get('/api/auth/csrf').pipe(
      switchMap(() => this.http.patch<void>(`/api/annunci/${id}/ribasso`, { nuovoPrezzo }))
    );
  }

  chiudi(id: number): Observable<void> {
    return this.http.get('/api/auth/csrf').pipe(
      switchMap(() => this.http.patch<void>(`/api/annunci/${id}/chiudi`, {}))
    );
  }

}
