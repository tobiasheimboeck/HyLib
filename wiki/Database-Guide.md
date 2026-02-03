# Database Guide

Das Database API von HyLib bietet eine type-safe, SQL-Injection-sichere Schnittstelle für Datenbankoperationen mit MariaDB.

## Überblick

Das Database Module besteht aus:

- **DatabaseApi**: Zentrale API für Connections, Cache, Repositories und Executor
- **DatabaseConnectionHandler**: Verwaltet Datenbankverbindungen und Connectors
- **SqlBuilder**: Type-safe SQL Query Builder
- **Repository Pattern**: Basis-Repositories für CRUD-Operationen
- **Cache System**: In-Memory Caching für häufig genutzte Daten

## Initialisierung

### 1. DatabaseApi erstellen und registrieren

```java
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.common.DatabaseApiImpl;

// DatabaseApi erstellen
DatabaseApiImpl dbApi = new DatabaseApiImpl();

// Registrieren für globalen Zugriff
DatabaseProvider.register(dbApi);
```

### 2. Datenbankverbindung herstellen

```java
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.api.connection.credentials.impl.MariaDbCredentials;

DatabaseApi api = DatabaseProvider.getApi();

// MariaDB Credentials erstellen
MariaDbCredentials credentials = new MariaDbCredentials(
    "localhost",  // Hostname
    5520,         // Port
    "root",       // Username
    "game_db",    // Database
    "password"    // Password
);

// Verbindung herstellen
api.getDatabaseConnectionHandler().establishConnection(credentials);
```

### 3. Connection abrufen

```java
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseType;
import java.sql.Connection;

DatabaseApi api = DatabaseProvider.getApi();
DatabaseConnectionHandler handler = api.getDatabaseConnectionHandler();

// Connector abrufen
MariaDbConnector connector = handler.getConnectorNullsafe(DatabaseType.MARIADB)
    .getSafeConnection();

// Connection abrufen
Connection connection = connector.getConnection();
```

## SQL Query Builder

Der `SqlBuilder` bietet eine type-safe, fluent API für SQL-Queries. Siehe [SQL Query Builder](SQL-Query-Builder) für Details.

### Beispiel: SELECT Query

```java
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.*;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder.*;

Table usersTable = Table.of("users");
Column idCol = Column.of("id");
Column nameCol = Column.of("name");

BuiltQuery query = SqlBuilder.select(idCol, nameCol)
    .from(usersTable)
    .where(idCol, 123)
    .build();
```

## Repository Pattern

Repositories bieten eine abstrakte Schicht für Datenbankoperationen. Siehe [Repository Pattern](Repository-Pattern) für Details.

### Beispiel: Repository erstellen

```java
import dev.spacetivity.tobi.hylib.database.api.repository.impl.AbstractMariaDbRepository;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnectionHandler;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository extends AbstractMariaDbRepository<User> {
    
    private static final Table USERS_TABLE = Table.of("users");
    private static final Column ID_COL = Column.of("id");
    private static final Column NAME_COL = Column.of("name");
    
    public UserRepository(DatabaseConnectionHandler db, Connection connection) {
        super(db, TableDefinition.create(
            connection,
            USERS_TABLE,
            SQLColumn.fromPrimary(ID_COL, SQLDataType.INTEGER),
            SQLColumn.from(NAME_COL, SQLDataType.VARCHAR)
        ));
    }
    
    @Override
    public User deserializeResultSet(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt(ID_COL.name()),
            rs.getString(NAME_COL.name())
        );
    }
    
    @Override
    public void insert(User user) {
        BuiltQuery insert = SqlBuilder.insertInto(USERS_TABLE)
            .value(ID_COL, user.getId())
            .value(NAME_COL, user.getName())
            .build();
        executeUpdate(insert);
    }
}
```

## Cache System

Das Cache System bietet In-Memory Caching für häufig genutzte Daten. Siehe [Cache System](Cache-System) für Details.

### Beispiel: Cache erstellen

```java
import dev.spacetivity.tobi.hylib.database.api.cache.AbstractThreadSafeInMemoryCache;

public class UserCache extends AbstractThreadSafeInMemoryCache<String, User> {
    
    public UserCache() {
        super();
    }
}

// Cache verwenden
DatabaseApi api = DatabaseProvider.getApi();
CacheLoader cacheLoader = api.getCacheLoader();

UserCache userCache = new UserCache();
cacheLoader.register(userCache);
```

## Asynchrone Operationen

Das DatabaseApi bietet einen ExecutorService für asynchrone Operationen:

```java
import java.util.concurrent.Future;

DatabaseApi api = DatabaseProvider.getApi();

// Task asynchron ausführen
Future<?> future = api.execute(() -> {
    // Datenbankoperationen hier
    userRepository.insert(user);
});

// Auf Completion warten
future.get();
```

## Best Practices

1. **Immer Table und Column Objekte verwenden** - Nie Strings direkt in SQL verwenden
2. **PreparedStatements verwenden** - Alle Werte werden automatisch als Parameter übergeben
3. **Repositories für Domain Logic** - Kapsle Datenbankoperationen in Repositories
4. **Cache für häufig genutzte Daten** - Reduziere Datenbankzugriffe
5. **Asynchrone Operationen für lange Tasks** - Nutze den ExecutorService

## Nächste Schritte

- **[SQL Query Builder](SQL-Query-Builder)** - Detaillierte Query-Builder Dokumentation
- **[Repository Pattern](Repository-Pattern)** - Repository-Implementierung
- **[Cache System](Cache-System)** - Caching-Strategien
