# Design system Dueffe

Il linguaggio visivo di Dueffe Home, separato dall'app perché si possa riusare in altre app.
Tema solo scuro: fondo quasi nero, superfici calde, testo avorio, accento oro.

Fonti in questo repository, da tenere allineate con questo file:

| Piattaforma | Token | Componenti |
|---|---|---|
| Android (Compose) | `app/src/main/java/it/dueffe/home/ui/theme/Theme.kt` (`DF`, `Motion`, tipografia) | `ui/Components.kt`, `ui/Backdrop.kt` |
| Web | `:root` di `web/styles.css` | classi di `web/styles.css` |

## Come usarlo in un'altra app

- **Web**: copia il blocco `:root` qui sotto e le classi dei componenti che servono. Nessuna dipendenza.
- **Compose**: copia gli oggetti `DF` e `Motion`, la `Typography` e `DueffeTheme` da `Theme.kt`,
  i font `bricolage` e `inter` (font variabili in `res/font/`), poi i componenti da `Components.kt`.
  `ProjectIcon` dipende dal modello `Project` del catalogo: in un'altra app passa un'immagine e un nome.
- Il font del logo (`font-dueffe`) e il marchio "ff" sono del brand Dueffe: usali solo nelle app Dueffe.

## Colori

| Token | Web | Compose | Valore | Uso |
|---|---|---|---|---|
| Fondo | `--bg` | `DF.Bg` | `#050505` | Sfondo di pagina |
| Fondo rialzato | `--bg-elevated` | `DF.BgElevated` | `#0B0A08` | Fogli e pannelli sopra il fondo |
| Superficie | `--surface` | `DF.Surface` | `#11100D` | Chip, tasti secondari, fine del gradiente delle card |
| Superficie alta | `--surface-high` | `DF.SurfaceHigh` | `#1A1711` | Inizio del gradiente delle card, sfondo delle icone |
| Superficie più alta | `--surface-higher` | `DF.SurfaceHigher` | `#242016` | Toast, binari delle barre di avanzamento |
| Contenitore | `--surface-container` | `DF.SurfaceContainer` | `#12100C` al 94% | Superfici sopra contenuti che scorrono |
| Testo | `--text` | `DF.Text` | `#FFFAF0` | Testo principale (avorio) |
| Testo secondario | `--muted` | `DF.Muted` | `#B8AA8D` | Sottotitoli, metadati, icone inattive |
| Linea | `--line` | `DF.Line` | `#FFDC80` al 16% | Bordi di card, chip, icone |
| Linea forte | `--line-strong` | `DF.LineStrong` | `#FFD84D` al 34% | Bordi di tasti secondari, toast, campi |
| Primario | `--primary` | `DF.Primary` | `#FFD84D` | Accento oro: azioni, selezione, parole evidenziate |
| Primario forte | `--primary-strong` | `DF.PrimaryStrong` | `#FFB800` | Fine dei gradienti oro, sottolineature |
| Secondario | `--secondary` | `DF.Secondary` | `#FFF0AD` | Oro chiaro: pulviscolo, testo su contenitori primari |
| Sul primario | `--on-primary` | `DF.OnPrimary` | `#161006` | Testo e icone sopra l'oro |
| Successo | `--success` | `DF.Success` | `#A8F0B0` | Esiti positivi |
| Errore | `--error` | `DF.Error` | `#FF9B85` | Errori, pallini di notifica |
| Barra di navigazione | `--nav` | `DF.Nav` | `#080705` al 90–96% | Barra in basso, con sfocatura |

Regole:

- Un solo accento per schermata: l'oro. Le azioni principali sono oro pieno, le secondarie superficie con linea forte.
- Un colore d'accento diverso (per esempio estratto dall'icona di un progetto, `rememberAccent`) sostituisce
  `--primary` solo dentro la sua scheda. Il testo sopra un accento è `--on-primary` se l'accento è chiaro
  (luminanza > 0,45), altrimenti `--text`.
- Le tinte leggere di un colore sono il colore al 14% di sfondo e al 32% di bordo (vedi Pill), al 14% per
  lo stato selezionato di una riga.

