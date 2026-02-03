# SQL Query Builder

Der SQL Query Builder bietet eine type-safe, fluent API für SQL-Queries mit automatischem SQL-Injection-Schutz.

## Überblick

Der `SqlBuilder` unterstützt:

- **SELECT** Queries mit JOINs, WHERE, ORDER BY, LIMIT
- **INSERT** Queries
- **UPDATE** Queries mit WHERE
- **DELETE** Queries mit WHERE

Alle Queries verwenden:
- Validierte Table- und Column-Identifier
- Parameterized Statements (PreparedStatement) für alle Werte
- Keine String-Konkatenation für User-Input

## Grundlagen

### Table und Column erstellen

```java
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.*;

// Table definieren
Table usersTable = Table.of("users");

// Columns definieren
Column idCol = Column.of("id");
Column nameCol = Column.of("name");
Column emailCol = Column.of("email");
```

**Wichtig:** Verwende immer `Table.of()` und `Column.of()` statt Strings direkt!

## SELECT Queries

### Einfache SELECT Query

```java
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder.*;

BuiltQuery query = SqlBuilder.select(idCol, nameCol, emailCol)
    .from(usersTable)
    .build();
```

### SELECT mit WHERE

```java
BuiltQuery query = SqlBuilder.select(idCol, nameCol)
    .from(usersTable)
    .where(idCol, 123)
    .build();
```

### SELECT mit mehreren WHERE-Bedingungen

```java
BuiltQuery query = SqlBuilder.select(idCol, nameCol)
    .from(usersTable)
    .where(idCol, 123)
    .where(nameCol, "John")
    .build();
// Generiert: WHERE id = ? AND name = ?
```

### SELECT mit ORDER BY

```java
BuiltQuery query = SqlBuilder.select(idCol, nameCol)
    .from(usersTable)
    .orderBy(nameCol, true)  // true = ASC, false = DESC
    .build();
```

### SELECT mit LIMIT

```java
BuiltQuery query = SqlBuilder.select(idCol, nameCol)
    .from(usersTable)
    .limit(10)
    .build();
```

### SELECT mit JOINs

```java
Table postsTable = Table.of("posts");
Column userIdCol = Column.of("user_id");
Column titleCol = Column.of("title");

BuiltQuery query = SqlBuilder.select(
        usersTable, nameCol,
        postsTable, titleCol
    )
    .from(usersTable)
    .innerJoin(postsTable, userIdCol, idCol)
    .where(usersTable, idCol, 123)
    .build();
```

### JOIN Typen

```java
// INNER JOIN
.innerJoin(postsTable, userIdCol, idCol)

// LEFT JOIN
.leftJoin(postsTable, userIdCol, idCol)

// RIGHT JOIN
.rightJoin(postsTable, userIdCol, idCol)
```

### Qualified WHERE für JOINs

Bei JOINs musst du die Tabelle spezifizieren:

```java
BuiltQuery query = SqlBuilder.select(
        usersTable, nameCol,
        postsTable, titleCol
    )
    .from(usersTable)
    .innerJoin(postsTable, userIdCol, idCol)
    // Qualified WHERE: table.column = value
    .where(usersTable, idCol, 123)
    .where(postsTable, titleCol, "My Post")
    .build();
```

## INSERT Queries

### Einfacher INSERT

```java
BuiltQuery insert = SqlBuilder.insertInto(usersTable)
    .value(idCol, 123)
    .value(nameCol, "John")
    .value(emailCol, "john@example.com")
    .build();
```

### INSERT ausführen

```java
// Mit Repository
executeUpdate(insert);

// Oder direkt mit Connection
try (PreparedStatement stmt = connection.prepareStatement(insert.sql())) {
    for (int i = 0; i < insert.params().size(); i++) {
        stmt.setObject(i + 1, insert.params().get(i));
    }
    stmt.executeUpdate();
}
```

## UPDATE Queries

### Einfaches UPDATE

```java
BuiltQuery update = SqlBuilder.update(usersTable)
    .set(nameCol, "Jane")
    .set(emailCol, "jane@example.com")
    .where(idCol, 123)
    .build();
```

**Wichtig:** UPDATE sollte immer eine WHERE-Klausel haben!

### UPDATE mit mehreren SET-Klauseln

