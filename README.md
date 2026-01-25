# Elytra Database API

A type-safe, SQL-injection-resistant database API for MariaDB (SQL).

## Features

- **Type-safe SQL Queries** - Query Builder with explicit column lists
- **SQL Injection Protection** - Validated identifiers (Table/Column) instead of string concatenation
- **Flexible Query API** - Fluent Builder instead of enum-based templates
- **RowMapper-based Mapping** - Clean separation of SQL and domain mapping
- **Connection Pooling** - HikariCP for MariaDB
- **In-Memory Cache** - Simple cache API
- **Config API** - Type-safe configuration with annotation-based codec generation

## Modules

- `database-api`: Interfaces, abstractions, Query Builder, RowMapper, Config API
- `database-common`: Default implementations (Api, ConnectionHandler, Loader, CodecLoader)
- `database-processor`: Annotation processor for automatic codec generation

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
- `REPO`: Repository Name (z.B. `game-database-lib`)

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
    // Database API
    implementation("dev.spacetivity.tobi.database:database-api:VERSION")
    
    // Annotation Processor für Config Codec Generation
    annotationProcessor("dev.spacetivity.tobi.database:database-processor:VERSION")
    
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
        url = uri("https://maven.pkg.github.com/spacetivity/game-database-lib")
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
    implementation("dev.spacetivity.tobi.database:database-api:1.0.0")
    
    // Annotation Processor
    annotationProcessor("dev.spacetivity.tobi.database:database-processor:1.0.0")
    
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
MariaDbCredentials maria = new MariaDbCredentials(
    "localhost", 3306, "user", "database", "secret"
);

DatabaseApi api = new DatabaseApiImpl(maria);
DatabaseProvider.register(api);
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
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.SqlBuilder;

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
import dev.spacetivity.tobi.database.api.connection.impl.sql.RowMapper;

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
import dev.spacetivity.tobi.database.api.connection.impl.sql.*;
import dev.spacetivity.tobi.database.api.repository.impl.AbstractMariaDbRepository;

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
import dev.spacetivity.tobi.database.api.connection.impl.sql.SQLDataType;

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

The Config API provides type-safe configuration management using annotation-based codec generation. Config classes are automatically processed at compile-time to generate efficient codecs for serialization/deserialization.

### Features

- **Annotation-based** - Simple `@AutoCodec` and `@CodecField` annotations
- **Compile-time generation** - Codecs are generated during compilation for optimal performance
- **Type-safe** - Full type checking at compile time
- **Default values** - Support for optional fields with default values
- **Hytale integration** - Works seamlessly with Hytale's `Config<T>` system

### Quick Start

#### 1. Repository und Dependencies hinzufügen