```css
:root {
  --bg: #050505;
  --bg-elevated: #0b0a08;
  --surface: #11100d;
  --surface-high: #1a1711;
  --surface-higher: #242016;
  --surface-container: #12100cf0;
  --text: #fffaf0;
  --muted: #b8aa8d;
  --line: #ffdc8029;
  --line-strong: #ffd84d57;
  --primary: #ffd84d;
  --primary-strong: #ffb800;
  --secondary: #fff0ad;
  --on-primary: #161006;
  --success: #a8f0b0;
  --error: #ff9b85;
  --nav: #080705e6;
  --ease-reveal: cubic-bezier(.22, .78, .16, 1);
  --ease-mark: cubic-bezier(.2, .82, .2, 1);
  --ease-letter: cubic-bezier(.16, .86, .24, 1);
  --ease-std: cubic-bezier(.4, 0, .2, 1);
  --ease-spring: cubic-bezier(.34, 1.56, .64, 1);
  --display: "Bricolage Grotesque", Inter, system-ui, sans-serif;
  --body: Inter, system-ui, -apple-system, "Segoe UI", sans-serif;
  --mono: ui-monospace, "Cascadia Code", "SF Mono", Consolas, monospace;
  --frame: 560px;
  color-scheme: dark;
}
```

## Tipografia

Due famiglie: **Bricolage Grotesque** per titoli e display, **Inter** per testo ed etichette.
Monospace di sistema per il codice.

| Stile | Classe web | Compose | Famiglia | Peso | Corpo / interlinea | Spaziatura |
|---|---|---|---|---|---|---|
| Display L | — | `displayLarge` | Bricolage | 800 | 48 / 48 | −0,035em |
| Display M | `.display-m` | `displayMedium` | Bricolage | 800 | 38 / 40 | −0,03em |
| Display S | `.display-s` | `displaySmall` | Bricolage | 700 | 30 / 34 | −0,02em |
| Headline M | `.headline-m` | `headlineMedium` | Bricolage | 700 | 26 / 30 | −0,02em |
| Headline S | `.headline-s` | `headlineSmall` | Bricolage | 700 | 22 / 26 | −0,01em |
| Title L | `.title-l` | `titleLarge` | Bricolage | 600 | 20 / 24 | — |
| Title M | `.title-m` | `titleMedium` | Bricolage | 600 | 16 / 20 | — |
| Title S | `.title-s` | `titleSmall` | Inter | 600 | 14 / 18 | — |
| Body L | `.body-l` | `bodyLarge` | Inter | 400 | 16 / 24 | — |
| Body M | `.body-m` | `bodyMedium` | Inter | 400 | 14 / 20 | — |
| Body S | `.body-s` | `bodySmall` | Inter | 400 | 12 / 16 | — |
| Label L | `.label-l` | `labelLarge` | Inter | 600 | 14 / 18 | — |
| Label M | `.label-m` | `labelMedium` | Inter | 600 | 12 / 16 | — |
| Label S | `.label-s` | `labelSmall` | Inter | 500 | 11 / 14 | +0,04em |

Regole:

- Il titolo di una pagina è Display S; il titolo di una sezione Headline S con sottotitolo Body M in `--muted`.
- Le etichette di tasti e schede stanno su una riga e si rimpiccioliscono fino a entrare (`FitText`, minimo 8sp)
  invece di essere tagliate.
- Una parola chiave di un titolo può essere oro, con una sottolineatura disegnata a mano in `--primary-strong`.

## Forme e misure

| Elemento | Raggio |
|---|---|
| Card | 26 (20 per le card piccole, come le statistiche) |
| Tasto | 18 (14 nella versione compatta) |
| Banner | 22 |
| Barra di navigazione | 28, indicatore 22 |
| Toast | 18 |
| Campo di testo | 16 |
| Icona di app | 27% del lato (squircle) |
| Chip, pill, avatar, tasti tondi | completamente arrotondati |

- Margine laterale delle pagine: **20** (32 su schermi da 1000px in su).
- Bordi: sempre 1px in `--line` o `--line-strong`; nessuna ombra, la profondità viene dai gradienti e dai bordi.
- Su web il contenuto sta in una colonna di `--frame` (560px), più larga su schermi grandi.
- Distanze verticali ricorrenti: 4, 6, 8, 10, 12, 14, 16, 18, 22, 26, 30.

## Movimento

| Curva | Web | Compose | Uso |
|---|---|---|---|
| Reveal | `--ease-reveal` | `Motion.Reveal` | Entrata di pagine e dettagli, aperture |
| Mark | `--ease-mark` | `Motion.Mark` | Comparsa del marchio |
| Letter | `--ease-letter` | `Motion.Letter` | Entrate a cascata, lettere e parole |
| Standard | `--ease-std` | `Motion.Standard` | Transizioni brevi, pressione |
| Molla | `--ease-spring` | `spring(dampingRatio 0.42, stiffness 380)` | Rilascio di ciò che è stato premuto |

Pattern:

- **Pressione a molla** (`.press`, `Modifier.pressScale`): premuto si comprime a 0,94 (0,92 i chip, 0,95 i tasti,
  0,97 le righe) e al rilascio rimbalza. Sostituisce l'effetto ripple: nessuna indicazione Material.
