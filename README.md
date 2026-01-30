# HyLib

A Hytale utility library providing database functionality and Hytale-specific features.

## Features

### Database Module (Hytale-independent)
- **Type-safe SQL Queries** - Query Builder with explicit column lists
- **SQL Injection Protection** - Validated identifiers (Table/Column) instead of string concatenation
- **Flexible Query API** - Fluent Builder instead of enum-based templates
- **RowMapper-based Mapping** - Clean separation of SQL and domain mapping
- **Connection Pooling** - HikariCP for MariaDB
- **In-Memory Cache** - Simple cache API

### Hytale Module
- **Config API** - Type-safe configuration with fluent DSL codec builder

## Modules

### Database Modules (no Hytale dependencies)
- `database-api`: Interfaces, abstractions, Query Builder, RowMapper (usable in Discord Bot, etc.)
- `database-common`: Default implementations (Api, ConnectionHandler, Loader)

### Hytale Modules (requires Hytale dependencies)
- `hytale-api`: Hytale-specific API (CodecBuilder Interface)
- `hytale-common`: Hytale-specific implementations (CodecBuilderImpl)
- `database-hytale-plugin`: Hytale plugin combining both APIs

---

## Installation

### GitHub Packages einrichten

Die Library ist über GitHub Packages verfügbar. Um sie zu verwenden, musst du das GitHub Packages Repository zu deinem Projekt hinzufügen.

#### 1. Repository konfigurieren

Füge das GitHub Packages Repository zu deiner `build.gradle.kts` hinzu:

```kotlin
repositories {
    mavenCentral()
    
    // GitHub Packages Repository
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/OWNER/REPO")
        credentials {
            username = project.findProperty("github.username") as String? 
                ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("github.token") as String? 
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
    
    // Hytale Repositories (falls benötigt)
    maven {
        name = "hytale-release"
        url = uri("https://maven.hytale.com/release")
    }
    maven {
        name = "hytale-pre-release"
        url = uri("https://maven.hytale.com/pre-release")
    }
}
```

**Wichtig:** Ersetze `OWNER` und `REPO` mit deinen GitHub Repository-Informationen:
- `OWNER`: GitHub Username oder Organisation (z.B. `spacetivity`)
- `REPO`: Repository Name (z.B. `hylib`)

#### 2. Authentifizierung einrichten

Du benötigst einen GitHub Personal Access Token (PAT) mit `read:packages` Berechtigung.

**Option A: Über gradle.properties (empfohlen für lokale Entwicklung)**

Erstelle eine `gradle.properties` Datei im Projekt-Root:

```properties
github.username=dein-github-username
github.token=dein-github-personal-access-token
```

**Option B: Über Umgebungsvariablen**

Setze die Umgebungsvariablen:
```bash
export GITHUB_ACTOR=dein-github-username
export GITHUB_TOKEN=dein-github-personal-access-token
```

**GitHub Token erstellen:**
1. Gehe zu: https://github.com/settings/tokens
2. Klicke auf "Generate new token (classic)"
3. Wähle die Berechtigung `read:packages`
4. Kopiere den generierten Token

#### 3. Dependencies hinzufügen

Füge die Dependencies zu deiner `build.gradle.kts` hinzu:

```kotlin
dependencies {
    // Database API (no Hytale dependencies - usable in Discord Bot, etc.)
    implementation("dev.spacetivity.tobi.hylib.database:database-api:VERSION")
    implementation("dev.spacetivity.tobi.hylib.database:database-common:VERSION")
    
    // Hytale API (for CodecBuilder - requires Hytale dependencies)
    implementation("dev.spacetivity.tobi.hylib.database:hytale-api:VERSION")
    implementation("dev.spacetivity.tobi.hylib.database:hytale-common:VERSION")
    
    // Hytale Server (für BuilderCodec und Config)
    compileOnly("com.hypixel.hytale:Server:2026.01.22-6f8bdbdc4")
    
    // Lombok (optional, aber empfohlen für Getter/Setter)
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}
```

