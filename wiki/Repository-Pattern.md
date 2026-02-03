# Repository Pattern

Das Repository Pattern bietet eine abstrakte Schicht für Datenbankoperationen und automatische CRUD-Funktionalität.

## Überblick

Repositories in HyLib:

- **Automatische CRUD-Operationen** - Get, GetAll, Delete, Insert
- **Type-Safe Queries** - Verwendet SqlBuilder für alle Queries
- **Automatische Tabellenerstellung** - Tabellen werden automatisch erstellt
- **Sync und Async Support** - Beide Varianten verfügbar
- **Foreign Key Support** - Referentielle Integrität

## Repository erstellen

### Basis-Repository

```java
import dev.spacetivity.tobi.hylib.database.api.repository.impl.AbstractMariaDbRepository;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository extends AbstractMariaDbRepository<User> {
    
    // Table und Column Definitionen als Konstanten
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
            SQLColumn.from(EMAIL_COL, SQLDataType.VARCHAR)
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
        BuiltQuery insert = SqlBuilder.insertInto(USERS_TABLE)
            .value(ID_COL, user.getId())
            .value(NAME_COL, user.getName())
            .value(EMAIL_COL, user.getEmail())
            .build();
        executeUpdate(insert);
    }
}
```

## Automatische CRUD-Operationen

### Synchron

```java
UserRepository repo = new UserRepository(db, connection);

// Get by ID
User user = repo.getSync(ID_COL, 123);

// Get all
List<User> allUsers = repo.getAllSync();

// Check existence
boolean exists = repo.exists(ID_COL, 123);

// Delete
repo.delete(ID_COL, 123);

// Insert
User newUser = new User(456, "John", "john@example.com");
repo.insert(newUser);
```

### Asynchron

```java
// Get by ID (async)
CompletableFuture<User> userFuture = repo.getAsync(ID_COL, 123);
userFuture.thenAccept(user -> {
    if (user != null) {
        // User gefunden
    }
});

// Get all (async)
CompletableFuture<List<User>> allUsersFuture = repo.getAllAsync();
allUsersFuture.thenAccept(users -> {
    // Alle Users verarbeiten
});
```

## Custom Queries

### Custom SELECT Query

```java
public List<User> findByName(String name) {
    BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
        .from(getTable())
        .where(NAME_COL, name)
        .build();
    return query(query, this::deserializeResultSet);
}

public Optional<User> findByEmail(String email) {
    BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
        .from(getTable())
        .where(EMAIL_COL, email)
        .limit(1)
        .build();
    return queryOne(query, this::deserializeResultSet);
}
```

### Custom UPDATE Query

```java
public void updateName(int id, String newName) {
    BuiltQuery update = SqlBuilder.update(getTable())
        .set(NAME_COL, newName)
        .where(ID_COL, id)
        .build();
    executeUpdate(update);
}
```

### Custom Query mit JOIN

```java
Table postsTable = Table.of("posts");
Column userIdCol = Column.of("user_id");

public List<User> findUsersWithPosts() {
    BuiltQuery query = SqlBuilder.select(
            getTable(), ID_COL,
            getTable(), NAME_COL
        )
        .from(getTable())
        .innerJoin(postsTable, userIdCol, ID_COL)
        .build();
    return query(query, this::deserializeResultSet);
}
```

## Foreign Keys

### Foreign Key definieren

```java
// In PermissionNodeRepository
public static final Table PERMISSION_NODES_TABLE = Table.of("permission_nodes");
public static final Column GROUP_NAME_COL = Column.of("group_name");
public static final Column PERMISSION_COL = Column.of("permission");

// Referenz auf PermissionGroupRepository
private static final Table PERMISSION_GROUPS_TABLE = PermissionGroupRepository.PERMISSION_GROUPS_TABLE;
private static final Column PG_NAME_COL = PermissionGroupRepository.NAME_COL;

public PermissionNodeRepository(DatabaseConnectionHandler db, Connection connection) {
    super(db, TableDefinition.create(
        connection,
        PERMISSION_NODES_TABLE,
        // Primary Key + Foreign Key kombiniert
        SQLColumn.fromPrimaryForeignKey(
            GROUP_NAME_COL,
            SQLDataType.VARCHAR,
            PERMISSION_GROUPS_TABLE,
            PG_NAME_COL
        ),
        SQLColumn.fromPrimary(PERMISSION_COL, SQLDataType.VARCHAR)
    ));
}
```

