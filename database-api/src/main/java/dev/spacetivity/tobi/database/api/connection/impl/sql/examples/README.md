# Permission Group & Permission Node Example

Dieses Beispiel demonstriert die Verwendung von Foreign Keys und JOINs mit dem Database API.

## Übersicht

Das Beispiel zeigt:
- **PermissionGroup**: Interface für Berechtigungsgruppen (admin, mod, dev, player, etc.)
- **PermissionInfo**: Interface für einzelne Berechtigungen (Permission Nodes), die mit Gruppen verknüpft sind

## Tabellen-Struktur

### permission_groups
- `name` (VARCHAR, PRIMARY KEY) - Name der Gruppe (z.B. "admin", "mod", "player")
- `priority` (INT, NOT NULL) - Priorität der Gruppe (höher = wichtiger)
- `is_default` (BOOLEAN, NOT NULL) - Ist dies die Standard-Gruppe?

### permission_nodes
- `group_name` (VARCHAR, PRIMARY KEY, FOREIGN KEY → permission_groups.name) - Name der Gruppe
- `permission` (VARCHAR, PRIMARY KEY) - Die Berechtigung (z.B. "server.manage")

**Composite Primary Key**: `(group_name, permission)` - Eine Berechtigung kann nur einmal pro Gruppe existieren.

**Foreign Key**: `group_name` referenziert `permission_groups.name` - Stellt sicher, dass nur existierende Gruppen verwendet werden können.

### permission_group_parents (optional, falls Parent-Beziehungen benötigt werden)
- `group_name` (VARCHAR, PRIMARY KEY, FOREIGN KEY → permission_groups.name) - Name der Gruppe
- `parent_name` (VARCHAR, PRIMARY KEY, FOREIGN KEY → permission_groups.name) - Name der Parent-Gruppe

**Composite Primary Key**: `(group_name, parent_name)` - Eine Parent-Beziehung kann nur einmal existieren.

**Foreign Keys**: Beide Spalten referenzieren `permission_groups.name` - Stellt sicher, dass nur existierende Gruppen verwendet werden können.

## Domain Model

```java
// PermissionGroup Interface
public interface PermissionGroup {
    String getName();
    int getPriority();
    Set<PermissionInfo> getPermissions();  // Verknüpfte Permissions
    Set<String> getParents();
    boolean isDefault();
    Boolean hasPermission(String permission);
    Set<PermissionInfo> collectAllPermissions();
    // ...
}

// PermissionInfo Interface (Permission Node)
public interface PermissionInfo extends Expirable {
    String getName();
}
```

## Repository Setup

### 1. PermissionGroupRepository (Basis-Repository)