**Wichtig:** Ersetze `VERSION` mit der gewünschten Version (z.B. `1.0.0`). Verfügbare Versionen findest du unter:
`https://github.com/OWNER/REPO/packages`

#### Vollständiges Beispiel

```kotlin
// build.gradle.kts
plugins {
    id("java")
    id("java-library")
}

repositories {
    mavenCentral()
    
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/spacetivity/hylib")
        credentials {
            username = project.findProperty("github.username") as String? 
                ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("github.token") as String? 
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
    
    maven {
        name = "hytale-release"
        url = uri("https://maven.hytale.com/release")
    }
}

dependencies {
    // Database API
    implementation("dev.spacetivity.tobi.hylib.database:database-api:1.0.0")
    
    // Hytale Server
    compileOnly("com.hypixel.hytale:Server:2026.01.22-6f8bdbdc4")
    
    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}
```

---

## Quick Start

### 1. API Initialization

```java
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.api.connection.credentials.impl.MariaDbCredentials;
import dev.spacetivity.tobi.hylib.database.common.DatabaseApiImpl;

// Create and register the API
DatabaseApi api = new DatabaseApiImpl();
DatabaseProvider.register(api);

// Establish connection (optional, can be done later)
MariaDbCredentials credentials = new MariaDbCredentials(
    "localhost", 3306, "user", "database", "secret"
);
api.establishConnection(credentials);
```

### 2. Connection Handling

```java
DatabaseConnectionHandler db = DatabaseProvider.getApi().getDatabaseConnectionHandler();

// MariaDB Connector (HikariCP)
DatabaseConnector<HikariDataSource, DatabaseCredentials> sqlConnector = 
    db.getConnectorNullsafe(DatabaseType.MARIADB);
Connection connection = sqlConnector.getSafeConnection().getConnection();
```

---

## Safe Identifiers

### Table & Column

Instead of free strings, validated identifiers are used that prevent SQL injection:

```java
// Table Identifier
Table usersTable = Table.of("users");  // Validated: only [A-Za-z0-9_]

// Column Identifier
Column idColumn = Column.of("id");
Column nameColumn = Column.of("user_name");

// SQL-safe output
String sql = "SELECT * FROM " + usersTable.toSql();  // `users`
String col = idColumn.toSql();  // `id`
```

**Validation:** Only alphanumeric characters and underscores allowed. Invalid names throw `IllegalArgumentException`.

---

## Query Builder API

### SELECT Queries

```java
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder.SqlBuilder;

Table usersTable = Table.of("users");
Column idCol = Column.of("id");
Column nameCol = Column.of("name");
Column emailCol = Column.of("email");

// Simple SELECT with WHERE
BuiltQuery query = SqlBuilder
    .select(idCol, nameCol, emailCol)
    .from(usersTable)
    .where(idCol, 123)
    .build();

// With ORDER BY and LIMIT
BuiltQuery sortedQuery = SqlBuilder
    .select(idCol, nameCol)
    .from(usersTable)
    .where(emailCol, "test@example.com")
    .orderBy(nameCol, true)  // ASC
    .limit(10)
    .build();

// Multiple WHERE conditions (AND)
BuiltQuery multiWhere = SqlBuilder
    .select(idCol, nameCol)
    .from(usersTable)
    .where(idCol, 123)
    .where(nameCol, "John")
    .build();
```

### INSERT Queries

```java
BuiltQuery insert = SqlBuilder
    .insertInto(usersTable)
    .value(idCol, 123)
    .value(nameCol, "John Doe")
    .value(emailCol, "john@example.com")
    .build();

// Or multiple values at once
BuiltQuery batchInsert = SqlBuilder
    .insertInto(usersTable)
    .values(
        new Column[]{idCol, nameCol, emailCol},
        new Object[]{123, "John", "john@example.com"}
    )
    .build();
```

### UPDATE Queries

