
import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { Navbar } from './components/navbar/navbar';
import { Footer } from './components/footer/footer';
import { AuthService } from './services/auth';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, Footer],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {

  private authService = inject(AuthService);

  ngOnInit(): void {
    this.authService.ripristinaSessione().subscribe({
      error: (err) => {
        if (err.status !== 401 && err.status !== 403) {
          console.error('Errore durante il recupero della sessione:', err);
        }
      }
    });
  }
}
