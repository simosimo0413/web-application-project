
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';

export interface PubblicazioneAnnuncioRequest {
  titolo: string;
  tipoContratto: 'VENDITA' | 'AFFITTO';
  prezzo: number;
  categoriaId: number;
  superficieMq: number;
  indirizzo: string;
  citta: string;
  provincia: string;
  cap: string;
  descrizione: string;
}

export interface PubblicazioneAnnuncioResponse {
  id: number;
  messaggio: string;
}

@Injectable({
  providedIn: 'root'
})
export class PubblicazioneAnnuncioService {

  private http = inject(HttpClient);

  pubblica(
    dati: PubblicazioneAnnuncioRequest
  ): Observable<PubblicazioneAnnuncioResponse> {

    return this.http.get('/api/auth/csrf').pipe(
      switchMap(() =>
        this.http.post<PubblicazioneAnnuncioResponse>(
          '/api/annunci',
          dati
        )
      )
    );
  }
  
}
