# Localization Guide

Das Localization System bietet Mehrsprachigkeit mit automatischer Fallback-Logik und Placeholder-Unterstützung.

## Überblick

Das Localization System unterstützt:

- **Mehrere Sprachen** - Beliebige Anzahl von Sprachen
- **Placeholder** - Dynamische Werte in Übersetzungen
- **Fallback** - Automatischer Fallback auf Standard-Sprache
- **Plugin-Integration** - Mehrere Plugins können Übersetzungen bereitstellen
- **Message Formatting** - Automatisches Parsen von Formatierungs-Tags

## Setup

### 1. Language Files erstellen

Erstelle JSON-Dateien in `src/main/resources/lang/{language}/*.json`:

**`src/main/resources/lang/en/command.json`:**
```json
{
  "command.usage": "Usage: /{command} {args}",
  "command.language.set": "Language set to {language}",
  "command.language.available": "Available languages: {languages}"
}
```

**`src/main/resources/lang/de/command.json`:**
```json
{
  "command.usage": "Verwendung: /{command} {args}",
  "command.language.set": "Sprache auf {language} gesetzt",
  "command.language.available": "Verfügbare Sprachen: {languages}"
}
```

### 2. LocalizationService initialisieren

```java
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LocalizationService;

public class MyPlugin extends JavaPlugin {
    
    private LocalizationService localizationService;
    
    @Override
    protected void setup() {
        super.setup();
        
        HytaleApi api = HytaleProvider.getApi();
        this.localizationService = api.getLocalizationService();
        
        // Plugin's Language Source registrieren
        localizationService.registerLanguageSource(getClass().getClassLoader());
    }
}
```

## Übersetzungen verwenden

### Einfache Übersetzung

```java
import dev.spacetivity.tobi.hylib.hytale.api.localization.LangKey;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import com.hypixel.hytale.server.core.Message;

// LangKey erstellen
LangKey key = LangKey.of("command.usage");

// Übersetzen (mit Standard-Sprache)
Message message = localizationService.translate(key);

// Übersetzen (mit spezifischer Sprache)
Message message = localizationService.translate(key, Lang.GERMAN);
```

### Mit Placeholders

```java
import dev.spacetivity.tobi.hylib.hytale.api.localization.Placeholder;

// Placeholder definieren
Placeholder commandPlaceholder = Placeholder.of("command", "help");
Placeholder argsPlaceholder = Placeholder.of("args", "<player>");

// Übersetzen mit Placeholders
LangKey key = LangKey.of("command.usage");
Message message = localizationService.translate(
    key,
    commandPlaceholder,
    argsPlaceholder
);
```

### Raw String (ohne Message-Formatting)

```java
// Übersetzung als String (ohne Message-Parsing)
String translation = localizationService.getRawTranslation(
    LangKey.of("command.usage"),
    Lang.ENGLISH,
    Placeholder.of("command", "help")
);
```

## Verfügbare Sprachen

### Standard-Sprache abrufen

```java
Lang defaultLang = localizationService.getDefaultLanguage();
```

### Verfügbare Sprachen abrufen

```java
Set<Lang> availableLangs = localizationService.getAvailableLanguages();
```

### Sprache prüfen

```java
boolean isAvailable = localizationService.isLanguageAvailable(Lang.GERMAN);
boolean hasKey = localizationService.hasKey(LangKey.of("command.usage"), Lang.ENGLISH);
```

## Placeholder

### Placeholder erstellen

```java
// Einfacher Placeholder
Placeholder namePlaceholder = Placeholder.of("name", "John");

// Mehrere Placeholders
Placeholder namePlaceholder = Placeholder.of("name", "John");
Placeholder agePlaceholder = Placeholder.of("age", "25");
```

### Placeholder in JSON

```json
{
  "player.info": "Player {name} is {age} years old"
}
```

### Placeholder verwenden

```java
LangKey key = LangKey.of("player.info");
Message message = localizationService.translate(
    key,
    Placeholder.of("name", "John"),
    Placeholder.of("age", "25")
);
```

## Reload

### Language Files neu laden

```java
// Alle Language Files neu laden
localizationService.reload();
```

**Hinweis:** Fehler beim Reload werden geloggt, bestehende Übersetzungen bleiben erhalten.

## Plugin-Integration

### Mehrere Plugins registrieren

```java
// Plugin 1
localizationService.registerLanguageSource(plugin1ClassLoader);

// Plugin 2
localizationService.registerLanguageSource(plugin2ClassLoader);
```

**Wichtig:** Später registrierte Sources überschreiben existierende Keys!

## Lang Enum

### Verfügbare Sprachen

```java
Lang.ENGLISH    // en
Lang.GERMAN     // de
Lang.FRENCH     // fr
Lang.SPANISH    // es
// ... weitere Sprachen
```

### Custom Language

```java
// Custom Language erstellen
Lang customLang = Lang.of("custom", "Custom Language");
```

## Best Practices

1. **LangKey als Konstanten** - Definiere LangKeys als `public static final`
2. **Placeholder validieren** - Stelle sicher, dass alle Placeholders vorhanden sind
3. **Fallback nutzen** - Nutze die Standard-Sprache als Fallback
4. **Message Formatting** - Nutze Formatierungs-Tags in Übersetzungen
5. **Plugin-Namespace** - Verwende Plugin-spezifische Key-Präfixe

## Beispiel: Vollständige Integration

```java
public class MyPlugin extends JavaPlugin {
    
    private LocalizationService localizationService;
    
    // LangKeys als Konstanten
    private static final LangKey COMMAND_USAGE = LangKey.of("command.usage");
    private static final LangKey LANGUAGE_SET = LangKey.of("command.language.set");
    
    @Override
    protected void setup() {
        super.setup();
        
        HytaleApi api = HytaleProvider.getApi();
        this.localizationService = api.getLocalizationService();
        
        // Language Source registrieren
        localizationService.registerLanguageSource(getClass().getClassLoader());
    }
    
    public void sendUsageMessage(PlayerRef player, String command) {
        Message message = localizationService.translate(
            COMMAND_USAGE,
            Placeholder.of("command", command),
            Placeholder.of("args", "<player>")
        );
        player.sendMessage(message);
    }
    
    public void sendLanguageSetMessage(PlayerRef player, Lang lang) {
        Message message = localizationService.translate(
            LANGUAGE_SET,
            Placeholder.of("language", lang.displayName())
        );
        player.sendMessage(message);
    }
}
```

## Nächste Schritte

- **[Message Formatting](Message-Formatting)** - Formatierte Nachrichten mit Tags
- **[Player Management](Player-Management)** - Player-Sprach-Präferenzen
