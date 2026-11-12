import { ChangeDetectorRef, Component, Input, OnChanges, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

interface RisultatoGeocoding {
  lat: string;
  lon: string;
}

// Cache in memoria: non ripetere le richieste per le stesse localita' nella sessione SPA.
const coordinateLocalita = new Map<string, { lat: number; lon: number }>();

/** Mappa OSM incorporata senza chiavi API o fatturazione. */
@Component({
  selector: 'app-mappa-immobile',
  standalone: true,
  templateUrl: './mappa-immobile.html',
  styleUrl: './mappa-immobile.css'
})
export class MappaImmobile implements OnChanges {
  @Input() indirizzo: string | null = null;
  @Input() citta: string | null = null;
  @Input() provincia: string | null = null;
  @Input() latitudine: number | null = null;
  @Input() longitudine: number | null = null;

  private readonly sanitizer = inject(DomSanitizer);
  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);

  urlGoogleMaps: string | null = null;
  urlIndicazioni: string | null = null;
  mappaIncorporata: SafeResourceUrl | null = null;
  usaCoordinate = false;
  caricamentoMappa = false;
  erroreMappa = '';

  ngOnChanges(): void {
    this.caricamentoMappa = false;
    this.erroreMappa = '';
    this.mappaIncorporata = null;

    const lat = this.latitudine;
    const lon = this.longitudine;
    this.usaCoordinate =
      lat !== null && lon !== null &&
      Number.isFinite(lat) && Number.isFinite(lon) &&
      lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;

    const indirizzoCompleto = [this.indirizzo, this.citta, this.provincia, 'Italia']
      .filter((parte): parte is string => typeof parte === 'string' && parte.trim().length > 0)
      .map(parte => parte.trim())
      .join(', ');

    const haIndirizzo = Boolean(this.indirizzo?.trim() || this.citta?.trim());
    const ricerca = this.usaCoordinate ? `${lat},${lon}` : (haIndirizzo ? indirizzoCompleto : null);
    if (!ricerca) {
      this.urlGoogleMaps = null;
      this.urlIndicazioni = null;
      return;
    }

    const query = encodeURIComponent(ricerca);
    this.urlGoogleMaps = `https://www.google.com/maps/search/?api=1&query=${query}`;
    this.urlIndicazioni = `https://www.google.com/maps/dir/?api=1&destination=${query}`;

    if (this.usaCoordinate) {
      this.creaMappa(lat!, lon!, true);
    } else {
      const localita = this.localitaRicerca();
      const salvata = coordinateLocalita.get(localita);
      if (salvata) {
        this.creaMappa(salvata.lat, salvata.lon, false);
      }
    }
  }

  /** Cerca solo la localita', non inventa una posizione precisa dell'immobile. */
  mostraMappa(): void {
    if (this.caricamentoMappa || this.mappaIncorporata || !this.urlGoogleMaps) {
      return;
    }

    const localita = this.localitaRicerca();
    if (!localita) {
      this.erroreMappa = 'Localita non disponibile per mostrare una mappa.';
      return;
    }

    const salvata = coordinateLocalita.get(localita);
    if (salvata) {
      this.creaMappa(salvata.lat, salvata.lon, false);
      return;
    }

    this.caricamentoMappa = true;
    this.erroreMappa = '';
    // Richiesta esplicita dell'utente, senza autocomplete o geocodifica in massa.
    // Per la demo scolastica usare responsabilmente il servizio pubblico Nominatim.
    const url = `https://nominatim.openstreetmap.org/search?format=jsonv2&limit=1&q=${encodeURIComponent(localita)}`;
    this.http.get<RisultatoGeocoding[]>(url).subscribe({
      next: (risultati) => {
        this.caricamentoMappa = false;
        const lat = Number(risultati[0]?.lat);
        const lon = Number(risultati[0]?.lon);
        if (!risultati.length || !this.coordinateValide(lat, lon)) {
          this.erroreMappa = 'Non e stato possibile localizzare la citta. Usa Apri su Google Maps.';
          this.cdr.markForCheck();
          return;
        }
        coordinateLocalita.set(localita, { lat, lon });
        this.creaMappa(lat, lon, false);
        this.cdr.markForCheck();
      },
      error: () => {
        this.caricamentoMappa = false;
        this.erroreMappa = 'Mappa temporaneamente non disponibile. Puoi usare Google Maps.';
        this.cdr.markForCheck();
      }
    });
  }

  private localitaRicerca(): string {
    return [this.citta?.trim(), 'Italia'].filter(Boolean).join(', ');
  }

  private coordinateValide(lat: number, lon: number): boolean {
    return Number.isFinite(lat) && Number.isFinite(lon) &&
      lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
  }

  private creaMappa(lat: number, lon: number, precisa: boolean): void {
    if (!this.coordinateValide(lat, lon)) return;
    const delta = precisa ? 0.009 : 0.045;
    const bbox = [lon - delta, lat - delta, lon + delta, lat + delta].join(',');
    // L'URL e' costruito esclusivamente da numeri validati e da un host fisso.
    const marker = precisa ? `&marker=${lat}%2C${lon}` : '';
    const url = `https://www.openstreetmap.org/export/embed.html?bbox=${encodeURIComponent(bbox)}&layer=mapnik${marker}`;
    this.mappaIncorporata = this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }
}