```java
package dev.spacetivity.tobi.hyperms.common.repository;

import dev.spacetivity.tobi.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.database.api.connection.impl.sql.*;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.BuiltQuery;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.SqlBuilder;
import dev.spacetivity.tobi.database.api.repository.impl.AbstractMariaDbRepository;
import dev.spacetivity.tobi.hyperms.api.group.PermissionGroup;
import dev.spacetivity.tobi.hyperms.common.api.group.PermissionGroupImpl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class PermissionGroupRepository extends AbstractMariaDbRepository<PermissionGroup> {

    // Table und Column Definitionen als Konstanten
    private static final Table PERMISSION_GROUPS_TABLE = Table.of("permission_groups");
    private static final Column NAME_COL = Column.of("name");
    private static final Column PRIORITY_COL = Column.of("priority");
    private static final Column IS_DEFAULT_COL = Column.of("is_default");

    public PermissionGroupRepository(DatabaseConnectionHandler db, Connection connection) {
        super(db, TableDefinition.create(
                connection,
                PERMISSION_GROUPS_TABLE,
                SQLColumn.fromPrimary(NAME_COL, SQLDataType.VARCHAR),
                SQLColumn.from(PRIORITY_COL, SQLDataType.INTEGER),
                SQLColumn.from(IS_DEFAULT_COL, SQLDataType.BOOLEAN)
        ));
    }

    @Override
    public PermissionGroup deserializeResultSet(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString(NAME_COL.name());
        int priority = resultSet.getInt(PRIORITY_COL.name());
        boolean isDefault = resultSet.getBoolean(IS_DEFAULT_COL.name());
        
        // Permissions werden separat über PermissionNodeRepository geladen
        // Parents werden separat über PermissionGroupParentRepository geladen
        return new PermissionGroupImpl(
                name,
                priority,
                Set.of(),  // Permissions werden später über PermissionNodeRepository geladen
                Set.of(),  // Parents werden später über PermissionGroupParentRepository geladen
                isDefault  // Direkt aus der Datenbank
        );
    }

    /**
     * Lädt eine PermissionGroup mit allen Permissions und Parents in einem Query.
     * Verwendet JOINs um alle Daten auf einmal zu laden.
     */
    public PermissionGroup loadWithRelations(String groupName, PermissionNodeRepository nodeRepo, PermissionGroupParentRepository parentRepo) {
        // Gruppe laden
        PermissionGroup group = getSync(NAME_COL, groupName);
        if (group == null) {
            return null;
        }
        
        // Permissions laden
        List<PermissionInfo> permissions = nodeRepo.findByGroupName(groupName);
        
        // Parents laden
        List<String> parents = parentRepo.findParentsByGroupName(groupName);
        
        return new PermissionGroupImpl(
                group.getName(),
                group.getPriority(),
                new HashSet<>(permissions),
                new HashSet<>(parents),
                group.isDefault()
        );
    }

    @Override
    public void insert(PermissionGroup value) {
        BuiltQuery insert = SqlBuilder
                .insertInto(PERMISSION_GROUPS_TABLE)
                .value(NAME_COL, value.getName())
                .value(PRIORITY_COL, value.getPriority())
                .build();
        executeUpdate(insert);
    }
}
```

### 2. PermissionGroupRepository (mit public static final für Foreign Key Referenzen)

**Wichtig**: Die Table- und Column-Definitionen müssen als `public static final` definiert sein, damit andere Repositories darauf zugreifen können:

```java
package dev.spacetivity.tobi.hyperms.common.repository;

import dev.spacetivity.tobi.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.database.api.connection.impl.sql.*;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.BuiltQuery;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.SqlBuilder;
import dev.spacetivity.tobi.database.api.repository.impl.AbstractMariaDbRepository;
import dev.spacetivity.tobi.hyperms.api.group.PermissionGroup;
import dev.spacetivity.tobi.hyperms.common.api.group.PermissionGroupImpl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionGroupRepository extends AbstractMariaDbRepository<PermissionGroup> {

    // WICHTIG: public static final für Foreign Key Referenzen
    public static final Table PERMISSION_GROUPS_TABLE = Table.of("permission_groups");
    public static final Column NAME_COL = Column.of("name");
    
    private static final Column PRIORITY_COL = Column.of("priority");
    private static final Column IS_DEFAULT_COL = Column.of("is_default");

    public PermissionGroupRepository(DatabaseConnectionHandler db, Connection connection) {
        super(db, TableDefinition.create(
                connection,
                PERMISSION_GROUPS_TABLE,
                SQLColumn.fromPrimary(NAME_COL, SQLDataType.VARCHAR),
                SQLColumn.from(PRIORITY_COL, SQLDataType.INTEGER),
                SQLColumn.from(IS_DEFAULT_COL, SQLDataType.BOOLEAN)
        ));
    }

    @Override
    public PermissionGroup deserializeResultSet(ResultSet resultSet) {
        try {
            String name = resultSet.getString(NAME_COL.name());
            int priority = resultSet.getInt(PRIORITY_COL.name());
            boolean isDefault = resultSet.getBoolean(IS_DEFAULT_COL.name());
            
            // Permissions werden separat über PermissionNodeRepository geladen
            // Parents werden separat über PermissionGroupParentRepository geladen
            return new PermissionGroupImpl(
                    name,
                    priority,
                    Set.of(),  // Permissions werden später über PermissionNodeRepository geladen
                    Set.of(),  // Parents werden später über PermissionGroupParentRepository geladen
                    isDefault  // Direkt aus der Datenbank
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insert(PermissionGroup value) {
        executeUpdate(SqlBuilder
                .insertInto(PERMISSION_GROUPS_TABLE)
                .value(NAME_COL, value.getName())
                .value(PRIORITY_COL, value.getPriority())
                .value(IS_DEFAULT_COL, value.isDefault())
                .build());
    }

    /**
     * Lädt eine PermissionGroup mit allen Permissions und Parents in einem Query.
     * Verwendet JOINs um alle Daten auf einmal zu laden.
     */
    public PermissionGroup loadWithRelations(String groupName, PermissionNodeRepository nodeRepo, PermissionGroupParentRepository parentRepo) {
        // Gruppe laden
        PermissionGroup group = getSync(NAME_COL, groupName);
        if (group == null) {
            return null;
        }
        
        // Permissions laden
        List<PermissionInfo> permissions = nodeRepo.findByGroupName(groupName);
        
        // Parents laden
        List<String> parents = parentRepo.findParentsByGroupName(groupName);
        
        return new PermissionGroupImpl(
                group.getName(),
                group.getPriority(),
                new HashSet<>(permissions),
                new HashSet<>(parents),
                group.isDefault()
        );
    }
}
```