```java
BuiltQuery update = SqlBuilder
    .update(usersTable)
    .set(nameCol, "Jane Doe")
    .set(emailCol, "jane@example.com")
    .where(idCol, 123)
    .build();

// Multiple WHERE conditions
BuiltQuery updateMulti = SqlBuilder
    .update(usersTable)
    .set(emailCol, "new@example.com")
    .where(idCol, 123)
    .where(nameCol, "John")
    .build();
```

### DELETE Queries

```java
BuiltQuery delete = SqlBuilder
    .deleteFrom(usersTable)
    .where(idCol, 123)
    .build();

// Multiple WHERE conditions
BuiltQuery deleteMulti = SqlBuilder
    .deleteFrom(usersTable)
    .where(idCol, 123)
    .where(nameCol, "John")
    .build();
```

---

## RowMapper

The `RowMapper<T>` interface separates SQL logic from domain mapping:

```java
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.RowMapper;

public class User {
    private final int id;
    private final String name;
    private final String email;
    
    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}

// RowMapper implementation
RowMapper<User> userMapper = (ResultSet rs) -> {
    return new User(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getString("email")
    );
};

// Usage in repository
BuiltQuery query = SqlBuilder.select(idCol, nameCol, emailCol)
    .from(usersTable)
    .where(idCol, 123)
    .build();

Optional<User> user = queryOne(query, userMapper);
List<User> users = query(query, userMapper);
```

---

## Repository Pattern

### Creating a Base Repository

```java
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.*;
import dev.spacetivity.tobi.hylib.database.api.repository.impl.AbstractMariaDbRepository;

public class UserRepository extends AbstractMariaDbRepository<User> {
    
    // Column definitions as constants
    private static final Table USERS_TABLE = Table.of("users");
    private static final Column ID_COL = Column.of("id");
    private static final Column NAME_COL = Column.of("name");
    private static final Column EMAIL_COL = Column.of("email");
    
    public UserRepository(DatabaseConnectionHandler db, Connection connection) {
        super(db, TableDefinition.create(
            connection,
            USERS_TABLE,
            SQLColumn.fromPrimary(ID_COL, SQLDataType.INTEGER),
            SQLColumn.from(NAME_COL, SQLDataType.VARCHAR),
            SQLColumn.fromNullable(EMAIL_COL, SQLDataType.VARCHAR)
        ));
    }
    
    @Override
    public User deserializeResultSet(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt(ID_COL.name()),
            rs.getString(NAME_COL.name()),
            rs.getString(EMAIL_COL.name())
        );
    }
    
    @Override
    public void insert(User user) {
        BuiltQuery insert = SqlBuilder
            .insertInto(USERS_TABLE)
            .value(ID_COL, user.getId())
            .value(NAME_COL, user.getName())
            .value(EMAIL_COL, user.getEmail())
            .build();
        executeUpdate(insert);
    }
    
    // Custom query methods
    public Optional<User> findById(int id) {
        BuiltQuery query = SqlBuilder
            .select(ID_COL, NAME_COL, EMAIL_COL)
            .from(USERS_TABLE)
            .where(ID_COL, id)
            .build();
        return queryOne(query, this::deserializeResultSet);
    }
    
    public List<User> findByName(String name) {
        BuiltQuery query = SqlBuilder
            .select(ID_COL, NAME_COL, EMAIL_COL)
            .from(USERS_TABLE)
            .where(NAME_COL, name)
            .orderBy(ID_COL, true)
            .build();
        return query(query, this::deserializeResultSet);
    }
    
    public void updateEmail(int id, String email) {
        BuiltQuery update = SqlBuilder
            .update(USERS_TABLE)
            .set(EMAIL_COL, email)
            .where(ID_COL, id)
            .build();
        executeUpdate(update);
    }
    
    public void deleteById(int id) {
        BuiltQuery delete = SqlBuilder
            .deleteFrom(USERS_TABLE)
            .where(ID_COL, id)
            .build();
        executeUpdate(delete);
    }
    
    public boolean exists(int id) {
        return exists(ID_COL, id);
    }
}
```

### Registering a Repository

