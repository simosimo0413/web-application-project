
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';

export interface RegistrazioneRequest {
  nome: string;
  cognome: string;
  email: string;
  password: string;
  ruolo: 'ACQUIRENTE' | 'VENDITORE';
}

export interface RegistrazioneResponse {
  id: number;
  messaggio: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);

  registra(dati: RegistrazioneRequest): Observable<RegistrazioneResponse> {
    return this.http.get('/api/auth/csrf').pipe(
      switchMap(() =>
        this.http.post<RegistrazioneResponse>(
          '/api/auth/registrazione',
          dati
        )
      )
    );
  }
}