### 3. PermissionNodeRepository (mit Foreign Key)

```java
package dev.spacetivity.tobi.hyperms.common.repository;

import dev.spacetivity.tobi.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.database.api.connection.impl.sql.*;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.BuiltQuery;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.SqlBuilder;
import dev.spacetivity.tobi.database.api.repository.impl.AbstractMariaDbRepository;
import dev.spacetivity.tobi.hyperms.api.permission.PermissionInfo;
import dev.spacetivity.tobi.hyperms.common.api.permission.PermissionInfoImpl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PermissionNodeRepository extends AbstractMariaDbRepository<PermissionInfo> {

    // Table und Column Definitionen
    public static final Table PERMISSION_NODES_TABLE = Table.of("permission_nodes");
    public static final Column GROUP_NAME_COL = Column.of("group_name");
    public static final Column PERMISSION_COL = Column.of("permission");

    // Referenz auf die PermissionGroupRepository Spalten für Foreign Key
    private static final Table PERMISSION_GROUPS_TABLE = PermissionGroupRepository.PERMISSION_GROUPS_TABLE;
    private static final Column PG_NAME_COL = PermissionGroupRepository.NAME_COL;

    public PermissionNodeRepository(DatabaseConnectionHandler db, Connection connection) {
        super(db, TableDefinition.create(
                connection,
                PERMISSION_NODES_TABLE,
                // Composite Primary Key: group_name + permission
                // group_name ist gleichzeitig Foreign Key zu permission_groups.name
                SQLColumn.fromPrimaryForeignKey(
                        GROUP_NAME_COL,
                        SQLDataType.VARCHAR,
                        PERMISSION_GROUPS_TABLE,
                        PG_NAME_COL
                ),
                SQLColumn.fromPrimary(PERMISSION_COL, SQLDataType.VARCHAR)
        ));
    }

    @Override
    public PermissionInfo deserializeResultSet(ResultSet resultSet) throws SQLException {
        // PermissionInfo aus der Datenbank deserialisieren
        String permissionName = resultSet.getString(PERMISSION_COL.name());
        return new PermissionInfoImpl(permissionName);
    }

    @Override
    public void insert(PermissionInfo value) {
        // Insert benötigt group_name - muss von außen übergeben werden
        throw new UnsupportedOperationException("Use insert(String groupName, PermissionInfo) instead");
    }

    /**
     * Fügt eine Berechtigung zu einer Gruppe hinzu.
     */
    public void insert(String groupName, PermissionInfo permission) {
        BuiltQuery insert = SqlBuilder
                .insertInto(PERMISSION_NODES_TABLE)
                .value(GROUP_NAME_COL, groupName)
                .value(PERMISSION_COL, permission.getName())
                .build();
        executeUpdate(insert);
    }

    /**
     * Findet alle Berechtigungen für eine bestimmte Gruppe.
     */
    public List<PermissionInfo> findByGroupName(String groupName) {
        BuiltQuery query = SqlBuilder
                .select(getColumns().toArray(new Column[0]))
                .from(PERMISSION_NODES_TABLE)
                .where(GROUP_NAME_COL, groupName)
                .build();
        return query(query, this::deserializeResultSet);
    }

    /**
     * Prüft ob eine Gruppe eine bestimmte Berechtigung hat.
     */
    public boolean hasPermission(String groupName, String permission) {
        BuiltQuery query = SqlBuilder
                .select(PERMISSION_COL)
                .from(PERMISSION_NODES_TABLE)
                .where(GROUP_NAME_COL, groupName)
                .where(PERMISSION_COL, permission)
                .limit(1)
                .build();
        return existsQuery(query);
    }

    /**
     * Löscht eine Berechtigung von einer Gruppe.
     */
    public void deletePermission(String groupName, String permission) {
        BuiltQuery delete = SqlBuilder
                .deleteFrom(PERMISSION_NODES_TABLE)
                .where(GROUP_NAME_COL, groupName)
                .where(PERMISSION_COL, permission)
                .build();
        executeUpdate(delete);
    }

    /**
     * Löscht alle Berechtigungen einer Gruppe.
     */
    public void deleteByGroupName(String groupName) {
        BuiltQuery delete = SqlBuilder
                .deleteFrom(PERMISSION_NODES_TABLE)
                .where(GROUP_NAME_COL, groupName)
                .build();
        executeUpdate(delete);
    }
}
```

