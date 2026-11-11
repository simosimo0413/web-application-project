
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { switchMap } from 'rxjs';

export interface VisitaRequest {
  dataVisita: string;
}

export interface VisitaResponse {
  id: number;
}

export interface VisitaRicevuta {
  id: number;
  annuncioId: number;
  emailAcquirente: string;
  dataVisita: string;
  confermata: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class VisitaService {

  private readonly http = inject(HttpClient);

  richiediVisita(
    annuncioId: number,
    richiesta: VisitaRequest
  ) {
    return this.http.get('/api/auth/csrf').pipe(
      switchMap(() =>
        this.http.post<VisitaResponse>(
          `/api/visite/annunci/${annuncioId}`,
          richiesta
        )
      )
    );
  }

  recuperaVisiteRicevute() {
    return this.http.get<VisitaRicevuta[]>(
      '/api/visite/ricevute'
    );
  }

  confermaVisita(visitaId: number) {
    return this.http.get('/api/auth/csrf').pipe(
      switchMap(() =>
        this.http.patch<void>(
          `/api/visite/${visitaId}/conferma`,
          {}
        )
      )
    );
  }
}
