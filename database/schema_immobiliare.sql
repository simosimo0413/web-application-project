BEGIN;

CREATE TABLE utenti (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    ruolo VARCHAR(20) NOT NULL CHECK (ruolo IN ('ADMIN', 'VENDITORE', 'ACQUIRENTE')),
    bannato BOOLEAN NOT NULL DEFAULT FALSE,
    data_registrazione TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categorie (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(80) NOT NULL UNIQUE,
    descrizione TEXT
);

CREATE TABLE immobili (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    categoria_id BIGINT NOT NULL REFERENCES categorie(id),
    superficie_mq NUMERIC(10,2) NOT NULL CHECK (superficie_mq > 0),
    indirizzo VARCHAR(255) NOT NULL,
    citta VARCHAR(120) NOT NULL,
    provincia VARCHAR(100),
    cap VARCHAR(10),
    descrizione TEXT NOT NULL,
    latitudine NUMERIC(9,6) CHECK (latitudine BETWEEN -90 AND 90),
    longitudine NUMERIC(9,6) CHECK (longitudine BETWEEN -180 AND 180),
    CHECK ((latitudine IS NULL) = (longitudine IS NULL))
);

CREATE TABLE annunci (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    immobile_id BIGINT NOT NULL REFERENCES immobili(id),
    venditore_id BIGINT NOT NULL REFERENCES utenti(id),
    titolo VARCHAR(200) NOT NULL,
    tipo_contratto VARCHAR(10) NOT NULL CHECK (tipo_contratto IN ('VENDITA', 'AFFITTO')),
    prezzo NUMERIC(12,2) NOT NULL CHECK (prezzo > 0),
    prezzo_precedente NUMERIC(12,2) CHECK (prezzo_precedente > 0),
    stato VARCHAR(20) NOT NULL DEFAULT 'ATTIVO' CHECK (stato IN ('ATTIVO', 'CHIUSO', 'SOSPESO')),
    data_pubblicazione TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (prezzo_precedente IS NULL OR prezzo_precedente > prezzo)
);

CREATE TABLE foto (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    immobile_id BIGINT NOT NULL REFERENCES immobili(id),
    url VARCHAR(1000) NOT NULL,
    descrizione VARCHAR(255),
    principale BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX una_foto_principale_per_immobile
    ON foto (immobile_id) WHERE principale;

CREATE TABLE visite (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annuncio_id BIGINT NOT NULL REFERENCES annunci(id),
    acquirente_id BIGINT NOT NULL REFERENCES utenti(id),
    data_visita TIMESTAMPTZ NOT NULL,
    confermata BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE recensioni (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    visita_id BIGINT NOT NULL UNIQUE REFERENCES visite(id),
    voto SMALLINT NOT NULL CHECK (voto BETWEEN 1 AND 5),
    commento TEXT NOT NULL,
    data_recensione TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE contatti (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annuncio_id BIGINT NOT NULL REFERENCES annunci(id),
    mittente_id BIGINT NOT NULL REFERENCES utenti(id),
    oggetto VARCHAR(255) NOT NULL,
    messaggio TEXT NOT NULL,
    data_invio TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE aste (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annuncio_id BIGINT NOT NULL UNIQUE REFERENCES annunci(id),
    prezzo_base NUMERIC(12,2) NOT NULL CHECK (prezzo_base > 0),
    inizio TIMESTAMPTZ NOT NULL,
    fine TIMESTAMPTZ NOT NULL,
    CHECK (fine > inizio)
);

CREATE TABLE offerte (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asta_id BIGINT NOT NULL REFERENCES aste(id),
    offerente_id BIGINT NOT NULL REFERENCES utenti(id),
    importo NUMERIC(12,2) NOT NULL CHECK (importo > 0),
    data_offerta TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_annunci_venditore ON annunci(venditore_id);
CREATE INDEX idx_annunci_immobile ON annunci(immobile_id);
CREATE INDEX idx_immobili_categoria ON immobili(categoria_id);
CREATE INDEX idx_visite_annuncio ON visite(annuncio_id);
CREATE INDEX idx_offerte_asta ON offerte(asta_id);

COMMIT;
