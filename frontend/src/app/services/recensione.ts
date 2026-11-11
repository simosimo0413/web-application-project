import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Recensione } from '../models/recensione';

@Injectable({
  providedIn: 'root'
})
export class RecensioneService {
  private http = inject(HttpClient);

  recuperaRecensioniAnnuncio(
    annuncioId: number
  ): Observable<Recensione[]> {
    return this.http.get<Recensione[]>(
      `/api/recensioni/annunci/${annuncioId}`
    );
  }

  pubblicaRecensione(
    visitaId: number,
    voto: number,
    commento: string
  ): Observable<{ id: number }> {

    return this.http.post<{ id: number }>(
      '/api/recensioni/visite/' + visitaId,
      {
        voto: voto,
        commento: commento
      }
    );
  }

}