```java
DatabaseConnectionHandler db = DatabaseProvider.getApi().getDatabaseConnectionHandler();
Connection connection = db.getConnectorNullsafe(DatabaseType.MARIADB)
    .getSafeConnection()
    .getConnection();

UserRepository userRepo = new UserRepository(db, connection);

RepositoryLoader loader = DatabaseProvider.getApi().getRepositoryLoader();
loader.register(userRepo);
```

### Asynchronous Queries

```java
// Async GET
CompletableFuture<User> futureUser = userRepo.getAsync(ID_COL, 123);
futureUser.thenAccept(user -> {
    System.out.println("User: " + user.getName());
});

// Async GET ALL
CompletableFuture<List<User>> futureUsers = userRepo.getAllAsync();
futureUsers.thenAccept(users -> {
    users.forEach(u -> System.out.println(u.getName()));
});
```

---

## Available Repository Methods

### Query Methods

```java
// Base query methods (protected)
List<T> query(BuiltQuery query, RowMapper<T> mapper)
Optional<T> queryOne(BuiltQuery query, RowMapper<T> mapper)
int executeUpdate(BuiltQuery query)
boolean existsQuery(BuiltQuery query)

// Public convenience methods
T getSync(Column keyColumn, Object key)
Optional<T> findById(Column keyColumn, Object key)  // via queryOne
boolean exists(Column keyColumn, Object key)
List<T> getAllSync()
```

### Async Methods

```java
CompletableFuture<T> getAsync(Column keyColumn, Object key)
CompletableFuture<List<T>> getAllAsync()
```

---

## SQL Data Types

```java
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.SQLDataType;

// Text Types
SQLDataType.VARCHAR      // VARCHAR(255)
SQLDataType.CHAR         // CHAR(1)
SQLDataType.TEXT         // TEXT

// Numeric Types
SQLDataType.INTEGER      // INT
SQLDataType.BIGINT       // BIGINT
SQLDataType.DECIMAL     // DECIMAL
SQLDataType.DOUBLE      // DOUBLE

// Date/Time Types
SQLDataType.DATE         // DATE
SQLDataType.TIMESTAMP   // TIMESTAMP

// Boolean
SQLDataType.BOOLEAN     // TINYINT
```

### SQLColumn Factory Methods

```java
// Primary Key (NOT NULL)
SQLColumn.fromPrimary(Column.of("id"), SQLDataType.INTEGER)
SQLColumn.fromPrimary("id", SQLDataType.INTEGER)  // String variant

// NOT NULL Column
SQLColumn.from(Column.of("name"), SQLDataType.VARCHAR)
SQLColumn.from("name", SQLDataType.VARCHAR)

// Nullable Column
SQLColumn.fromNullable(Column.of("email"), SQLDataType.VARCHAR)
SQLColumn.fromNullable("email", SQLDataType.VARCHAR)

// Custom Value
SQLColumn.from(Column.of("status"), "VARCHAR(50) DEFAULT 'active'")
```

---

## Config API

The Config API provides type-safe configuration management using a fluent DSL for building codecs. Codecs are created programmatically at runtime using method references.

### Features

- **Fluent DSL** - Simple, readable API for building codecs
- **Method References** - Type-safe getter/setter references
- **Default Values** - Support for optional fields with default values
- **Runtime Creation** - No compile-time code generation required
- **Hytale integration** - Works seamlessly with Hytale's `Config<T>` system

### Quick Start

#### 1. Create a Config Class

Create a config class with getters and setters:

```java
package com.example.plugin.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPluginConfig {
    
    private String serverName = "MyServer";
    private int maxPlayers = 100;
    private boolean enabled = true;
    private String apiKey;
    private String databaseUrl = "jdbc:mariadb://localhost:3306/mydb";
}
```

#### 2. Create a Codec

Add a static `codec()` method to your config class. **Note:** `HytaleApi` must be initialized before calling `newCodec()`:

