# Elytra Database API

A type-safe, SQL-injection-resistant database API for MariaDB (SQL).

## Features

- **Type-safe SQL Queries** - Query Builder with explicit column lists
- **SQL Injection Protection** - Validated identifiers (Table/Column) instead of string concatenation
- **Flexible Query API** - Fluent Builder instead of enum-based templates
- **RowMapper-based Mapping** - Clean separation of SQL and domain mapping
- **Connection Pooling** - HikariCP for MariaDB
- **In-Memory Cache** - Simple cache API

## Modules

- `database-api`: Interfaces, abstractions, Query Builder, RowMapper
- `database-common`: Default implementations (Api, ConnectionHandler, Loader)

---

## Quick Start

### 1. API Initialization

```java
MariaDbCredentials maria = new MariaDbCredentials(
    "localhost", 3306, "user", "database", "secret"
);

DatabaseApi api = new DatabaseApiImpl(maria);
ElytraDatabaseProvider.register(api);
```

### 2. Connection Handling

```java
DatabaseConnectionHandler db = ElytraDatabaseProvider.getApi().getDatabaseConnectionHandler();

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
DatabaseConnectionHandler db = ElytraDatabaseProvider.getApi().getDatabaseConnectionHandler();
Connection connection = db.getConnectorNullsafe(DatabaseType.MARIADB)
    .getSafeConnection()
    .getConnection();

UserRepository userRepo = new UserRepository(db, connection);

RepositoryLoader loader = ElytraDatabaseProvider.getApi().getRepositoryLoader();
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

## Cache API

### In-Memory Cache

```java
import dev.spacetivity.tobi.database.api.cache.AbstractInMemoryCache;

public class LocalUserCache extends AbstractInMemoryCache<String, User> {
    // Optional: Custom Logic
}

// Register
CacheLoader cacheLoader = ElytraDatabaseProvider.getApi().getCacheLoader();
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

- `ElytraDatabaseProvider.getApi()` throws `IllegalStateException` if no instance has been registered
- `DatabaseConnector#getSafeConnection()` throws `NullPointerException` if no connection exists
- Dependencies for MariaDB are `compileOnly` - must be provided at runtime
- Table/Column identifiers are validated at compile time
- Query Builder always creates PreparedStatements (parameter binding)
- ResultSet is automatically closed correctly (try-with-resources)

---

## Dependencies (Runtime)

```gradle
runtimeOnly 'org.mariadb.jdbc:mariadb-java-client:3.0.7'
runtimeOnly 'com.zaxxer:HikariCP:5.0.1'
runtimeOnly 'com.google.code.gson:gson:2.10.1'
```

---

## License

See LICENSE file in the project root.
