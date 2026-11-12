
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Asta } from '../models/asta';

@Injectable({
  providedIn: 'root'
})
export class AstaService {

  private readonly http = inject(HttpClient);

  recuperaPerAnnuncio(annuncioId: number): Observable<Asta> {
    return this.http.get<Asta>(
      `/api/aste/annunci/${annuncioId}`
    );
  }


creaPerAnnuncio(
  annuncioId: number,
  dati: {
    prezzoBase: number;
    inizio: string;
    fine: string;
  }
): Observable<{ id: number }> {
  return this.http.post<{ id: number }>(
    `/api/aste/annunci/${annuncioId}`,
    dati
  );
}

}
