import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { VisitaAcquirente } from '../models/visita-acquirente';

@Injectable({
  providedIn: 'root'
})
export class VisiteAcquirenteService {

  private http = inject(HttpClient);

  recuperaLeMieVisite(): Observable<VisitaAcquirente[]> {
    return this.http.get<VisitaAcquirente[]>(
      '/api/visite/mie'
    );
  }

}