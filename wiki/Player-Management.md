# Player Management

Der HyPlayerService bietet Verwaltung von Player-Daten, Sprach-Präferenzen und Online/Offline-Status.

## Überblick

Der HyPlayerService unterstützt:

- **Online/Offline Players** - Verwaltung von Online- und Offline-Playern
- **Language Preferences** - Spieler-spezifische Sprach-Präferenzen
- **Database Integration** - Automatische Datenbank-Synchronisation
- **Caching** - In-Memory Caching für schnellen Zugriff
- **ECS Integration** - Synchronisation mit Hytale ECS Store

## Setup

### HyPlayerService initialisieren

```java
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;

public class MyPlugin extends JavaPlugin {
    
    private HyPlayerService playerService;
    
    @Override
    protected void setup() {
        super.setup();
        
        HytaleApi api = HytaleProvider.getApi();
        this.playerService = api.getHyPlayerService();
    }
}
```

## Online Players

### Online Player abrufen

```java
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayer;
import java.util.UUID;

// By UUID
UUID playerId = UUID.fromString("...");
HyPlayer player = playerService.getOnlineHyPlayer(playerId);

// By Username
HyPlayer player = playerService.getOnlineHyPlayer("PlayerName");

// By PlayerRef
HyPlayer player = playerService.getOnlineHyPlayer(playerRef);

// Alle Online Players
Set<HyPlayer> onlinePlayers = playerService.getOnlineHyPlayers();
```

### Online Status prüfen

```java
HyPlayer player = playerService.getOnlineHyPlayer(playerId);
if (player != null) {
    // Player ist online
    Lang playerLang = player.getLanguage();
    String username = player.getUsername();
}
```

## Offline Players

### Offline Player abrufen

```java
import java.util.concurrent.CompletableFuture;

// By UUID (async)
CompletableFuture<HyPlayer> playerFuture = playerService.getOfflineHyPlayer(playerId);
playerFuture.thenAccept(player -> {
    if (player != null) {
        // Player gefunden
    }
});

// By Username (async)
CompletableFuture<HyPlayer> playerFuture = playerService.getOfflineHyPlayer("PlayerName");

// Alle Offline Players (async)
CompletableFuture<List<HyPlayer>> playersFuture = playerService.getOfflineHyPlayers();
```

## Player erstellen

### Neuen Player erstellen

```java
UUID playerId = UUID.fromString("...");
String username = "PlayerName";

// Player erstellen (mit Standard-Sprache)
playerService.createHyPlayer(playerId, username);
```

## Player löschen

### Player löschen

```java
UUID playerId = UUID.fromString("...");

// Player löschen (aus Datenbank und Cache)
playerService.deleteHyPlayer(playerId);
```

## Language Preferences

### Sprache ändern

```java
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;

UUID playerId = UUID.fromString("...");
Lang newLang = Lang.GERMAN;

// Sprache ändern (Datenbank + Cache)
playerService.changeLanguage(playerId, newLang);
```

### Sprache mit ECS synchronisieren

```java
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

UUID playerId = UUID.fromString("...");
Lang newLang = Lang.GERMAN;
Ref<EntityStore> playerRef = ...;
Store<EntityStore> store = ...;

// Sprache ändern und mit ECS synchronisieren
playerService.setLanguageAndSync(playerId, newLang, playerRef, store);
```

### Player-Sprache abrufen

```java
HyPlayer player = playerService.getOnlineHyPlayer(playerId);
if (player != null) {
    Lang playerLang = player.getLanguage();
    
    // Mit Localization verwenden
    Message message = localizationService.translate(
        LangKey.of("welcome"),
        playerLang
    );
}
```

## Username ändern

### Username aktualisieren

```java
UUID playerId = UUID.fromString("...");
String newUsername = "NewPlayerName";

// Username ändern (Datenbank + Cache)
playerService.changeUsername(playerId, newUsername);
```

## Caching

### Player laden und cachen

