# HyLib Wiki

Willkommen im HyLib Wiki! Diese Dokumentation hilft dir dabei, HyLib effektiv in deinen Hytale-Server-Plugins zu verwenden.

## Was ist HyLib?

HyLib ist eine umfassende Utility-Bibliothek für die Hytale-Server-Plugin-Entwicklung. Sie bietet:

- **Type-Safe Database Operations** - Fluent Builder API mit Compile-Time Safety
- **SQL Injection Protection** - Validierte Identifier verhindern häufige Sicherheitslücken
- **Flexible Configuration Management** - Type-safe Config API mit Fluent DSL
- **Multi-Language Localization** - Übersetzungssystem mit mehreren Sprachen
- **Rich Message Formatting** - Parse und rendere formatierte Nachrichten (Farben, Gradienten, Formatierung)
- **Player Management** - Integrierter Player-Service mit Datenbank-Integration

## Schnellstart

1. **[Installation](Installation)** - HyLib einrichten und konfigurieren
2. **[Database Guide](Database-Guide)** - Datenbankverbindungen und Queries
3. **[Configuration Guide](Configuration-Guide)** - Type-safe Konfigurationen erstellen
4. **[Localization Guide](Localization-Guide)** - Mehrsprachigkeit implementieren

## Dokumentation

### Database Module

- **[Database Guide](Database-Guide)** - Überblick über das Database API
- **[SQL Query Builder](SQL-Query-Builder)** - Type-safe SQL Queries erstellen
- **[Repository Pattern](Repository-Pattern)** - Repositories für Datenbankoperationen
- **[Cache System](Cache-System)** - In-Memory Caching

### Hytale Module

- **[Configuration Guide](Configuration-Guide)** - Type-safe Konfigurationen mit CodecBuilder
- **[Localization Guide](Localization-Guide)** - Mehrsprachigkeit und Übersetzungen
- **[Message Formatting](Message-Formatting)** - Formatierte Nachrichten mit Tags
- **[Player Management](Player-Management)** - Player-Daten und Sprach-Präferenzen

## Architektur

HyLib folgt einer modularen Architektur:

- **Database Modules**: Unabhängig von Hytale, verwendbar in jeder Java-Anwendung
- **Hytale Modules**: Basiert auf Database-Modulen, fügt Hytale-spezifische Funktionalität hinzu
- **Provider Pattern**: Zentrale Registries (`DatabaseProvider`, `HytaleProvider`) für Dependency Injection
- **Repository Pattern**: Basis-Repository-Klasse mit automatischen CRUD-Operationen

## Module

- **`database-api`**: Core Interfaces, Query Builder, RowMapper (keine Hytale Dependencies)
- **`database-common`**: Standard-Implementierungen (MariaDB Connector, Connection Handler)
- **`hytale-api`**: Hytale-spezifische API Interfaces (Codec Builder, Localization, Messages)
- **`hytale-common`**: Implementierungen (Codec Builder, JSON Language Loader, Message Parser)
- **`hytale-plugin`**: Komplettes Hytale Plugin kombiniert beide APIs

## Anforderungen

- **Java 21+**
- **Hytale Server** (für Hytale Module)
- **MariaDB** (für Database-Funktionalität)
- **Gradle 8.0+** (für Builds)

## Support

- **GitHub Issues**: [Bugs melden oder Features anfragen](https://github.com/Spacetivity/HyLib/issues)
- **Discord**: Trete unserem [Discord Server](https://discord.spacetivity.dev) für Community-Support bei

## Credits

Entwickelt von **Spacetivity** — Tobias Heimböck-Kramesberger (DeveloperTobi)
