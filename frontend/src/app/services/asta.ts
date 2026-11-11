
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
}
