
import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Annunci } from './pages/annunci/annunci';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'annunci', component: Annunci },
  { path: '**', redirectTo: '' }
];
