
import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Annunci } from './pages/annunci/annunci';
import { AnnuncioDettaglio } from './pages/annuncio-dettaglio/annuncio-dettaglio';
import { Registrazione } from './pages/registrazione/registrazione';
import { Login } from './pages/login/login';
import { PubblicaAnnuncio } from './pages/pubblica-annuncio/pubblica-annuncio';

export const routes: Routes = [


{
  path: 'visite-ricevute',
  loadComponent: () =>
    import('./pages/visite-ricevute/visite-ricevute')
      .then(m => m.VisiteRicevute)
},

{
  path: 'visite-mie',
  loadComponent: () =>
    import('./pages/visite-mie/visite-mie')
      .then(m => m.VisiteMie)
},


{
  path: 'messaggi-ricevuti',
  loadComponent: () =>
    import('./pages/messaggi-ricevuti/messaggi-ricevuti')
      .then(m => m.MessaggiRicevuti)
},

  { path: 'registrazione', component: Registrazione },
  { path: 'login', component: Login },
  { path: 'pubblica-annuncio', component: PubblicaAnnuncio },
  { path: 'annunci/:id', component: AnnuncioDettaglio },
  { path: '', component: Home },
  { path: 'annunci', component: Annunci },
  { path: '**', redirectTo: '' }
];
