
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Annuncio } from '../models/annuncio';

export interface FiltriAnnunci {
  citta: string;
  prezzoMax: number | null;
  categoria: string;
  tipoContratto: string;
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

    return this.http.get<Annuncio[]>('/api/annunci', { params });
  }
}
