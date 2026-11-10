
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';


export interface Foto {
  id: number;
  url: string;
  descrizione: string | null;
  principale: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class FotoService {

  private http = inject(HttpClient);

  recuperaFotoAnnuncio(annuncioId: number): Observable<Foto[]> {
    return this.http.get<Foto[]>(
      `/api/annunci/${annuncioId}/foto`
    );
  }
  
caricaFoto(
  annuncioId: number,
  file: File,
  descrizione: string
): Observable<Foto> {

  const dati = new FormData();

  dati.append('file', file);
  dati.append('descrizione', descrizione);

  return this.http.get('/api/auth/csrf').pipe(
    switchMap(() =>
      this.http.post<Foto>(
        `/api/annunci/${annuncioId}/foto`,
        dati
      )
    )
  );
}

}
