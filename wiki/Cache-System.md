# Cache System

Das Cache System bietet In-Memory Caching für häufig genutzte Daten, um Datenbankzugriffe zu reduzieren.

## Überblick

HyLib bietet zwei Cache-Implementierungen:

- **AbstractInMemoryCache** - Einfacher Cache (nicht thread-safe)
- **AbstractThreadSafeInMemoryCache** - Thread-safe Cache für Multi-Thread-Umgebungen

## Cache erstellen

### Thread-Safe Cache (Empfohlen)

```java
import dev.spacetivity.tobi.hylib.database.api.cache.AbstractThreadSafeInMemoryCache;

public class UserCache extends AbstractThreadSafeInMemoryCache<String, User> {
    
    public UserCache() {
        super();
    }
}
```

### Einfacher Cache

```java
import dev.spacetivity.tobi.hylib.database.api.cache.AbstractInMemoryCache;

public class UserCache extends AbstractInMemoryCache<String, User> {
    
    public UserCache() {
        super();
    }
}
```

## Cache verwenden

### Cache registrieren

```java
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.api.cache.CacheLoader;

DatabaseApi api = DatabaseProvider.getApi();
CacheLoader cacheLoader = api.getCacheLoader();

UserCache userCache = new UserCache();
cacheLoader.register(userCache);
```

### Cache-Operationen

```java
UserCache cache = new UserCache();

// Insert (fügt oder ersetzt)
cache.insert("user123", user);

// Update (nur wenn Key existiert)
boolean updated = cache.update("user123", updatedUser);

// Get
User user = cache.getValue("user123");

// Remove
cache.remove("user123");
```

## Cache mit Repository kombinieren

### Cache-Through Pattern

```java
public class CachedUserRepository {
    
    private final UserRepository repository;
    private final UserCache cache;
    
    public CachedUserRepository(UserRepository repository, UserCache cache) {
        this.repository = repository;
        this.cache = cache;
    }
    
    public User getUser(String id) {
        // Zuerst Cache prüfen
        User cached = cache.getValue(id);
        if (cached != null) {
            return cached;
        }
        
        // Falls nicht im Cache, aus Datenbank laden
        User user = repository.getSync(UserRepository.ID_COL, Integer.parseInt(id));
        if (user != null) {
            // In Cache speichern
            cache.insert(id, user);
        }
        
        return user;
    }
    
    public void updateUser(User user) {
        // Datenbank aktualisieren
        repository.updateUser(user);
        
        // Cache aktualisieren
        cache.update(user.getId().toString(), user);
    }
    
    public void deleteUser(String id) {
        // Aus Datenbank löschen
        repository.delete(UserRepository.ID_COL, Integer.parseInt(id));
        
        // Aus Cache entfernen
        cache.remove(id);
    }
}
```

### Cache-Aside Pattern

```java
public class CachedUserService {
    
    private final UserRepository repository;
    private final UserCache cache;
    
    public User getUser(String id) {
        // Cache prüfen
        User cached = cache.getValue(id);
        if (cached != null) {
            return cached;
        }
        
        // Aus Datenbank laden
        User user = repository.getSync(UserRepository.ID_COL, Integer.parseInt(id));
        
        // In Cache speichern (falls gefunden)
        if (user != null) {
            cache.insert(id, user);
        }
        
        return user;
    }
    
    public void invalidateCache(String id) {
        cache.remove(id);
    }
    
    public void clearCache() {
        // Cache komplett leeren (falls unterstützt)
        // Oder alle Keys einzeln entfernen
    }
}
```

## Cache Loader

Der `CacheLoader` verwaltet alle registrierten Caches:

```java
import dev.spacetivity.tobi.hylib.database.api.cache.CacheLoader;

DatabaseApi api = DatabaseProvider.getApi();
CacheLoader cacheLoader = api.getCacheLoader();

// Cache registrieren
UserCache userCache = new UserCache();
cacheLoader.register(userCache);

// Alle Caches abrufen
List<Cache<?, ?>> allCaches = cacheLoader.getCaches();
```

## Best Practices

1. **Thread-Safe für Multi-Thread** - Verwende `AbstractThreadSafeInMemoryCache` in Multi-Thread-Umgebungen
2. **Cache-Through Pattern** - Für automatisches Caching bei Repository-Zugriffen
3. **Cache-Invalidierung** - Entferne Cache-Einträge bei Updates/Deletes
4. **TTL (Time-To-Live)** - Implementiere TTL für automatische Expiration (falls benötigt)
5. **Memory Management** - Überwache Cache-Größe bei großen Datensätzen

## Beispiel: Vollständiger Cache

```java
import dev.spacetivity.tobi.hylib.database.api.cache.AbstractThreadSafeInMemoryCache;
import java.util.concurrent.TimeUnit;

public class UserCache extends AbstractThreadSafeInMemoryCache<String, User> {
    
    private final long ttlMillis;
    private final Map<String, Long> timestamps = new ConcurrentHashMap<>();
    
    public UserCache(long ttlMinutes) {
        super();
        this.ttlMillis = TimeUnit.MINUTES.toMillis(ttlMinutes);
    }
    
    @Override
    public void insert(String key, User value) {
        super.insert(key, value);
        timestamps.put(key, System.currentTimeMillis());
    }
    
    @Override
    public User getValue(String key) {
        Long timestamp = timestamps.get(key);
        if (timestamp != null && System.currentTimeMillis() - timestamp > ttlMillis) {
            // TTL abgelaufen
            remove(key);
            return null;
        }
        return super.getValue(key);
    }
    
    @Override
    public void remove(String key) {
        super.remove(key);
        timestamps.remove(key);
    }
}
```

## Nächste Schritte

- **[Database Guide](Database-Guide)** - Vollständiger Database Guide
- **[Repository Pattern](Repository-Pattern)** - Repository-Implementierung mit Caching