### 4. PermissionGroupParentRepository (für Parent-Beziehungen)

```java
package dev.spacetivity.tobi.hyperms.common.repository;

import dev.spacetivity.tobi.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.database.api.connection.impl.sql.*;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.BuiltQuery;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.SqlBuilder;
import dev.spacetivity.tobi.database.api.repository.impl.AbstractMariaDbRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PermissionGroupParentRepository extends AbstractMariaDbRepository<String> {

    public static final Table PERMISSION_GROUP_PARENTS_TABLE = Table.of("permission_group_parents");
    public static final Column GROUP_NAME_COL = Column.of("group_name");
    public static final Column PARENT_NAME_COL = Column.of("parent_name");

    private static final Table PERMISSION_GROUPS_TABLE = PermissionGroupRepository.PERMISSION_GROUPS_TABLE;
    private static final Column PG_NAME_COL = PermissionGroupRepository.NAME_COL;

    public PermissionGroupParentRepository(DatabaseConnectionHandler db, Connection connection) {
        super(db, TableDefinition.create(
                connection,
                PERMISSION_GROUP_PARENTS_TABLE,
                SQLColumn.fromPrimaryForeignKey(
                        GROUP_NAME_COL,
                        SQLDataType.VARCHAR,
                        PERMISSION_GROUPS_TABLE,
                        PG_NAME_COL
                ),
                SQLColumn.fromPrimaryForeignKey(
                        PARENT_NAME_COL,
                        SQLDataType.VARCHAR,
                        PERMISSION_GROUPS_TABLE,
                        PG_NAME_COL
                )
        ));
    }

    @Override
    public String deserializeResultSet(ResultSet resultSet) throws SQLException {
        return resultSet.getString(PARENT_NAME_COL.name());
    }

    @Override
    public void insert(String value) {
        throw new UnsupportedOperationException("Use insert(String groupName, String parentName) instead");
    }

    /**
     * Fügt eine Parent-Beziehung hinzu.
     */
    public void insert(String groupName, String parentName) {
        BuiltQuery insert = SqlBuilder
                .insertInto(PERMISSION_GROUP_PARENTS_TABLE)
                .value(GROUP_NAME_COL, groupName)
                .value(PARENT_NAME_COL, parentName)
                .build();
        executeUpdate(insert);
    }

    /**
     * Findet alle Parent-Gruppen für eine bestimmte Gruppe.
     */
    public List<String> findParentsByGroupName(String groupName) {
        BuiltQuery query = SqlBuilder
                .select(PARENT_NAME_COL)
                .from(PERMISSION_GROUP_PARENTS_TABLE)
                .where(GROUP_NAME_COL, groupName)
                .build();
        return query(query, this::deserializeResultSet);
    }

    /**
     * Löscht eine Parent-Beziehung.
     */
    public void deleteParent(String groupName, String parentName) {
        BuiltQuery delete = SqlBuilder
                .deleteFrom(PERMISSION_GROUP_PARENTS_TABLE)
                .where(GROUP_NAME_COL, groupName)
                .where(PARENT_NAME_COL, parentName)
                .build();
        executeUpdate(delete);
    }
}
```

