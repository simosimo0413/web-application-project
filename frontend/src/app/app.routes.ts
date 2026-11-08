import { AnnuncioDettaglio } from './pages/annuncio-dettaglio/annuncio-dettaglio';
import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Annunci } from './pages/annunci/annunci';

export const routes: Routes = [
  { path: 'annunci/:id', component: AnnuncioDettaglio },
  { path: '', component: Home },
  { path: 'annunci', component: Annunci },
  { path: '**', redirectTo: '' }
];
