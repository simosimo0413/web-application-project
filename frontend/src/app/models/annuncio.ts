
export interface Annuncio {
  id: number;
  titolo: string;
  prezzo: number;
  prezzoPrecedente: number | null;
  superficieMq: number;
  citta: string;
  categoria: string;
  tipoContratto: 'VENDITA' | 'AFFITTO';
  fotoPrincipale: string | null;
}