### Foreign Key Methoden

```java
// Foreign Key (NOT NULL)
SQLColumn.fromForeignKey(column, dataType, referencedTable, referencedColumn)

// Foreign Key (nullable)
SQLColumn.fromNullableForeignKey(column, dataType, referencedTable, referencedColumn)

// Primary Key + Foreign Key kombiniert
SQLColumn.fromPrimaryForeignKey(column, dataType, referencedTable, referencedColumn)
```

## Composite Primary Keys

```java
public class PermissionNodeRepository extends AbstractMariaDbRepository<PermissionInfo> {
    
    public static final Table PERMISSION_NODES_TABLE = Table.of("permission_nodes");
    public static final Column GROUP_NAME_COL = Column.of("group_name");
    public static final Column PERMISSION_COL = Column.of("permission");
    
    public PermissionNodeRepository(DatabaseConnectionHandler db, Connection connection) {
        super(db, TableDefinition.create(
            connection,
            PERMISSION_NODES_TABLE,
            // Composite Primary Key: (group_name, permission)
            SQLColumn.fromPrimary(GROUP_NAME_COL, SQLDataType.VARCHAR),
            SQLColumn.fromPrimary(PERMISSION_COL, SQLDataType.VARCHAR)
        ));
    }
    
    // Custom get-Methode für Composite Key
    public PermissionInfo getByGroupAndPermission(String groupName, String permission) {
        BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
            .from(getTable())
            .where(GROUP_NAME_COL, groupName)
            .where(PERMISSION_COL, permission)
            .build();
        return queryOne(query, this::deserializeResultSet).orElse(null);
    }
}
```

## Repository registrieren

```java
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.api.repository.RepositoryLoader;

DatabaseApi api = DatabaseProvider.getApi();
RepositoryLoader loader = api.getRepositoryLoader();

UserRepository userRepo = new UserRepository(db, connection);
loader.register(userRepo, UserRepository.class);

// Repository abrufen
Optional<UserRepository> repo = loader.get(UserRepository.class);
```

## Best Practices

1. **Table und Column als Konstanten** - Verwende `public static final` für wiederverwendbare Definitionen
2. **Foreign Keys als public static final** - Ermöglicht Referenzen von anderen Repositories
3. **Custom Queries in Repository** - Kapsle Query-Logik im Repository
4. **Async für lange Operationen** - Nutze `getAsync()` für nicht-blockierende Operationen
5. **RowMapper wiederverwenden** - Nutze `this::deserializeResultSet` für konsistente Mapping

## Beispiel: Vollständiges Repository

```java
public class UserRepository extends AbstractMariaDbRepository<User> {
    
    public static final Table USERS_TABLE = Table.of("users");
    public static final Column ID_COL = Column.of("id");
    public static final Column NAME_COL = Column.of("name");
    public static final Column EMAIL_COL = Column.of("email");
    
    public UserRepository(DatabaseConnectionHandler db, Connection connection) {
        super(db, TableDefinition.create(
            connection,
            USERS_TABLE,
            SQLColumn.fromPrimary(ID_COL, SQLDataType.INTEGER),
            SQLColumn.from(NAME_COL, SQLDataType.VARCHAR),
            SQLColumn.from(EMAIL_COL, SQLDataType.VARCHAR)
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
        BuiltQuery insert = SqlBuilder.insertInto(USERS_TABLE)
            .value(ID_COL, user.getId())
            .value(NAME_COL, user.getName())
            .value(EMAIL_COL, user.getEmail())
            .build();
        executeUpdate(insert);
    }
    
    // Custom Queries
    public List<User> findByName(String name) {
        BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
            .from(getTable())
            .where(NAME_COL, name)
            .build();
        return query(query, this::deserializeResultSet);
    }
    
    public void updateEmail(int id, String email) {
        BuiltQuery update = SqlBuilder.update(getTable())
            .set(EMAIL_COL, email)
            .where(ID_COL, id)
            .build();
        executeUpdate(update);
    }
}
```

## Nächste Schritte

- **[SQL Query Builder](SQL-Query-Builder)** - Detaillierte Query-Builder Dokumentation
- **[Database Guide](Database-Guide)** - Vollständiger Database Guide
- **[Foreign Keys Beispiel](../database-api/src/main/java/dev/spacetivity/tobi/hylib/database/api/connection/impl/sql/examples/README.md)** - Komplexes Beispiel mit Foreign Keys und JOINs
