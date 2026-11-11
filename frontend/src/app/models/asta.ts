
export interface Asta {
  id: number;
  annuncioId: number;
  prezzoBase: number;
  inizio: string;
  fine: string;
  stato: 'PROGRAMMATA' | 'IN_CORSO' | 'TERMINATA';
}
