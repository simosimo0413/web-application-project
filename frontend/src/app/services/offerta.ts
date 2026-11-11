
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { OffertaRiepilogo } from '../models/offerta-riepilogo';

@Injectable({
  providedIn: 'root'
})
export class OffertaService {

  private readonly http = inject(HttpClient);

  recuperaRiepilogo(astaId: number): Observable<OffertaRiepilogo> {
    return this.http.get<OffertaRiepilogo>(
      `/api/offerte/aste/${astaId}/riepilogo`
    );
  }

presentaOfferta(
  astaId: number,
  importo: number
): Observable<{ id: number }> {

  return this.http.post<{ id: number }>(
    `/api/offerte/aste/${astaId}`,
    { importo }
  );
}

}
