
export interface AnnuncioDettaglio {
  id: number;
  titolo: string;
  descrizione: string | null;
  prezzo: number;
  prezzoPrecedente: number | null;
  superficieMq: number;
  indirizzo: string;
  citta: string;
  provincia: string;
  categoria: string;
  tipoContratto: 'VENDITA' | 'AFFITTO';
  fotoPrincipale: string | null;
  venditoreId: number;
}

