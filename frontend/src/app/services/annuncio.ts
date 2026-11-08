import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Annuncio } from '../models/annuncio';

@Injectable({
  providedIn: 'root'
})
export class AnnuncioService {

  private http = inject(HttpClient);

  getAnnunci(): Observable<Annuncio[]> {
    return this.http.get<Annuncio[]>('/api/annunci');
  }
}