## Verwendung

### 1. Repositories initialisieren

```java
DatabaseConnectionHandler db = DatabaseProvider.getApi().getDatabaseConnectionHandler();
Connection connection = db.getConnectorNullsafe(DatabaseType.MARIADB)
    .getSafeConnection()
    .getConnection();

PermissionGroupRepository groupRepo = new PermissionGroupRepository(db, connection);
PermissionNodeRepository nodeRepo = new PermissionNodeRepository(db, connection);
PermissionGroupParentRepository parentRepo = new PermissionGroupParentRepository(db, connection);
```

### 2. Gruppen erstellen

```java
// Gruppen mit isDefault erstellen
PermissionGroup adminGroup = new PermissionGroupImpl("admin", 100, Set.of(), Set.of(), false);
PermissionGroup playerGroup = new PermissionGroupImpl("player", 10, Set.of(), Set.of(), true);

groupRepo.insert(adminGroup);
groupRepo.insert(playerGroup);
```

### 3. Berechtigungen hinzufügen

```java
// PermissionInfo Objekte erstellen
PermissionInfo serverManage = new PermissionInfoImpl("server.manage");
PermissionInfo playerBan = new PermissionInfoImpl("player.ban");
PermissionInfo chatSend = new PermissionInfoImpl("chat.send");

// Berechtigungen zu Gruppen hinzufügen
nodeRepo.insert("admin", serverManage);
nodeRepo.insert("admin", playerBan);
nodeRepo.insert("player", chatSend);
```

**Wichtig**: Der Foreign Key Constraint stellt sicher, dass nur existierende Gruppen verwendet werden können. Ein Insert mit einer nicht-existierenden Gruppe würde einen Fehler werfen.

### 4. Abfragen mit JOINs

#### Einfacher JOIN Query

```java
// Alle Berechtigungen mit Gruppen-Informationen abrufen
BuiltQuery query = SqlBuilder
    .select(
        PermissionGroupRepository.NAME_COL,
        PermissionGroupRepository.PRIORITY_COL,
        PermissionNodeRepository.PERMISSION_COL
    )
    .from(PermissionNodeRepository.PERMISSION_NODES_TABLE)
    .innerJoin(
        PermissionGroupRepository.PERMISSION_GROUPS_TABLE,
        PermissionNodeRepository.GROUP_NAME_COL,
        PermissionGroupRepository.NAME_COL
    )
    .build();

// Query ausführen und Ergebnisse mappen
List<PermissionInfo> permissions = nodeRepo.query(query, rs -> {
    String permissionName = rs.getString(PermissionNodeRepository.PERMISSION_COL.name());
    return new PermissionInfoImpl(permissionName);
});

// Oder mit Group-Informationen kombinieren
List<GroupPermissionResult> results = nodeRepo.query(query, rs -> {
    return new GroupPermissionResult(
        rs.getString(PermissionGroupRepository.NAME_COL.name()),
        rs.getInt(PermissionGroupRepository.PRIORITY_COL.name()),
        rs.getString(PermissionNodeRepository.PERMISSION_COL.name())
    );
});
```