```java
BuiltQuery update = SqlBuilder.update(usersTable)
    .set(nameCol, "Jane")
    .set(emailCol, "jane@example.com")
    .where(idCol, 123)
    .build();
```

## DELETE Queries

### Einfaches DELETE

```java
BuiltQuery delete = SqlBuilder.deleteFrom(usersTable)
    .where(idCol, 123)
    .build();
```

**Wichtig:** DELETE sollte immer eine WHERE-Klausel haben!

### DELETE mit mehreren Bedingungen

```java
BuiltQuery delete = SqlBuilder.deleteFrom(usersTable)
    .where(idCol, 123)
    .where(nameCol, "John")
    .build();
```

## Query ausführen

### Mit Repository

```java
// SELECT Query
List<User> users = query(query, this::deserializeResultSet);

// INSERT/UPDATE/DELETE Query
executeUpdate(insert);
```

### Direkt mit Connection

```java
import java.sql.*;

// SELECT Query
try (PreparedStatement stmt = connection.prepareStatement(query.sql())) {
    // Parameter binden
    for (int i = 0; i < query.params().size(); i++) {
        stmt.setObject(i + 1, query.params().get(i));
    }
    
    // Query ausführen
    try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            // ResultSet verarbeiten
        }
    }
}

// INSERT/UPDATE/DELETE Query
try (PreparedStatement stmt = connection.prepareStatement(query.sql())) {
    // Parameter binden
    for (int i = 0; i < query.params().size(); i++) {
        stmt.setObject(i + 1, query.params().get(i));
    }
    
    // Query ausführen
    int rowsAffected = stmt.executeUpdate();
}
```

## Komplexe Beispiele

### JOIN mit mehreren Tabellen

```java
Table usersTable = Table.of("users");
Table postsTable = Table.of("posts");
Table commentsTable = Table.of("comments");

Column userIdCol = Column.of("user_id");
Column postIdCol = Column.of("post_id");
Column idCol = Column.of("id");

BuiltQuery query = SqlBuilder.select(
        usersTable, Column.of("name"),
        postsTable, Column.of("title"),
        commentsTable, Column.of("content")
    )
    .from(usersTable)
    .innerJoin(postsTable, userIdCol, idCol)
    .innerJoin(commentsTable, postIdCol, postsTable, Column.of("id"))
    .where(usersTable, idCol, 123)
    .build();
```

### Subquery (manuell)

Für komplexe Subqueries kannst du `BuiltQuery` manuell kombinieren:

```java
BuiltQuery subquery = SqlBuilder.select(Column.of("user_id"))
    .from(postsTable)
    .where(Column.of("published"), true)
    .build();

// Subquery als String verwenden (nur für komplexe Fälle)
String sql = "SELECT * FROM users WHERE id IN (" + subquery.sql() + ")";
// Parameter müssen manuell kombiniert werden
```

## SQL Injection Schutz

Der Query Builder schützt automatisch vor SQL Injection:

1. **Validierte Identifier**: Table- und Column-Namen werden validiert
2. **Parameterized Statements**: Alle Werte werden als Parameter übergeben
3. **Keine String-Konkatenation**: User-Input wird nie direkt in SQL eingefügt

### ❌ Falsch (SQL Injection möglich)

```java
// NIEMALS so machen!
String sql = "SELECT * FROM users WHERE id = " + userId;
```

### ✅ Richtig (SQL Injection sicher)

```java
BuiltQuery query = SqlBuilder.select(Column.of("*"))
    .from(Table.of("users"))
    .where(Column.of("id"), userId)
    .build();
```

## Best Practices

1. **Immer Table und Column Objekte verwenden** - Nie Strings direkt
2. **WHERE-Klauseln bei UPDATE/DELETE** - Verhindert versehentliche Mass-Updates
3. **Qualified WHERE bei JOINs** - Spezifiziere die Tabelle
4. **Parameterized Statements** - Nutze immer `BuiltQuery.params()`
5. **Repositories für Query-Logik** - Kapsle Queries in Repositories

## Nächste Schritte

- **[Repository Pattern](Repository-Pattern)** - Queries in Repositories kapseln
- **[Database Guide](Database-Guide)** - Vollständiger Database Guide
