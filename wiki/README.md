# GitHub Wiki Setup Anleitung

Diese Dateien sind für das GitHub Wiki vorbereitet. Um das Wiki zu aktivieren und diese Dokumentation zu verwenden:

## GitHub Wiki aktivieren

1. Gehe zu deinem Repository auf GitHub
2. Klicke auf **Settings** → **Features**
3. Aktiviere **Wikis** (falls noch nicht aktiviert)
4. Klicke auf **Wiki** im Repository-Menü

## Wiki-Seiten hochladen

Das GitHub Wiki ist ein separates Git-Repository. Du kannst die Seiten auf zwei Arten hochladen:

### Option 1: Über GitHub Web Interface

1. Gehe zum Wiki-Tab deines Repositories
2. Klicke auf **Create the first page** oder **New Page**
3. Kopiere den Inhalt aus den entsprechenden `.md` Dateien in diesem Verzeichnis
4. Speichere die Seite

### Option 2: Über Git Clone

```bash
# Wiki Repository klonen
git clone https://github.com/Spacetivity/HyLib.wiki.git

# Dateien kopieren
cp wiki/*.md HyLib.wiki/

# Committen und pushen
cd HyLib.wiki
git add .
git commit -m "Add wiki documentation"
git push origin master
```

## Wiki-Struktur

Die folgenden Seiten sollten erstellt werden:

1. **Home.md** - Hauptseite mit Übersicht
2. **Installation.md** - Installations-Anleitung
3. **Database-Guide.md** - Database API Guide
4. **SQL-Query-Builder.md** - SQL Query Builder Dokumentation
5. **Repository-Pattern.md** - Repository Pattern Guide
6. **Configuration-Guide.md** - Configuration API Guide
7. **Localization-Guide.md** - Localization System Guide
8. **Message-Formatting.md** - Message Formatting Guide
9. **Player-Management.md** - Player Management Guide
10. **Cache-System.md** - Cache System Guide

## Seiten-Reihenfolge

Die Seiten sollten in dieser Reihenfolge erstellt werden, damit die Links funktionieren:

1. Home.md (wird automatisch als Startseite verwendet)
2. Installation.md
3. Database-Guide.md
4. SQL-Query-Builder.md
5. Repository-Pattern.md
6. Cache-System.md
7. Configuration-Guide.md
8. Localization-Guide.md
9. Message-Formatting.md
10. Player-Management.md

## Links aktualisieren

Nach dem Hochladen der Seiten funktionieren alle internen Links automatisch, da GitHub Wiki die Markdown-Links automatisch in Wiki-Links konvertiert.

## Hinweis

- Die Dateinamen im Wiki sollten **ohne Leerzeichen** sein (z.B. `Database-Guide.md` statt `Database Guide.md`)
- GitHub Wiki unterstützt Markdown-Links zwischen Seiten automatisch
- Die Home-Seite wird automatisch als Startseite verwendet