- **Entrata a cascata** (`.reveal`, `Modifier.reveal(i)`): dissolvenza, salita di 42 e zoom da 0,94, 620ms
  con curva Letter, 55ms di ritardo per elemento fino al decimo.
- **Pagine**: entrano scorrendo dell'8% di lato in 460ms (Reveal); il dettaglio sale del 7% in 520ms.
- **Segnaposto di caricamento** (`.shimmer`, `Modifier.shimmer`): riflesso tra Surface High e Surface Higher, 1,4s.
- **Respiro** (`breathing`): pulsazione lenta 0..1..0 in 3,2s per elementi decorativi.
- **Indicatore "liquido"** della barra di navigazione: il bordo verso cui va arriva prima, l'altro dopo
  (260ms e 620ms), così l'indicatore si allunga e si richiude.
- **Animazioni ridotte**: con `prefers-reduced-motion` (web) o la scala animazioni di Android a 0
  (`animationsDisabled`) tutto diventa istantaneo.

## Componenti

Ogni componente esiste uguale su Android e sul web.

| Componente | Compose | Web | Aspetto |
|---|---|---|---|
| Tasto principale | `GoldButton(filled = true)` | `.btn` | Gradiente a 135° dall'accento a un misto con Primary Strong, testo Sul primario, Label L 15, padding 14×18, raggio 18 |
| Tasto secondario | `GoldButton(filled = false)` | `.btn.ghost` | Superficie, bordo Linea forte, testo avorio |
| Tasto compatto | `GoldButton(dense = true)` | `.btn.small` | Raggio 14, padding ridotto, testo 13–14 |
| Chip | `DfChip` | `.chip` / `.chip.on` | Pillola Superficie con bordo Linea; selezionato diventa oro pieno; icona 16 facoltativa; colori in 260ms |
| Pill | `Pill` | `.pill` | Etichetta Label S in un colore, con sfondo al 14% e bordo al 32% dello stesso colore |
| Card | `CardSurface` | `.card` | Gradiente verticale da Surface High a Surface, bordo Linea, raggio 26, padding 18 |
| Intestazione di sezione | `SectionHeader` | `.section-head` | Headline S e sottotitolo Muted; a destra un'azione oro con freccia |
| Icona di app | `ProjectIcon` | `.icon` | Squircle 27% su Surface High con bordo Linea; senza immagine un monogramma su gradiente dal colore del nome |
| Barra di navigazione | `BottomNav` in `DueffeHomeApp.kt` | `#nav` | Barra flottante alta 68, raggio 28, colore Nav con sfocatura 18; indicatore oro sotto la scheda scelta, l'icona scelta rimbalza |
| Toast | messaggio di `vm.toast` in `DueffeHomeApp.kt` | `#toast` | Surface Higher, bordo Linea forte, raggio 18, entra dal basso a molla |
| Banner | — | `.banner` | Fascia oro raggio 22 con riflesso che scorre, per un annuncio importante (es. aggiornamento) |
| Campo di testo | — | `.pin-form input` | Fondo, bordo Linea forte, raggio 16; a fuoco bordo oro; errore bordo Errore e scossa |
| Barra di avanzamento | in `AppActionButton` | — | Alta 4, binario Surface Higher, riempimento nell'accento, estremità tonde |
| Testo che si adatta | `FitText` | — | Una riga che si rimpicciolisce invece di essere tagliata |

## Sfondi e momenti di brand

- **Aurora** (`AuroraBackground`, `#aurora`): sul fondo tre nebulose radiali oro e ambra (alfa 12–22%) che
  derivano in 32s, e 34 granelli di pulviscolo avorio che salgono e scintillano. Si sposta un poco con lo scorrimento.
- **Splash** (`SplashOverlay`, `#splash`): il marchio "ff" compare per primo, poi si aprono "Due", "e" e le lettere
  di "Home" in oro; esce con un'apertura circolare dal centro.
- **Titolo animato** (`AnimatedHeadline`, `.headline`): le parole salgono una alla volta ruotando in avanti, poi
  la parola oro riceve la sottolineatura disegnata.
- **Fondo del dettaglio** (`.hero-bg`): l'immagine del progetto sfocata (60px, 55%) con un alone dell'accento e
  una sfumatura verso il fondo.

## Accessibilità

- Testo avorio su fondo quasi nero e testo Sul primario sull'oro: contrasto ben oltre AA. Il Muted resta per testo
  secondario, mai per informazioni essenziali da solo.
- Bersagli di tocco di almeno 40–48.
- Ogni icona senza testo ha una descrizione (`contentDescription`, `aria-label`).
- Il movimento si spegne con le preferenze di sistema (vedi Movimento).
