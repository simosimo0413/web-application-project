
import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Annunci } from './pages/annunci/annunci';
import { AnnuncioDettaglio } from './pages/annuncio-dettaglio/annuncio-dettaglio';
import { Registrazione } from './pages/registrazione/registrazione';
import { Login } from './pages/login/login';
import { PubblicaAnnuncio } from './pages/pubblica-annuncio/pubblica-annuncio';

export const routes: Routes = [
  { path: 'registrazione', component: Registrazione },
  { path: 'login', component: Login },
  { path: 'pubblica-annuncio', component: PubblicaAnnuncio },
  { path: 'annunci/:id', component: AnnuncioDettaglio },
  { path: '', component: Home },
  { path: 'annunci', component: Annunci },
  { path: '**', redirectTo: '' }
];
