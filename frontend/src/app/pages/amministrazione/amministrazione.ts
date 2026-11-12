import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService, AnnuncioAdmin, UtenteAdmin } from '../../services/admin';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-amministrazione',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './amministrazione.html',
  styleUrl: './amministrazione.css'
})
export class Amministrazione implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(AdminService);
  private readonly cdr = inject(ChangeDetectorRef);

  utenti: UtenteAdmin[] = [];
  annunci: AnnuncioAdmin[] = [];
  caricamentoUtenti = true;
  caricamentoAnnunci = true;
  erroreUtenti = '';
  erroreAnnunci = '';
  conferma = '';
  inCorso: string | null = null;

  ngOnInit(): void {
    this.caricaUtenti();
    this.caricaAnnunci();
  }

  caricaUtenti(): void {
    this.caricamentoUtenti = true;
    this.erroreUtenti = '';
    this.api.utenti().subscribe({
      next: lista => {
        this.utenti = lista;
        this.caricamentoUtenti = false;
        this.cdr.markForCheck();
      },
      error: err => {
        this.caricamentoUtenti = false;
        this.erroreUtenti = err.status === 401 || err.status === 403
          ? 'Accesso riservato agli amministratori.'
          : 'Impossibile caricare gli utenti.';
        this.cdr.markForCheck();
      }
    });
  }

  caricaAnnunci(): void {
    this.caricamentoAnnunci = true;
    this.erroreAnnunci = '';
    this.api.annunci().subscribe({
      next: lista => {
        this.annunci = lista;
        this.caricamentoAnnunci = false;
        this.cdr.markForCheck();
      },
      error: err => {
        this.caricamentoAnnunci = false;
        this.erroreAnnunci = err.status === 401 || err.status === 403
          ? 'Accesso riservato agli amministratori.'
          : 'Impossibile caricare gli annunci.';
        this.cdr.markForCheck();
      }
    });
  }

  cambiaBan(u: UtenteAdmin): void {
    if (this.inCorso || u.ruolo === 'ADMIN') return;
    const nuovo = !u.bannato;
    if (!window.confirm(nuovo
      ? `Bannare l'utente ${u.email}?`
      : `Riattivare l'utente ${u.email}?`)) return;
    this.inCorso = `ban-${u.id}`;
    this.conferma = '';
    this.erroreUtenti = '';
    this.api.impostaBan(u.id, nuovo).subscribe({
      next: () => {
        u.bannato = nuovo;
        this.conferma = nuovo ? 'Utente bannato.' : 'Utente riattivato.';
        this.inCorso = null;
        this.cdr.markForCheck();
      },
      error: () => {
        this.erroreUtenti = 'Impossibile aggiornare il ban. Controlla permessi e stato utente.';
        this.inCorso = null;
        this.cdr.markForCheck();
      }
    });
  }

  promuovi(u: UtenteAdmin): void {
    if (this.inCorso || u.ruolo === 'ADMIN' || u.bannato) return;
    if (!window.confirm(`Rendere ${u.email} amministratore? Questa operazione concede privilegi elevati.`)) return;
    this.inCorso = `promuovi-${u.id}`;
    this.conferma = '';
    this.erroreUtenti = '';
    this.api.promuovi(u.id).subscribe({
      next: () => {
        u.ruolo = 'ADMIN';
        this.conferma = 'Utente promosso. Dovrà effettuare nuovamente il login.';
        this.inCorso = null;
        this.cdr.markForCheck();
      },
      error: () => {
        this.erroreUtenti = 'Impossibile promuovere questo utente.';
        this.inCorso = null;
        this.cdr.markForCheck();
      }
    });
  }

  chiudi(a: AnnuncioAdmin): void {
    if (this.inCorso || a.stato !== 'ATTIVO') return;
    if (!window.confirm(`Ritirare l'annuncio #${a.id}: ${a.titolo}?`)) return;
    this.inCorso = `chiudi-${a.id}`;
    this.conferma = '';
    this.erroreAnnunci = '';
    this.api.chiudiAnnuncio(a.id).subscribe({
      next: () => {
        a.stato = 'CHIUSO';
        this.conferma = 'Annuncio ritirato dal catalogo.';
        this.inCorso = null;
        this.cdr.markForCheck();
      },
      error: err => {
        this.erroreAnnunci = err.status === 409
          ? 'Non puoi ritirare un annuncio con un’asta non terminata.'
          : 'Impossibile ritirare l’annuncio.';
        this.inCorso = null;
        this.cdr.markForCheck();
      }
    });
  }
}
