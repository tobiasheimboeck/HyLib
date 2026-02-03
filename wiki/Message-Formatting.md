# Message Formatting

Der MessageParser konvertiert formatierte Strings mit Tags in Hytale Message-Objekte.

## Überblick

Der MessageParser unterstützt:

- **Farben** - Named Colors und Hex-Farben
- **Gradienten** - Farbverläufe zwischen mehreren Farben
- **Formatierung** - Bold, Italic, Underline, Monospace
- **Links** - Klickbare Links
- **Nested Tags** - Verschachtelte Formatierungen

## Grundlagen

### MessageParser verwenden

```java
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.api.message.MessageParser;
import com.hypixel.hytale.server.core.Message;

HytaleApi api = HytaleProvider.getApi();
MessageParser parser = api.getMessageParser();

// String mit Tags parsen
String text = "<red>Hello</red> <bold>World</bold>";
Message message = parser.parse(text);

// Message senden
player.sendMessage(message);
```

## Farben

### Named Colors

```java
// Verfügbare Named Colors:
// black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple,
// gold, gray, dark_gray, blue, green, aqua, red, light_purple,
// yellow, white

String text = "<red>Red Text</red>";
String text = "<blue>Blue Text</blue>";
String text = "<green>Green Text</green>";
```

### Hex-Farben

```java
// Hex-Format: #RRGGBB
String text = "<color:#FF0000>Red Text</color>";
String text = "<color:#00FF00>Green Text</color>";
String text = "<color:#0000FF>Blue Text</color>";

// Kurzform
String text = "<c:#FF0000>Red Text</c>";
```

### Color Tag

```java
// Mit color Tag
String text = "<color:red>Red Text</color>";
String text = "<color:#FF0000>Red Text</color>";

// Kurzform
String text = "<c:red>Red Text</c>";
```

## Gradienten

### Einfacher Gradient

```java
// Gradient zwischen zwei Farben
String text = "<gradient:red:blue>Gradient Text</gradient>";

// Kurzform
String text = "<grnt:red:blue>Gradient Text</grnt>";
```

### Multi-Color Gradient

```java
// Gradient mit mehreren Farben
String text = "<gradient:red:green:blue>Multi-Color Gradient</gradient>";
```

### Gradient mit Hex-Farben

```java
// Gradient mit Hex-Farben
String text = "<gradient:#FF0000:#00FF00:#0000FF>Gradient</gradient>";
```

## Formatierung

### Bold

```java
String text = "<bold>Bold Text</bold>";
String text = "<b>Bold Text</b>";
```

### Italic

```java
String text = "<italic>Italic Text</italic>";
String text = "<i>Italic Text</i>";
String text = "<em>Italic Text</em>";
```

### Underline

```java
String text = "<underline>Underlined Text</underline>";
String text = "<u>Underlined Text</u>";
```

### Monospace

```java
String text = "<monospace>Monospace Text</monospace>";
String text = "<mono>Monospace Text</mono>";
```

## Kombinierte Formatierung

### Mehrere Formatierungen

```java
// Bold + Red
String text = "<bold><red>Bold Red Text</red></bold>";

// Oder verschachtelt
String text = "<red><bold>Bold Red Text</bold></red>";
```

### Reset Tag

```java
// Alle Formatierungen zurücksetzen
String text = "<red>Red Text</red> <reset>Normal Text</reset>";

// Kurzform
String text = "<red>Red Text</red> <r>Normal Text</r>";
```

## Links

### Link erstellen

```java
// Link Tag
String text = "<link:https://example.com>Click here</link>";

// Kurzform
String text = "<url:https://example.com>Click here</url>";
```

### Link mit Formatierung

```java
// Link mit Farbe
String text = "<link:https://example.com><blue>Click here</blue></link>";
```

## Komplexe Beispiele

### Kombinierte Tags

```java
String text = "<gradient:red:blue><bold>Bold Gradient Text</bold></gradient>";
```

### Verschachtelte Formatierung

```java
String text = "<red><bold>Red Bold</bold> <italic>Red Italic</italic></red>";
```

### Mit Localization

```java
// In Language File: command.usage = "<red>Usage:</red> /{command} {args}"
LangKey key = LangKey.of("command.usage");
Message message = localizationService.translate(
    key,
    Placeholder.of("command", "help"),
    Placeholder.of("args", "<player>")
);
// Ergebnis: <red>Usage:</red> /help <player>
```

## Verfügbare Tags

### Farben

- `<red>`, `<blue>`, `<green>`, etc. (Named Colors)
- `<color:...>` oder `<c:...>` (Color Tag)
- `<gradient:...>` oder `<grnt:...>` (Gradient Tag)

### Formatierung

- `<bold>` oder `<b>` (Bold)
- `<italic>` oder `<i>` oder `<em>` (Italic)
- `<underline>` oder `<u>` (Underline)
- `<monospace>` oder `<mono>` (Monospace)
- `<reset>` oder `<r>` (Reset)

### Links

- `<link:...>` oder `<url:...>` (Link)

## Best Practices

1. **Tags schließen** - Schließe alle öffnenden Tags
2. **Verschachtelung** - Nutze verschachtelte Tags für komplexe Formatierungen
3. **Reset verwenden** - Nutze `<reset>` um Formatierungen zurückzusetzen
4. **Mit Localization** - Kombiniere mit Localization für mehrsprachige Nachrichten
5. **Performance** - Parse Messages einmal und cachen sie bei Bedarf

## Beispiel: Vollständige Integration

```java
public class MessageService {
    
    private final MessageParser parser;
    
    public MessageService(MessageParser parser) {
        this.parser = parser;
    }
    
    public void sendWelcomeMessage(PlayerRef player) {
        String text = "<gradient:green:blue><bold>Welcome</bold></gradient> to the server!";
        Message message = parser.parse(text);
        player.sendMessage(message);
    }
    
    public void sendErrorMessage(PlayerRef player, String error) {
        String text = "<red><bold>Error:</bold></red> " + error;
        Message message = parser.parse(text);
        player.sendMessage(message);
    }
    
    public void sendLinkMessage(PlayerRef player) {
        String text = "Visit our website: <link:https://example.com><blue>Click here</blue></link>";
        Message message = parser.parse(text);
        player.sendMessage(message);
    }
}
```

## Nächste Schritte

- **[Localization Guide](Localization-Guide)** - Mehrsprachigkeit implementieren
- **[Player Management](Player-Management)** - Player-Nachrichten senden
