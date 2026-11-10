
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Categoria {
  id: number;
  nome: string;
  descrizione: string;
}

@Injectable({
  providedIn: 'root'
})
export class CategorieService {

  private http = inject(HttpClient);

  recuperaCategorie(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>('/api/categorie');
  }
}