#### JOIN mit WHERE-Bedingung (Qualified Column)

```java
// Berechtigungen für eine spezifische Gruppe mit JOIN
BuiltQuery query = SqlBuilder
    .select(
        PermissionGroupRepository.NAME_COL,
        PermissionGroupRepository.PRIORITY_COL,
        PermissionNodeRepository.PERMISSION_COL
    )
    .from(PermissionNodeRepository.PERMISSION_NODES_TABLE)
    .innerJoin(
        PermissionGroupRepository.PERMISSION_GROUPS_TABLE,
        PermissionNodeRepository.GROUP_NAME_COL,
        PermissionGroupRepository.NAME_COL
    )
    // Qualified WHERE: table.column = value
    .where(PermissionGroupRepository.PERMISSION_GROUPS_TABLE, 
           PermissionGroupRepository.NAME_COL, 
           "admin")
    .build();
```

#### JOIN mit WHERE auf beide Tabellen

```java
BuiltQuery query = SqlBuilder
    .select(
        PermissionGroupRepository.NAME_COL,
        PermissionNodeRepository.PERMISSION_COL
    )
    .from(PermissionNodeRepository.PERMISSION_NODES_TABLE)
    .innerJoin(
        PermissionGroupRepository.PERMISSION_GROUPS_TABLE,
        PermissionNodeRepository.GROUP_NAME_COL,
        PermissionGroupRepository.NAME_COL
    )
    // WHERE auf permission_nodes Tabelle
    .where(PermissionNodeRepository.PERMISSION_NODES_TABLE, 
           PermissionNodeRepository.PERMISSION_COL, 
           "server.manage")
    // UND WHERE auf permission_groups Tabelle
    .where(PermissionGroupRepository.PERMISSION_GROUPS_TABLE, 
           PermissionGroupRepository.PRIORITY_COL, 
           100)
    .build();

// Query ausführen
List<PermissionInfo> results = nodeRepo.query(query, nodeRepo::deserializeResultSet);
```

### Beispiel: PermissionGroup mit allen Daten laden

```java
// Option 1: Mit Helper-Methode (lädt alles in einem Schritt)
PermissionGroup group = groupRepo.loadWithRelations("admin", nodeRepo, parentRepo);

// Option 2: Manuell mit separaten Queries
PermissionGroup group = groupRepo.getSync(PermissionGroupRepository.NAME_COL, "admin");
List<PermissionInfo> permissions = nodeRepo.findByGroupName("admin");
List<String> parents = parentRepo.findParentsByGroupName("admin");

PermissionGroup fullGroup = new PermissionGroupImpl(
    group.getName(),
    group.getPriority(),
    new HashSet<>(permissions),
    new HashSet<>(parents),
    group.isDefault()
);

// Option 3: Mit JOINs alles in einem Query laden
BuiltQuery fullQuery = SqlBuilder
    .select(
        PermissionGroupRepository.NAME_COL,
        PermissionGroupRepository.PRIORITY_COL,
        PermissionGroupRepository.IS_DEFAULT_COL,
        PermissionNodeRepository.PERMISSION_COL,
        PermissionGroupParentRepository.PARENT_NAME_COL
    )
    .from(PermissionGroupRepository.PERMISSION_GROUPS_TABLE)
    .leftJoin(
        PermissionNodeRepository.PERMISSION_NODES_TABLE,
        PermissionGroupRepository.NAME_COL,
        PermissionNodeRepository.GROUP_NAME_COL
    )
    .leftJoin(
        PermissionGroupParentRepository.PERMISSION_GROUP_PARENTS_TABLE,
        PermissionGroupRepository.NAME_COL,
        PermissionGroupParentRepository.GROUP_NAME_COL
    )
    .where(PermissionGroupRepository.PERMISSION_GROUPS_TABLE,
           PermissionGroupRepository.NAME_COL,
           "admin")
    .build();

// Query ausführen und Ergebnisse zusammenführen
// (Hier würde man die Results manuell zusammenführen, da ein JOIN mehrere Zeilen zurückgeben kann)
```