```java
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;

public class MyPluginConfig {
    // ... fields ...
    
    public static BuilderCodec<MyPluginConfig> codec() {
        return HytaleProvider.getApi().newCodec(MyPluginConfig.class)
                .field("server-name", Codec.STRING, 
                    MyPluginConfig::setServerName, 
                    MyPluginConfig::getServerName)
                    .withDefault("MyServer")
                .and()
                .field("max-players", Codec.INTEGER,
                    MyPluginConfig::setMaxPlayers,
                    MyPluginConfig::getMaxPlayers)
                    .withDefault(100)
                .and()
                .field("enabled", Codec.BOOLEAN,
                    MyPluginConfig::setEnabled,
                    MyPluginConfig::isEnabled)
                    .withDefault(true)
                .and()
                .field("api-key", Codec.STRING,
                    MyPluginConfig::setApiKey,
                    MyPluginConfig::getApiKey)
                .and()
                .field("database-url", Codec.STRING,
                    MyPluginConfig::setDatabaseUrl,
                    MyPluginConfig::getDatabaseUrl)
                    .withDefault("jdbc:mariadb://localhost:3306/mydb")
                .build();
    }
}
```

#### 3. Use the Config

**Important:** `HytaleApi` must be initialized before calling `newCodec()`. Initialize it in your plugin constructor:

```java
import com.hypixel.hytale.server.core.util.Config;
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.common.DatabaseApiImpl;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.common.HytaleApiImpl;

public class MyPlugin extends JavaPlugin {
    
    private Config<MyPluginConfig> config;
    
    public MyPlugin(JavaPluginInit init) {
        super(init);
        
        // Initialize DatabaseApi first
        DatabaseApi api = new DatabaseApiImpl();
        DatabaseProvider.register(api);
        
        // Now you can create configs with codecs
        config = withConfig("MyConfig", MyPluginConfig.codec());
    }
    
    @Override
    protected void setup() {
        super.setup();
        
        // Access config values
        MyPluginConfig cfg = config.get();
        String serverName = cfg.getServerName();
        int maxPlayers = cfg.getMaxPlayers();
        boolean enabled = cfg.isEnabled();
        
        if (cfg.getApiKey() == null) {
            getLogger().error("API key is required!");
            return;
        }
    }
    
    public MyPluginConfig getConfig() {
        return config.get();
    }
}
```

### CodecBuilder API

The `CodecBuilder` provides a fluent DSL for building codecs. Codecs are created via `HytaleProvider.getApi().newCodec(Class<T>)`:

#### Basic Usage

```java
BuilderCodec<MyConfig> codec = HytaleProvider.getApi().newCodec(MyConfig.class)
    .field("key", Codec.STRING, MyConfig::setValue, MyConfig::getValue)
    .build();
```

#### With Default Value

```java
BuilderCodec<MyConfig> codec = HytaleProvider.getApi().newCodec(MyConfig.class)
    .field("hostname", Codec.STRING, MyConfig::setHostname, MyConfig::getHostname)
        .withDefault("localhost")
    .build();
```

#### Multiple Fields

```java
BuilderCodec<MyConfig> codec = HytaleProvider.getApi().newCodec(MyConfig.class)
    .field("hostname", Codec.STRING, MyConfig::setHostname, MyConfig::getHostname)
        .withDefault("localhost")
    .and()
    .field("port", Codec.INTEGER, MyConfig::setPort, MyConfig::getPort)
        .withDefault(3306)
    .build();
```

### Supported Codec Types

The following Hytale `Codec` types are available:

#### Primitive Types
- `Codec.STRING`
- `Codec.INTEGER`
- `Codec.LONG`
- `Codec.DOUBLE`
- `Codec.FLOAT`
- `Codec.BOOLEAN`
- `Codec.BYTE`
- `Codec.SHORT`

#### Object Types
- `Codec.UUID_STRING`
- `Codec.DURATION`
- `Codec.INSTANT`
- `Codec.PATH`
- `Codec.LOG_LEVEL`

#### Custom Codecs

You can also use custom codecs from Hytale's codec system:

