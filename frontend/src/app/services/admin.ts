import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';

export interface UtenteAdmin {
  id: number;
  nome: string;
  cognome: string;
  email: string;
  ruolo: 'ADMIN' | 'VENDITORE' | 'ACQUIRENTE';
  bannato: boolean;
}

export interface AnnuncioAdmin {
  id: number;
  titolo: string;
  venditoreEmail: string;
  prezzo: number;
  stato: 'ATTIVO' | 'CHIUSO' | 'SOSPESO';
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);

  utenti(): Observable<UtenteAdmin[]> {
    return this.http.get<UtenteAdmin[]>('/api/admin/utenti');
  }

  annunci(): Observable<AnnuncioAdmin[]> {
    return this.http.get<AnnuncioAdmin[]>('/api/admin/annunci');
  }

  private csrfPrimaDi<T>(azione: () => Observable<T>): Observable<T> {
    return this.http.get('/api/auth/csrf').pipe(switchMap(() => azione()));
  }

  impostaBan(id: number, bannato: boolean): Observable<void> {
    return this.csrfPrimaDi(() =>
      this.http.patch<void>(`/api/admin/utenti/${id}/ban`, { bannato }));
  }

  promuovi(id: number): Observable<void> {
    return this.csrfPrimaDi(() =>
      this.http.patch<void>(`/api/admin/utenti/${id}/promuovi`, {}));
  }

  chiudiAnnuncio(id: number): Observable<void> {
    return this.csrfPrimaDi(() =>
      this.http.patch<void>(`/api/admin/annunci/${id}/chiudi`, {}));
  }
}
