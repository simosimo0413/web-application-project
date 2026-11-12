
import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Annunci } from './pages/annunci/annunci';
import { AnnuncioDettaglio } from './pages/annuncio-dettaglio/annuncio-dettaglio';
import { Registrazione } from './pages/registrazione/registrazione';
import { Login } from './pages/login/login';
import { PubblicaAnnuncio } from './pages/pubblica-annuncio/pubblica-annuncio';
import { richiediRuolo } from './guards/ruolo.guard';

export const routes: Routes = [


{
  path: 'visite-ricevute',
  canActivate: [richiediRuolo('VENDITORE')],
  loadComponent: () =>
    import('./pages/visite-ricevute/visite-ricevute')
      .then(m => m.VisiteRicevute)
},

{
  path: 'visite-mie',
  canActivate: [richiediRuolo('ACQUIRENTE')],
  loadComponent: () =>
    import('./pages/visite-mie/visite-mie')
      .then(m => m.VisiteMie)
},


{
  path: 'messaggi-ricevuti',
  canActivate: [richiediRuolo('VENDITORE')],
  loadComponent: () =>
    import('./pages/messaggi-ricevuti/messaggi-ricevuti')
      .then(m => m.MessaggiRicevuti)
},

  { path: 'amministrazione', canActivate: [richiediRuolo('ADMIN')], loadComponent: () => import('./pages/amministrazione/amministrazione').then(m => m.Amministrazione) },
  { path: 'registrazione', component: Registrazione },
  { path: 'login', component: Login },
  { path: 'pubblica-annuncio', component: PubblicaAnnuncio, canActivate: [richiediRuolo('VENDITORE')] },
  { path: 'annunci/:id', component: AnnuncioDettaglio },
  { path: '', component: Home },
  { path: 'annunci', component: Annunci },
  { path: '**', redirectTo: '' }
];
