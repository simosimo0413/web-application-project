import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, switchMap } from 'rxjs';

export interface StatoFacebook {
  configurato: boolean;
  urlPubblico: string | null;
}

export interface EsitoPubblicazioneFacebook {
  postId: string;
}

@Injectable({ providedIn: 'root' })
export class FacebookPromozioneService {
  private readonly http = inject(HttpClient);

  stato(annuncioId: number): Observable<StatoFacebook> {
    return this.http.get<StatoFacebook>(`/api/annunci/${annuncioId}/facebook/stato`);
  }

  pubblica(annuncioId: number): Observable<EsitoPubblicazioneFacebook> {
    return this.http.get('/api/auth/csrf').pipe(
      switchMap(() => this.http.post<EsitoPubblicazioneFacebook>(
        `/api/annunci/${annuncioId}/facebook/pubblica`, {}
      ))
    );
  }
}