```java
import com.hypixel.hytale.codec.FunctionCodec;

// For enums
FunctionCodec<GameMode> gameModeCodec = new FunctionCodec<>(
    Codec.STRING, 
    GameMode::valueOf, 
    Enum::name
);

BuilderCodec<MyConfig> codec = HytaleProvider.getApi().newCodec(MyConfig.class)
    .field("mode", gameModeCodec, MyConfig::setMode, MyConfig::getMode)
        .withDefault(GameMode.NORMAL)
    .build();
```

### Complete Example

Here's a complete example of a database configuration:

```java
package com.example.plugin.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseConfig {
    
    private String hostname = "localhost";
    private int port = 3306;
    private String database;
    private String username;
    private String password;
    private int poolSize = 10;
    private java.time.Duration connectionTimeout = java.time.Duration.ofSeconds(30);
    private boolean sslEnabled = false;
    
    public static BuilderCodec<DatabaseConfig> codec() {
        return HytaleProvider.getApi().newCodec(DatabaseConfig.class)
                .field("hostname", Codec.STRING, 
                    DatabaseConfig::setHostname, 
                    DatabaseConfig::getHostname)
                    .withDefault("localhost")
                .and()
                .field("port", Codec.INTEGER,
                    DatabaseConfig::setPort,
                    DatabaseConfig::getPort)
                    .withDefault(3306)
                .and()
                .field("database", Codec.STRING,
                    DatabaseConfig::setDatabase,
                    DatabaseConfig::getDatabase)
                .and()
                .field("username", Codec.STRING,
                    DatabaseConfig::setUsername,
                    DatabaseConfig::getUsername)
                .and()
                .field("password", Codec.STRING,
                    DatabaseConfig::setPassword,
                    DatabaseConfig::getPassword)
                .and()
                .field("pool-size", Codec.INTEGER,
                    DatabaseConfig::setPoolSize,
                    DatabaseConfig::getPoolSize)
                    .withDefault(10)
                .and()
                .field("connection-timeout", Codec.DURATION,
                    DatabaseConfig::setConnectionTimeout,
                    DatabaseConfig::getConnectionTimeout)
                    .withDefault(java.time.Duration.ofSeconds(30))
                .and()
                .field("ssl-enabled", Codec.BOOLEAN,
                    DatabaseConfig::setSslEnabled,
                    DatabaseConfig::isSslEnabled)
                    .withDefault(false)
                .build();
    }
}
```

Usage:

```java
public class DatabasePlugin extends JavaPlugin {
    
    private Config<DatabaseConfig> dbConfig;
    
    public DatabasePlugin(JavaPluginInit init) {
        super(init);
        
        dbConfig = withConfig("DatabaseConfig", DatabaseConfig.codec());
    }
    
    @Override
    protected void setup() {
        super.setup();
        
        DatabaseConfig config = dbConfig.get();
        
        // Use config values
        MariaDbCredentials credentials = new MariaDbCredentials(
            config.getHostname(),
            config.getPort(),
            config.getDatabase(),
            config.getUsername(),
            config.getPassword()
        );
        
        // Initialize database with config
        DatabaseApi api = new DatabaseApiImpl();
        api.establishConnection(credentials);
        DatabaseProvider.register(api);
    }
}
```

### Best Practices

1. **Use Lombok**: Use `@Getter` and `@Setter` to avoid boilerplate:

```java
@Getter
@Setter
public class MyConfig {
    // ...
}
```

2. **Static Codec Method**: Create a static `codec()` method in your config class:

```java
public class MyConfig {
    // ... fields ...
    
    public static BuilderCodec<MyConfig> codec() {
        return HytaleProvider.getApi().newCodec(MyConfig.class)
            // ... fields ...
            .build();
    }
}
```

3. **Meaningful Field Names**: Use descriptive field names and config keys:

```java
// Good
.field("max-concurrent-connections", Codec.INTEGER, ...)
    .withDefault(10)

// Bad
.field("mcc", Codec.INTEGER, ...)
    .withDefault(10)
```

