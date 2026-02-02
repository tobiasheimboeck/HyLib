---
name: HyLib Message WebUI
overview: Statische Web-UI im Ordner `/webui` zum Bearbeiten und Vorschau von HyLib-Nachrichten im gleichen Tag-Format wie `MessageParserImpl` – ohne Backend, rein clientseitig mit Vite + TypeScript.
todos: []
isProject: false
---

# HyLib Message WebUI (MiniMessage-ähnlich)

## Ziel

Eine Web-Oberfläche ähnlich [MiniMessage Viewer](https://webui.advntr.dev/): Nutzer geben einen Text mit HyLib-Format-Tags ein und sehen sofort eine Vorschau. Alles läuft im Browser, **kein externes Backend**.

## Referenz: euer Message-Format

Die Logik steht in [MessageParserImpl.java](hytale-common/src/main/java/dev/spacetivity/tobi/hylib/hytale/common/api/message/MessageParserImpl.java):

- **Tag-Syntax**: `<tag>`, `</tag>`, `<tag:arg>` (Regex: `<(/?)([a-zA-Z0-9_]+)(?::([^>]+))?>`)
- **Farben**: Named colors (z. B. `black`, `dark_blue`, `red`, `white`) sowie `<color:#hex>` / `<c:#hex>`
- **Gradient**: `<gradient:color1:color2>` bzw. `<grnt:...>` mit Zeichen-für-Zeichen-Interpolation
- **Formatierung**: `<bold>`/`<b>`, `<italic>`/`<i>`/`<em>`, `<underline>`/`<u>`, `<monospace>`/`<mono>`
- **Link**: `<link:url>` / `<url:...>`
- **Reset**: `<reset>`/`<r>`

Die WebUI muss dieses Format **1:1** in TypeScript parsen und als HTML/CSS darstellen, damit die Vorschau zum Verhalten in Hytale passt.

---

## Tech-Stack (ohne Backend)


| Bereich    | Wahl                                     | Begründung                                                                                       |
| ---------- | ---------------------------------------- | ------------------------------------------------------------------------------------------------ |
| Build      | **Vite**                                 | Schnell, einfache Static-SPA, kein Server nötig                                                  |
| Sprache    | **TypeScript**                           | Typsicherer Parser, gute Wartbarkeit                                                             |
| UI         | **Vanilla TS + DOM**                     | Kein Framework nötig, geringe Abhängigkeiten; bei Bedarf später React/Vue ergänzbar              |
| Styling    | **CSS** (evtl. CSS Variables für Themes) | Ausreichend für Chat-Vorschau (Farben, bold, italic, underline, monospace, Links)                |
| Deployment | Statische Dateien                        | `npm run build` → `webui/dist/`; hostbar via GitHub Pages, Netlify, oder lokal `npm run preview` |


**Kein Backend**: Alles läuft im Browser. Optional „Teilen“ über URL-Hash/Fragment (z. B. `#input=...`), damit man Links ohne Server teilen kann.

---

## Ordnerstruktur

```
webui/
├── index.html              # Einstieg
├── package.json
├── tsconfig.json
├── vite.config.ts
├── src/
│   ├── main.ts             # Einstieg, DOM-Bindung
│   ├── parser.ts           # Port von MessageParserImpl (Tags → interne Struktur)
│   ├── render.ts           # Struktur → HTML/CSS (inkl. Gradient pro Zeichen)
│   ├── ui/
│   │   ├── input.ts        # Textarea + Toolbar (Tag-Buttons)
│   │   ├── preview.ts      # Live-Vorschau (Chat-ähnlich)
│   │   └── docs.ts         # Kurze Liste unterstützter Tags
│   └── style.css
└── public/                 # optional: Favicon etc.
```

Das Projekt bleibt ein reines Frontend-Projekt (eigenes `package.json`), kein Gradle-Modul nötig.

---

## Kernkomponenten

### 1. Parser (`parser.ts`)

- Gleiche Tag-Regex und -Logik wie in Java (Stack für verschachtelte Tags, gleiche Farbnamen und Hex-Parsing).
- Ausgabe: einfache Struktur (z. B. Liste von Segmenten mit `{ text, color?, gradient?, bold?, italic?, underline?, monospace?, link? }`).
- Gradient: wie in Java Zeichen für Zeichen interpolieren und pro Zeichen eine Farbe ausgeben.

### 2. Renderer (`render.ts`)

- Segment-Liste → HTML (z. B. `<span>` mit style/class).
- Gradient: pro Zeichen ein `<span style="color: ...">` oder ähnlich.
- Styles für bold, italic, underline, monospace, link (cursor, underline, evtl. click-Handling nur für Vorschau).

### 3. UI

- **Input**: Eine Textarea mit dem Rohtext (Tag-String).
- **Toolbar**: Buttons zum Einfügen von Tags (z. B. Bold, Italic, Color-Dropdown, Gradient, Link) an Cursorposition.
- **Preview**: Bereich unterhalb (oder daneben), der bei jeder Änderung (z. B. `input`/`keyup`) neu parst und rendert.
- **Kurze Doku**: Collapsible-Bereich „Unterstützte Tags“ mit Liste (color, gradient, bold, italic, underline, monospace, link, reset) und Beispielen.

### 4. Optionale Features (später)

- **Teilen**: Input in URL-Hash encoden (`#m=...` oder Base64); beim Laden dekodieren und in Textarea übernehmen.
- **Placeholder-Vorschau**: Optionales Feld „Placeholders“ (z. B. `{0}=Spieler`, `{1}=5`) und Anzeige des Ergebnisses nach Ersetzung vor dem Tag-Parsing (nur für Vorschau, keine Backend-Logik).
- **Export**: Button „Copy“ für den fertigen Tag-String (z. B. für Lang-Dateien oder Code).

---

## Abgrenzung zu Adventure WebUI

- Adventure nutzt Ktor + Kotlin/JS und optional ein Backend für Editor-Tokens. Ihr wollt **kein Backend** → reines statisches Build.
- Ihr habt ein eigenes Tag-Format (nicht MiniMessage) → eigener Parser in TS, angelehnt an `MessageParserImpl`.
- Preview-Kontext: vorerst eine „Chat“-Vorschau; bei Bedarf später weitere Kontexte (z. B. Lore/Hover), wenn Hytale das trennt.

---

## Nächste Schritte (Implementierung)

1. `**webui/` anlegen**: `package.json` (Vite, TypeScript), `vite.config.ts`, `tsconfig.json`, `index.html`.
2. **Parser portieren**: In `parser.ts` Regex, Farbtabelle, Tag-Handling und Gradient-Interpolation aus `MessageParserImpl` nachbilden.
3. **Renderer**: Segment-Liste in HTML/CSS umsetzen (inkl. Gradient pro Zeichen).
4. **UI**: `main.ts` verbindet Input + Preview; Toolbar und Tag-Doku einbauen.
5. **Doku**: In README oder in der App kurz beschreiben, dass die WebUI das gleiche Format wie `HyMessages.parse()` verwendet.

Optional: GitHub Actions oder Skript, das `npm run build` ausführt und `webui/dist/` z. B. für GitHub Pages bereitstellt (z. B. unter `/webui` oder eigenem Branch).

Wenn du willst, können wir als Nächstes mit dem genauen Parser-API-Design (Ein-/Ausgabe) und den konkreten Vite-/TS-Dateien starten.