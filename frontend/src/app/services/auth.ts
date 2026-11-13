
import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, switchMap, tap, throwError } from 'rxjs';

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

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  id: number;
  nome: string;
  ruolo: 'ACQUIRENTE' | 'VENDITORE' | 'ADMIN';
  messaggio: string;
}


export interface SessioneResponse {
  id: number;
  email: string;
  ruolo: string;
}

export interface UtenteAutenticato {
  id: number;
  nome?: string;
  email?: string;
  ruolo: string;
}


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);

  private utenteSignal = signal<UtenteAutenticato | null>(null);

  readonly utenteCorrente = this.utenteSignal.asReadonly();

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

  login(dati: LoginRequest): Observable<LoginResponse> {
    return this.http.get('/api/auth/csrf').pipe(
      switchMap(() =>
        this.http.post<LoginResponse>(
          '/api/auth/login',
          dati
        )
      ),
      tap(risposta => {
        
      this.utenteSignal.set({
        id: risposta.id,
        nome: risposta.nome,
        ruolo: risposta.ruolo
      });

      })
    );
  }

  ripristinaSessione(): Observable<SessioneResponse> {
    return this.http.get<SessioneResponse>('/api/auth/me').pipe(
      tap(sessione => {
      this.utenteSignal.set({
        id: sessione.id,
        email: sessione.email,
        ruolo: sessione.ruolo.replace(/^ROLE_/, '')
      });

      }),
      catchError(errore => {
        if (errore.status === 401 || errore.status === 403) {
          this.utenteSignal.set(null);
        }
        return throwError(() => errore);
      })
    );
  }


  logout(): Observable<void> {
    return this.http.get('/api/auth/csrf').pipe(
      switchMap(() =>
        this.http.post<void>('/api/auth/logout', {})
      ),
      tap(() => {
        this.utenteSignal.set(null);
      })
    );
  }

}