```java
import java.util.function.Consumer;

UUID playerId = UUID.fromString("...");

// Player laden und automatisch cachen
playerService.loadHyPlayer(playerId, player -> {
    if (player != null) {
        // Player geladen und gecacht
    }
});
```

### Player manuell cachen

```java
HyPlayer player = ...; // Player-Objekt

// Player manuell cachen
playerService.cacheHyPlayer(playerId, player);
```

### Player aus Cache entfernen

```java
UUID playerId = UUID.fromString("...");

// Player aus Cache entfernen
playerService.removeCachedHyPlayer(playerId);
```

## Player Listener

### Player Join/Leave Events

```java
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;

@EventHandler
public void onPlayerReady(PlayerReadyEvent event) {
    PlayerRef playerRef = event.getPlayerRef();
    UUID playerId = playerRef.getUniqueId();
    
    // Player laden und cachen
    playerService.loadHyPlayer(playerId, player -> {
        if (player != null) {
            // Player bereit
            Lang playerLang = player.getLanguage();
        }
    });
}

@EventHandler
public void onPlayerDisconnect(PlayerDisconnectEvent event) {
    PlayerRef playerRef = event.getPlayerRef();
    UUID playerId = playerRef.getUniqueId();
    
    // Player aus Cache entfernen (optional)
    playerService.removeCachedHyPlayer(playerId);
}
```

## Beispiel: Vollständige Integration

```java
public class PlayerManager {
    
    private final HyPlayerService playerService;
    private final LocalizationService localizationService;
    
    public PlayerManager(HyPlayerService playerService, LocalizationService localizationService) {
        this.playerService = playerService;
        this.localizationService = localizationService;
    }
    
    public void sendWelcomeMessage(PlayerRef playerRef) {
        UUID playerId = playerRef.getUniqueId();
        
        // Online Player abrufen
        HyPlayer player = playerService.getOnlineHyPlayer(playerRef);
        if (player == null) {
            // Falls nicht online, aus Datenbank laden
            playerService.loadHyPlayer(playerId, loadedPlayer -> {
                if (loadedPlayer != null) {
                    sendWelcomeMessage(playerRef, loadedPlayer);
                }
            });
            return;
        }
        
        sendWelcomeMessage(playerRef, player);
    }
    
    private void sendWelcomeMessage(PlayerRef playerRef, HyPlayer player) {
        Lang playerLang = player.getLanguage();
        LangKey welcomeKey = LangKey.of("welcome");
        
        Message message = localizationService.translate(
            welcomeKey,
            playerLang,
            Placeholder.of("player", player.getUsername())
        );
        
        playerRef.sendMessage(message);
    }
    
    public void changePlayerLanguage(PlayerRef playerRef, Lang newLang) {
        UUID playerId = playerRef.getUniqueId();
        
        // Sprache ändern und mit ECS synchronisieren
        Store<EntityStore> store = ...; // Store abrufen
        playerService.setLanguageAndSync(playerId, newLang, playerRef, store);
        
        // Bestätigungs-Nachricht senden
        LangKey key = LangKey.of("command.language.set");
        Message message = localizationService.translate(
            key,
            newLang,
            Placeholder.of("language", newLang.displayName())
        );
        playerRef.sendMessage(message);
    }
}
```

## Best Practices

1. **Online Players bevorzugen** - Nutze `getOnlineHyPlayer()` für bessere Performance
2. **Async für Offline** - Nutze `getOfflineHyPlayer()` für Offline-Zugriffe
3. **Cache nutzen** - Player werden automatisch gecacht
4. **Language Preferences** - Speichere Player-Sprache für personalisierte Nachrichten
5. **ECS Synchronisation** - Nutze `setLanguageAndSync()` für ECS-Integration

## Nächste Schritte

- **[Localization Guide](Localization-Guide)** - Mehrsprachigkeit implementieren
- **[Message Formatting](Message-Formatting)** - Formatierte Nachrichten senden
