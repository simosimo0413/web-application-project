
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';

export interface ContattoRequest {
  oggetto: string;
  messaggio: string;
}

export interface ContattoResponse {
  id: number;
}

export interface ContattoRicevuto {
  id: number;
  annuncioId: number;
  emailMittente: string;
  oggetto: string;
  messaggio: string;
  dataInvio: string;
}

@Injectable({
  providedIn: 'root'
})
export class ContattoService {

  private http = inject(HttpClient);

  inviaContatto(
    annuncioId: number,
    richiesta: ContattoRequest
  ): Observable<ContattoResponse> {

    return this.http.get('/api/auth/csrf').pipe(
      switchMap(() =>
        this.http.post<ContattoResponse>(
          `/api/contatti/annunci/${annuncioId}`,
          richiesta
        )
      )
    );
  }

  recuperaMessaggiRicevuti(): Observable<ContattoRicevuto[]> {

    return this.http.get<ContattoRicevuto[]>(
      '/api/contatti/ricevuti'
    );
  }
}
