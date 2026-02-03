# Configuration Guide

Der CodecBuilder bietet eine type-safe, fluent API für die Erstellung von Konfigurationen mit automatischer Serialisierung/Deserialisierung.

## Überblick

Der CodecBuilder ermöglicht:

- **Type-Safe Configuration** - Fluent DSL mit Method References
- **Automatische Serialisierung** - JSON/YAML Serialisierung
- **Default Values** - Optionale Standardwerte für Felder
- **Compile-Time Safety** - Type-Checking zur Compile-Zeit

## Konfiguration erstellen

### 1. Config-Klasse definieren

```java
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DbConfig {
    
    private String hostname;
    private Integer port;
    private String username;
    private String database;
    private String password;
}
```

### 2. Codec erstellen

```java
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;

public class DbConfig {
    
    private String hostname;
    private Integer port;
    private String username;
    private String database;
    private String password;
    
    public static BuilderCodec<DbConfig> CODEC = HytaleProvider.getApi()
        .newCodec(DbConfig.class)
        .field("Hostname", Codec.STRING, DbConfig::setHostname, DbConfig::getHostname)
        .withDefault("localhost")
        .and()
        .field("Port", Codec.INTEGER, DbConfig::setPort, DbConfig::getPort)
        .withDefault(5520)
        .and()
        .field("Username", Codec.STRING, DbConfig::setUsername, DbConfig::getUsername)
        .withDefault("root")
        .and()
        .field("Database", Codec.STRING, DbConfig::setDatabase, DbConfig::setDatabase)
        .withDefault("game_db")
        .and()
        .field("Password", Codec.STRING, DbConfig::setPassword, DbConfig::getPassword)
        .withDefault("password")
        .and()
        .build();
}
```

## Konfiguration verwenden

### In Hytale Plugin

```java
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.util.Config;

public class MyPlugin extends JavaPlugin {
    
    private Config<DbConfig> dbConfig;
    
    public MyPlugin(JavaPluginInit init) {
        super(init);
        this.dbConfig = withConfig("DbConfig", DbConfig.CODEC);
    }
    
    @Override
    protected void setup() {
        super.setup();
        
        // Config speichern (erstellt Datei mit Default-Werten falls nicht vorhanden)
        this.dbConfig.save();
        
        // Config lesen
        DbConfig config = dbConfig.get();
        String hostname = config.getHostname();
        int port = config.getPort();
    }
}
```

## Feld-Typen

### String

```java
.field("Name", Codec.STRING, Config::setName, Config::getName)
```

### Integer

```java
.field("Port", Codec.INTEGER, Config::setPort, Config::getPort)
```

### Boolean

```java
.field("Enabled", Codec.BOOLEAN, Config::setEnabled, Config::getEnabled)
```

### Double

```java
.field("Price", Codec.DOUBLE, Config::setPrice, Config::getPrice)
```

### List

```java
.field("Items", Codec.listOf(Codec.STRING), Config::setItems, Config::getItems)
```

### Map

```java
.field("Settings", Codec.mapOf(Codec.STRING, Codec.INTEGER), Config::setSettings, Config::getSettings)
```

### Nested Config

```java
.field("Database", DatabaseConfig.CODEC, Config::setDatabase, Config::getDatabase)
```

## Default Values

### Mit Default Value

```java
.field("Hostname", Codec.STRING, Config::setHostname, Config::getHostname)
.withDefault("localhost")
.and()
```

### Ohne Default Value

```java
.field("Hostname", Codec.STRING, Config::setHostname, Config::getHostname)
.and()
```

**Hinweis:** Felder ohne Default Value müssen in der Config-Datei vorhanden sein, sonst wird ein Fehler geworfen.

## Komplexes Beispiel

```java
import lombok.Getter;
import lombok.Setter;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PluginConfig {
    
    private boolean enabled;
    private String language;
    private DatabaseConfig database;
    private List<String> features;
    private Map<String, Integer> limits;
    
    public static BuilderCodec<PluginConfig> CODEC = HytaleProvider.getApi()
        .newCodec(PluginConfig.class)
        .field("Enabled", Codec.BOOLEAN, PluginConfig::setEnabled, PluginConfig::isEnabled)
        .withDefault(true)
        .and()
        .field("Language", Codec.STRING, PluginConfig::setLanguage, PluginConfig::getLanguage)
        .withDefault("en")
        .and()
        .field("Database", DatabaseConfig.CODEC, PluginConfig::setDatabase, PluginConfig::getDatabase)
        .and()
        .field("Features", Codec.listOf(Codec.STRING), PluginConfig::setFeatures, PluginConfig::getFeatures)
        .withDefault(List.of("feature1", "feature2"))
        .and()
        .field("Limits", Codec.mapOf(Codec.STRING, Codec.INTEGER), PluginConfig::setLimits, PluginConfig::getLimits)
        .withDefault(Map.of("maxPlayers", 100))
        .and()
        .build();
}

@Getter
@Setter
class DatabaseConfig {
    
    private String hostname;
    private Integer port;
    
    public static BuilderCodec<DatabaseConfig> CODEC = HytaleProvider.getApi()
        .newCodec(DatabaseConfig.class)
        .field("Hostname", Codec.STRING, DatabaseConfig::setHostname, DatabaseConfig::getHostname)
        .withDefault("localhost")
        .and()
        .field("Port", Codec.INTEGER, DatabaseConfig::setPort, DatabaseConfig::getPort)
        .withDefault(3306)
        .and()
        .build();
}
```

## Best Practices

1. **Lombok verwenden** - `@Getter` und `@Setter` für sauberen Code
2. **Default Values setzen** - Für alle optionalen Felder
3. **Method References** - Verwende Method References statt Lambdas
4. **Nested Configs** - Für komplexe Strukturen
5. **CODEC als static final** - Für bessere Performance

## Nächste Schritte

- **[Localization Guide](Localization-Guide)** - Mehrsprachigkeit implementieren
- **[Database Guide](Database-Guide)** - Datenbankverbindungen einrichten