4. **Required vs Optional**: Use `.withDefault()` for optional fields with defaults, or omit it for required fields:

```java
// Required field (no default)
.field("api-key", Codec.STRING, MyConfig::setApiKey, MyConfig::getApiKey)

// Optional field (with default)
.field("timeout", Codec.DURATION, MyConfig::setTimeout, MyConfig::getTimeout)
    .withDefault(Duration.ofSeconds(30))

// Optional field (nullable - BuilderCodec handles null automatically)
.field("password", Codec.STRING, MyConfig::setPassword, MyConfig::getPassword)
```

5. **Group Related Configs**: Create separate config classes for different concerns:

```java
public class DatabaseConfig { /* ... */ }
public class PluginConfig { /* ... */ }
public class FeatureConfig { /* ... */ }
```

6. **Validate After Loading**: Check required fields after loading:

```java
DatabaseConfig config = dbConfig.get();
if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
    throw new IllegalStateException("API key is required!");
}
```

---

## Cache API

### In-Memory Cache

```java
import dev.spacetivity.tobi.hylib.database.api.cache.AbstractInMemoryCache;

public class LocalUserCache extends AbstractInMemoryCache<String, User> {
    // Optional: Custom Logic
}

// Register
CacheLoader cacheLoader = DatabaseProvider.getApi().getCacheLoader();
LocalUserCache cache = new LocalUserCache();
cacheLoader.register(cache);

// Usage
cache.insert("key", user);
User user = cache.getValue("key");
cache.remove("key");
```

---

## Best Practices

### 1. Column Definitions as Constants

```java
public class UserRepository extends AbstractMariaDbRepository<User> {
    private static final Column ID_COL = Column.of("id");
    private static final Column NAME_COL = Column.of("name");
    // ...
}
```

### 2. Explicit Column Lists Instead of SELECT *

```java
// Good
SqlBuilder.select(ID_COL, NAME_COL, EMAIL_COL).from(table)

// Bad
SqlBuilder.select(Column.of("*")).from(table)  // Not supported
```

### 3. Reuse RowMapper

```java
// Good: Method Reference
queryOne(query, this::deserializeResultSet)

// Also good: Lambda
queryOne(query, rs -> new User(rs.getInt("id"), rs.getString("name")))
```

### 4. TableDefinition Only for Schema Generation

```java
// TableDefinition only in constructor for generate()
super(db, TableDefinition.create(connection, table, columns...));

// For queries: Use Table identifier
Table usersTable = Table.of("users");
```

---

## Important Notes

- `DatabaseProvider.getApi()` throws `IllegalStateException` if no instance has been registered
- `HytaleProvider.getApi().newCodec()` requires `HytaleApi` to be initialized first
- `DatabaseConnector#getSafeConnection()` throws `NullPointerException` if no connection exists
- Dependencies for MariaDB are `compileOnly` - must be provided at runtime
- Table/Column identifiers are validated at compile time
- Query Builder always creates PreparedStatements (parameter binding)
- ResultSet is automatically closed correctly (try-with-resources)

---

## Dependencies

### Runtime Dependencies

```gradle
runtimeOnly 'org.mariadb.jdbc:mariadb-java-client:3.0.7'
runtimeOnly 'com.zaxxer:HikariCP:5.0.1'
runtimeOnly 'com.google.code.gson:gson:2.10.1'
```

### Config API Dependencies

For using the Config API in your project:

```gradle
dependencies {
    // Database API
    implementation("dev.spacetivity.tobi.hylib.database:database-api:1.0-SNAPSHOT")
    
    // Hytale Server (für BuilderCodec und Config)
    compileOnly("com.hypixel.hytale:Server:2026.01.22-6f8bdbdc4")
    
    // Lombok (optional, aber empfohlen für Getter/Setter)
    compileOnly("org.projectlombok:lombok:...")
    annotationProcessor("org.projectlombok:lombok:...")
}
```

---

## License

See LICENSE file in the project root.