## Foreign Key Methoden

### SQLColumn Factory-Methoden für Foreign Keys

```java
// Foreign Key (NOT NULL)
SQLColumn.fromForeignKey(column, dataType, referencedTable, referencedColumn)

// Foreign Key (nullable)
SQLColumn.fromNullableForeignKey(column, dataType, referencedTable, referencedColumn)

// Primary Key + Foreign Key kombiniert
SQLColumn.fromPrimaryForeignKey(column, dataType, referencedTable, referencedColumn)
```

### Beispiel: Foreign Key hinzufügen

```java
// In PermissionNodeRepository Constructor:
super(db, TableDefinition.create(
    connection,
    PERMISSION_NODES_TABLE,
    // Option 1: Primary Key + Foreign Key kombiniert
    SQLColumn.fromPrimaryForeignKey(
        GROUP_NAME_COL,
        SQLDataType.VARCHAR,
        PERMISSION_GROUPS_TABLE,
        PermissionGroupRepository.NAME_COL
    ),
    SQLColumn.fromPrimary(PERMISSION_COL, SQLDataType.VARCHAR)
    
    // ODER Option 2: Separater Foreign Key (wenn nicht Primary Key)
    // SQLColumn.fromPrimary(GROUP_NAME_COL, SQLDataType.VARCHAR),
    // SQLColumn.fromForeignKey(
    //     GROUP_NAME_COL,
    //     SQLDataType.VARCHAR,
    //     PERMISSION_GROUPS_TABLE,
    //     PermissionGroupRepository.NAME_COL
    // )
));
```

## JOIN Methoden

### SelectBuilder JOIN-Methoden

```java
// INNER JOIN
selectBuilder.innerJoin(table, leftColumn, rightColumn)

// LEFT JOIN
selectBuilder.leftJoin(table, leftColumn, rightColumn)

// RIGHT JOIN
selectBuilder.rightJoin(table, leftColumn, rightColumn)
```

### Qualified WHERE für JOINs

```java
// Normale WHERE (funktioniert bei einfachen Queries)
.where(column, value)

// Qualified WHERE (notwendig bei JOINs, um Tabelle zu spezifizieren)
.where(table, column, value)
```

## Features demonstriert

1. **Foreign Key Constraints**: `permission_nodes.group_name` referenziert `permission_groups.name`
   - Automatische Generierung in `TableDefinition.generate()`
   - Referentielle Integrität auf Datenbankebene

2. **JOIN Queries**: INNER JOIN zwischen `permission_groups` und `permission_nodes`
   - Type-safe über `Table` und `Column` Objekte
   - Unterstützt INNER, LEFT, RIGHT JOINs

3. **Qualified WHERE**: `where(Table, Column, Object)` für JOIN-Queries
   - Ermöglicht WHERE-Bedingungen auf spezifische Tabellen in JOINs
   - Format: `table.column = value`

4. **Composite Primary Keys**: `(group_name, permission)` als zusammengesetzter Primärschlüssel
   - Mehrere `SQLColumn.fromPrimary()` erzeugen Composite Key

## Vorteile

- **Referentielle Integrität**: Foreign Keys stellen sicher, dass nur existierende Gruppen verwendet werden können
- **Effiziente Abfragen**: JOINs ermöglichen das Abrufen verknüpfter Daten in einer Query
- **Type-Safety**: Alle Tabellen- und Spaltennamen sind type-safe über `Table` und `Column` Objekte
- **SQL Injection Schutz**: Alle Werte werden als Parameter übergeben
- **Automatische Constraint-Generierung**: Foreign Keys werden automatisch beim Tabellenerstellen generiert