Siehe [Installation](#installation) für die vollständige Anleitung zur Einrichtung des GitHub Packages Repositories.

**Kurze Zusammenfassung:**

1. Füge das GitHub Packages Repository hinzu (siehe [Installation](#installation))
2. Füge die Dependencies hinzu:

```kotlin
dependencies {
    // API für Annotationen und Interfaces
    implementation("dev.spacetivity.tobi.database:database-api:1.0.0")
    
    // Annotation Processor für Codec-Generierung
    annotationProcessor("dev.spacetivity.tobi.database:database-processor:1.0.0")
    
    // Hytale Server (für BuilderCodec und Config)
    compileOnly("com.hypixel.hytale:Server:2026.01.22-6f8bdbdc4")
    
    // Lombok (optional, aber empfohlen für Getter/Setter)
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}
```

**Wichtig:** 
- Ersetze `1.0.0` mit der gewünschten Version
- Stelle sicher, dass das GitHub Packages Repository korrekt konfiguriert ist
- Ein GitHub Token mit `read:packages` Berechtigung ist erforderlich

#### 2. Create a Config Class

Create a config class annotated with `@AutoCodec`:

```java
package com.example.plugin.config;

import dev.spacetivity.tobi.database.api.config.AutoCodec;
import dev.spacetivity.tobi.database.api.config.CodecField;
import lombok.Getter;
import lombok.Setter;

@AutoCodec
@Getter
@Setter
public class MyPluginConfig {
    
    @CodecField(value = "server-name", hasDefault = true, defaultValue = "MyServer")
    private String serverName;
    
    @CodecField(value = "max-players", hasDefault = true, defaultValue = "100")
    private int maxPlayers;
    
    @CodecField(value = "enabled", hasDefault = true, defaultValue = "true")
    private boolean enabled;
    
    @CodecField(value = "api-key", hasDefault = false) // Kein Default = Pflichtfeld
    private String apiKey;
    
    @CodecField(value = "database-url", hasDefault = true, defaultValue = "jdbc:mariadb://localhost:3306/mydb")
    private String databaseUrl;
}
```

#### 3. Use the Config

##### Option A: Via DatabaseProvider (Recommended)

If `DatabaseApi` is already initialized:

```java
import dev.spacetivity.tobi.database.api.DatabaseProvider;
import dev.spacetivity.tobi.database.api.config.CodecLoader;
import com.hypixel.hytale.server.core.util.Config;

public class MyPlugin extends JavaPlugin {
    
    private Config<MyPluginConfig> config;
    
    public MyPlugin(JavaPluginInit init) {
        super(init);
        
        // Get CodecLoader from DatabaseProvider
        CodecLoader codecLoader = DatabaseProvider.getApi().getCodecLoader();
        
        // Create config with generated codec
        config = withConfig(codecLoader.codec(MyPluginConfig.class));
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

##### Option B: Direct CodecLoader Creation

If you need to use configs before `DatabaseApi` is initialized:

```java
import dev.spacetivity.tobi.database.api.config.CodecLoader;
import dev.spacetivity.tobi.database.common.api.config.CodecLoaderImpl;
import com.hypixel.hytale.server.core.util.Config;

public class MyPlugin extends JavaPlugin {
    
    private Config<MyPluginConfig> config;
    
    public MyPlugin(JavaPluginInit init) {
        super(init);
        
        // Create CodecLoader directly
        CodecLoader codecLoader = new CodecLoaderImpl();
        
        // Create config with generated codec
        config = withConfig(codecLoader.codec(MyPluginConfig.class));
    }
    
    public MyPluginConfig getConfig() {
        return config.get();
    }
}
```

### Annotation Reference

#### @AutoCodec

Marks a class for automatic codec generation. Must be applied to the class level.

```java
@AutoCodec
public class MyConfig {
    // ...
}
```

**Requirements:**
- Class must have a no-argument constructor
- Fields must have getter/setter methods (use Lombok `@Getter`/`@Setter` or write manually)
- Only fields annotated with `@CodecField` will be included in the codec

#### @CodecField

Marks a field to be included in the generated codec.

```java
@CodecField(value = "field-name", hasDefault = true, defaultValue = "default")
private String fieldName;
```

**Parameters:**
- `value` (required): The key name in the config file/object
- `hasDefault` (optional, default: `false`): Whether this field has a default value
- `defaultValue` (optional, default: `""`): The default value as a string (will be parsed according to field type)

**Field Requirements:**
- Must have a getter method (e.g., `getFieldName()` or `isFieldName()` for booleans)
- Must have a setter method (e.g., `setFieldName(value)`)

### Supported Types

The annotation processor supports the following field types:

#### Primitive Types
- `int` / `Integer`
- `long` / `Long`
- `double` / `Double`
- `float` / `Float`
- `boolean` / `Boolean`
- `byte` / `Byte`
- `short` / `Short`

#### Object Types
- `String`
- `java.util.UUID`
- `java.time.Duration`
- `java.time.Instant`
- `java.nio.file.Path`
- `java.util.logging.Level`

#### Enum Types
Any enum type is automatically supported:

```java
@CodecField(value = "mode", hasDefault = true, defaultValue = "NORMAL")
private GameMode mode;

public enum GameMode {
    NORMAL, HARD, EXPERT
}
```

### Default Values

Default values are specified as strings and automatically parsed based on the field type:

```java
@CodecField(value = "port", hasDefault = true, defaultValue = "3306")
private int port;  // Parsed as integer

@CodecField(value = "enabled", hasDefault = true, defaultValue = "true")
private boolean enabled;  // Parsed as boolean

@CodecField(value = "timeout", hasDefault = true, defaultValue = "PT30S")
private Duration timeout;  // Parsed as ISO-8601 duration

@CodecField(value = "uuid", hasDefault = true, defaultValue = "550e8400-e29b-41d4-a716-446655440000")
private UUID uuid;  // Parsed as UUID string
```

**Important:** Fields without `hasDefault = true` are **required** and will cause errors if missing in the config.

### Complete Example

Here's a complete example of a database configuration:

```java
package com.example.plugin.config;

import dev.spacetivity.tobi.database.api.config.AutoCodec;
import dev.spacetivity.tobi.database.api.config.CodecField;
import lombok.Getter;
import lombok.Setter;

@AutoCodec
@Getter
@Setter
public class DatabaseConfig {
    
    @CodecField(value = "hostname", hasDefault = true, defaultValue = "localhost")
    private String hostname;
    
    @CodecField(value = "port", hasDefault = true, defaultValue = "3306")
    private int port;
    
    @CodecField(value = "database", hasDefault = false)
    private String database;
    
    @CodecField(value = "username", hasDefault = false)
    private String username;
    
    @CodecField(value = "password", hasDefault = false)
    private String password;
    
    @CodecField(value = "pool-size", hasDefault = true, defaultValue = "10")
    private int poolSize;
    
    @CodecField(value = "connection-timeout", hasDefault = true, defaultValue = "PT30S")
    private java.time.Duration connectionTimeout;
    
    @CodecField(value = "ssl-enabled", hasDefault = true, defaultValue = "false")
    private boolean sslEnabled;
}
```

Usage:

```java
public class DatabasePlugin extends JavaPlugin {
    
    private Config<DatabaseConfig> dbConfig;
    
    public DatabasePlugin(JavaPluginInit init) {
        super(init);
        
        CodecLoader codecLoader = DatabaseProvider.getApi().getCodecLoader();
        dbConfig = withConfig(codecLoader.codec(DatabaseConfig.class));
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
        DatabaseApi api = new DatabaseApiImpl(credentials);
        DatabaseProvider.register(api);
    }
}
```

### How It Works

1. **Compile-time Processing**: The `CodecProcessor` annotation processor scans for classes annotated with `@AutoCodec`
2. **Code Generation**: For each config class, it generates a `{ClassName}_Codec` class containing a `BuilderCodec` instance
3. **Runtime Loading**: `CodecLoaderImpl` loads the generated codec class and caches it
4. **Config Usage**: The codec is used with Hytale's `Config<T>` system for type-safe configuration

### Generated Code

For a config class `MyConfig`, the processor generates `MyConfig_Codec`:

```java
// Generated automatically
public final class MyConfig_Codec {
    public static final BuilderCodec<MyConfig> CODEC =
        BuilderCodec.builder(MyConfig.class, MyConfig::new)
            .append(new KeyedCodec<>("server-name", Codec.STRING),
                (obj, val, info) -> obj.setServerName(val != null ? val : "MyServer"),
                (obj, info) -> obj.getServerName())
            .add()
            .append(new KeyedCodec<>("max-players", Codec.INTEGER),
                (obj, val, info) -> obj.setMaxPlayers(val != null ? val : 100),
                (obj, info) -> obj.getMaxPlayers())
            .add()
            // ... more fields
            .build();
}
```

### Best Practices

1. **Use Lombok**: Use `@Getter` and `@Setter` to avoid boilerplate:

```java
@AutoCodec
@Getter
@Setter
public class MyConfig {
    // ...
}
```

2. **Meaningful Field Names**: Use descriptive field names and config keys:

```java
// Good
@CodecField(value = "max-concurrent-connections", hasDefault = true, defaultValue = "10")
private int maxConcurrentConnections;

// Bad
@CodecField(value = "mcc", hasDefault = true, defaultValue = "10")
private int mcc;
```

3. **Required vs Optional**: Clearly mark required fields:

```java
// Required field (no default)
@CodecField(value = "api-key", hasDefault = false)
private String apiKey;

// Optional field (with default)
@CodecField(value = "timeout", hasDefault = true, defaultValue = "PT30S")
private Duration timeout;
```

4. **Group Related Configs**: Create separate config classes for different concerns:

```java
@AutoCodec
public class DatabaseConfig { /* ... */ }

@AutoCodec
public class PluginConfig { /* ... */ }

@AutoCodec
public class FeatureConfig { /* ... */ }
```

5. **Validate After Loading**: Check required fields after loading:

```java
DatabaseConfig config = dbConfig.get();
if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
    throw new IllegalStateException("API key is required!");
}
```

### Troubleshooting

#### "No generated codec found"

**Problem:** The annotation processor didn't generate the codec class.

**Solutions:**
- Ensure `database-processor` is in `annotationProcessor` dependencies (not `compileOnly`)
- Rebuild the project (clean + build)
- Check that the class is annotated with `@AutoCodec`
- Verify that fields have getter/setter methods

#### "IllegalStateException: Api instance is null"

**Problem:** `DatabaseProvider.getApi()` is called before `DatabaseProvider.register()`.

**Solutions:**
- Initialize `DatabaseApi` before accessing `DatabaseProvider.getApi()`
- Or use `new CodecLoaderImpl()` directly if you need configs before API initialization

#### Default values not working

**Problem:** Default values are not applied.

**Solutions:**
- Ensure `hasDefault = true` is set
- Check that `defaultValue` matches the field type format
- Verify the default value can be parsed (e.g., valid UUID format, valid Duration format)

### Advanced Usage

#### Custom Codec Building (Manual)

If you need more control, you can build codecs manually using `CodecBuilderDSL`:

```java
import dev.spacetivity.tobi.database.common.api.config.CodecBuilderDSL;
import com.hypixel.hytale.codec.Codec;

BuilderCodec<MyConfig> codec = CodecBuilderDSL.of(MyConfig.class)
    .field("server-name", Codec.STRING, 
        MyConfig::setServerName, 
        MyConfig::getServerName)
    .field("max-players", Codec.INTEGER,
        MyConfig::setMaxPlayers,
        MyConfig::getMaxPlayers)
    .build();
```

However, using `@AutoCodec` is recommended for most use cases.

---

## Cache API

### In-Memory Cache

```java
import dev.spacetivity.tobi.database.api.cache.AbstractInMemoryCache;

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
    // API für Annotationen und Interfaces
    implementation("dev.spacetivity.tobi.database:database-api:1.0-SNAPSHOT")
    
    // Annotation Processor für Codec-Generierung (wichtig: annotationProcessor, nicht compileOnly!)
    annotationProcessor("dev.spacetivity.tobi.database:database-processor:1.0-SNAPSHOT")
    
    // Hytale Server (für BuilderCodec und Config)
    compileOnly("com.hypixel.hytale:Server:2026.01.22-6f8bdbdc4")
    
    // Lombok (optional, aber empfohlen für Getter/Setter)
    compileOnly("org.projectlombok:lombok:...")
    annotationProcessor("org.projectlombok:lombok:...")
}
```

**Important:** The `database-processor` must be in `annotationProcessor` dependencies, not `compileOnly`, otherwise codec generation won't work!

---

## License

See LICENSE file in the project root.
