# Web Application — README di avvio e informazioni del progetto

**Guida operativa locale (Windows / PowerShell)**  
**Ultimo aggiornamento: 16 novembre 2026**

Questo README descrive come avviare il progetto immobiliare sviluppato con **PostgreSQL 17**, **Spring Boot** e **Angular**. L'ordine corretto è **PostgreSQL → backend Spring Boot → frontend Angular**.

## 1. Avviare PostgreSQL

1. Aprire **pgAdmin 4** e verificare che il server PostgreSQL 17 sia disponibile.
2. Se necessario, controllare il servizio in PowerShell:

   ```powershell
   Get-Service -Name "postgresql-x64-17"
   ```

   Se lo stato è `Stopped`, avviarlo (possono essere necessari permessi di amministratore):

   ```powershell
   Start-Service -Name "postgresql-x64-17"
   ```

3. **Selezionare il database corretto in base a quello ripristinato sul proprio computer:**

   | Configurazione | Database PostgreSQL |
   | --- | --- |
   | Database originale | `immobiliare_db` |
   | Database completo ripristinato | `immobiliare_db_completo` |

**Nota:** pgAdmin serve a gestire PostgreSQL; dopo il controllo può essere chiuso. Il servizio PostgreSQL deve invece rimanere attivo.

## 2. Avviare il backend Spring Boot

Aprire **Visual Studio Code** nella cartella principale del progetto, poi aprire un terminale **PowerShell** e spostarsi nel backend:

```powershell
cd .\backend
```

**Se il terminale è già nella cartella `backend`, non ripetere il comando `cd`.**

### 2.1 Impostare il database

Se si utilizza il database originale:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/immobiliare_db"
```

Se invece è stato ripristinato il database completo:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/immobiliare_db_completo"
```

Questa impostazione vale per il **terminale corrente**: non modifica permanentemente il progetto.

### 2.2 Impostare utente e password PostgreSQL

Eseguire i seguenti comandi **nello stesso terminale PowerShell del backend**. Quando richiesto, inserire la password PostgreSQL configurata sul proprio computer:

```powershell
$env:DB_USERNAME = "postgres"
$securePassword = Read-Host "Password PostgreSQL" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
Remove-Variable securePassword
```

### 2.3 Avviare Spring Boot

```powershell
.\mvnw.cmd spring-boot:run
```

Attendere l'avvio del server e verificare l'indirizzo:

**http://localhost:8080/api/annunci**

Se nel browser compare un JSON di annunci, il backend sta rispondendo e riesce a leggere il database configurato. **Lasciare aperto il terminale**.

## 3. Avviare il frontend Angular

Aprire un **secondo terminale PowerShell** in VS Code.

Se ci si trova nella **cartella principale** del progetto, entrare in `frontend`:

```powershell
cd .\frontend
```

Se invece il prompt termina **già con `\frontend>`**, **non** eseguire di nuovo `cd .\frontend`.

Avviare Angular con il comando compatibile con PowerShell Windows:

```powershell
npx.cmd ng serve --proxy-config proxy.conf.json --port 4200
```

`npx.cmd` evita il blocco di sicurezza che su alcuni PC impedisce l'esecuzione di `npx.ps1`; non richiede di cambiare la policy di PowerShell.

Quando la compilazione è terminata, aprire:

**http://localhost:4200/**

Anche il terminale Angular deve rimanere aperto durante l'utilizzo dell'applicazione.

## 4. Dati attualmente verificati

Al momento della preparazione della demo, i database verificati presentavano questi valori:

| Elemento | Quantità |
| --- | ---: |
| Annunci totali | **66** |
| Annunci dimostrativi inseriti | **60** |
| Immobili totali | **66** |
| Record delle fotografie nel database | **12** |

Le **10 fotografie aggiunte durante la preparazione della demo** sono state trasferite e verificate nell'interfaccia Angular.

Per ricontrollare i dati, aprire il **Query Tool** in pgAdmin sul database effettivamente utilizzato e lanciare:

```sql
SELECT
    current_database() AS database_attivo,
    (SELECT COUNT(*) FROM public.annunci) AS annunci_totali,
    (SELECT COUNT(*) FROM public.foto) AS foto_totali,
    (SELECT COUNT(*) FROM public.immobili) AS immobili_totali,
    (SELECT COUNT(*) FROM public.demo_cs26_annunci_mappa) AS annunci_demo;
```

Questa query non modifica il database. Le quantità potranno cambiare in seguito a nuovi inserimenti.

## 5. Account di prova dell'applicazione

L'applicazione dispone di account di prova per i diversi ruoli (venditore, acquirente e amministratore).

**Per sicurezza, le credenziali degli account di prova non sono riportate in questo repository pubblico.** Se necessarie per la dimostrazione, verranno comunicate separatamente.

Questi account sono distinti dall'utente PostgreSQL `postgres`, utilizzato dal backend per collegarsi al database.

## 6. Database e file delle fotografie

**GitHub sincronizza il codice tracciato, ma non trasferisce automaticamente il database PostgreSQL né i file caricati localmente.**

Per riprodurre correttamente il progetto su un altro computer, occorre quindi:

- includere nella consegna il **dump PostgreSQL** del database;
- ripristinare il dump in PostgreSQL sul computer di destinazione;
- trasferire anche i file delle fotografie nella cartella `backend\uploads\immobili`, se non sono già inclusi nel repository.

Le fotografie sono composte da **due parti necessarie**: i record nella tabella `foto` e i file fisici nella cartella `backend\uploads\immobili`. Un backup del solo database non trasferisce i file delle immagini.

Prima di un futuro ripristino, **salvare un backup del database di destinazione**. È preferibile ripristinare in un database nuovo anziché sovrascrivere quello esistente.

## 7. Risoluzione dei problemi comuni

| Problema | Verifica / soluzione |
| --- | --- |
| `FATALE: autenticazione con password fallita` | Verificare `DB_USERNAME` (deve essere `postgres`) e la password PostgreSQL locale. Riavviare il backend dopo aver corretto le variabili. |
| Errore `500` sugli annunci | Verificare PostgreSQL, credenziali e nome del database in `SPRING_DATASOURCE_URL`. |
| `Cannot find path ...\frontend\frontend` | Si è già nella cartella frontend: non ripetere `cd .\frontend`. |
| `npx.ps1`: esecuzione script disabilitata | Usare `npx.cmd ng serve --proxy-config proxy.conf.json --port 4200`. |
| `mvn` non riconosciuto | Avviare da `backend` con `.\mvnw.cmd spring-boot:run`. |
| Annunci visibili, ma foto assenti | Controllare che i file siano in `backend\uploads\immobili` e che la tabella `foto` contenga i relativi record. |
| pgAdmin `Restore`: `could not open input file` | Selezionare il file `.backup` mediante il navigatore di pgAdmin; non concatenare due percorsi Windows nel campo Filename. |
| Database attivo non corretto | Verificare con `SELECT current_database();` e controllare la variabile `SPRING_DATASOURCE_URL`. |

## 8. Arrestare l'applicazione

Terminata la sessione, premere **Ctrl + C** nel terminale Angular e nel terminale Spring Boot. PostgreSQL può restare attivo come servizio di Windows.

**Sequenza da ricordare:** PostgreSQL → backend Spring Boot → frontend Angular.
