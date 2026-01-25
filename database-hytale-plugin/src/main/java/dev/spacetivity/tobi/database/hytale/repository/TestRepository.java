package dev.spacetivity.tobi.database.hytale.repository;

import com.zaxxer.hikari.HikariDataSource;
import dev.spacetivity.tobi.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.database.api.connection.DatabaseConnector;
import dev.spacetivity.tobi.database.api.connection.DatabaseType;
import dev.spacetivity.tobi.database.api.connection.credentials.DatabaseCredentials;
import dev.spacetivity.tobi.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.database.api.connection.impl.sql.SQLColumn;
import dev.spacetivity.tobi.database.api.connection.impl.sql.SQLDataType;
import dev.spacetivity.tobi.database.api.connection.impl.sql.Table;
import dev.spacetivity.tobi.database.api.connection.impl.sql.TableDefinition;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.BuiltQuery;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.SqlBuilder;
import dev.spacetivity.tobi.database.api.repository.impl.AbstractMariaDbRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Demo Repository für MariaDB.
 * Zeigt wie man ein Repository mit AbstractMariaDbRepository erstellt.
 */
public class TestRepository extends AbstractMariaDbRepository<TestRepository.TestEntity> {

    // Tabellen- und Spalten-Definitionen als Konstanten
    private static final Table TEST_TABLE = Table.of("test_entities");
    private static final Column ID_COL = Column.of("id");
    private static final Column NAME_COL = Column.of("name");
    private static final Column VALUE_COL = Column.of("value");
    private static final Column CREATED_AT_COL = Column.of("created_at");

    public TestRepository(DatabaseConnectionHandler db) {
        super(db, createTableDefinition(db));
    }

    @SneakyThrows
    private static TableDefinition createTableDefinition(DatabaseConnectionHandler db) {
        DatabaseConnector<HikariDataSource, DatabaseCredentials> connector = db.getConnectorNullsafe(DatabaseType.MARIADB);
        Connection connection = connector.getSafeConnection().getConnection();

        return TableDefinition.create(
                connection,
                TEST_TABLE,
                SQLColumn.fromPrimary(ID_COL, SQLDataType.INTEGER), // Primary Key, Auto-Increment
                SQLColumn.from(NAME_COL, SQLDataType.VARCHAR), // NOT NULL
                SQLColumn.fromNullable(VALUE_COL, SQLDataType.TEXT), // NULL erlaubt
                SQLColumn.fromNullable(CREATED_AT_COL, SQLDataType.TIMESTAMP) // NULL erlaubt
        );
    }

    /**
     * Deserialisiert ein ResultSet zu einer TestEntity.
     */
    @Override
    public TestEntity deserializeResultSet(ResultSet resultSet) {
        try {
            return new TestEntity(
                    resultSet.getInt(ID_COL.name()),
                    resultSet.getString(NAME_COL.name()),
                    resultSet.getString(VALUE_COL.name()),
                    resultSet.getTimestamp(CREATED_AT_COL.name()) != null
                            ? resultSet.getTimestamp(CREATED_AT_COL.name()).toInstant()
                            : null);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deserialize TestEntity", e);
        }
    }

    /**
     * Fügt eine neue TestEntity in die Datenbank ein.
     */
    @Override
    public void insert(TestEntity entity) {
        BuiltQuery insert = SqlBuilder
                .insertInto(TEST_TABLE)
                .value(NAME_COL, entity.getName())
                .value(VALUE_COL, entity.getValue())
                .value(CREATED_AT_COL, entity.getCreatedAt() != null
                        ? java.sql.Timestamp.from(entity.getCreatedAt())
                        : null)
                .build();
        executeUpdate(insert);
    }

    /**
     * Findet eine Entity anhand der ID.
     */
    public Optional<TestEntity> findById(int id) {
        BuiltQuery query = SqlBuilder
                .select(getColumns().toArray(new Column[0]))
                .from(TEST_TABLE)
                .where(ID_COL, id)
                .build();
        return queryOne(query, this::deserializeResultSet);
    }

    /**
     * Findet alle Entities mit einem bestimmten Namen.
     */
    public List<TestEntity> findByName(String name) {
        BuiltQuery query = SqlBuilder
                .select(getColumns().toArray(new Column[0]))
                .from(TEST_TABLE)
                .where(NAME_COL, name)
                .build();
        return query(query, this::deserializeResultSet);
    }

    /**
     * Findet alle Entities.
     */
    public List<TestEntity> findAll() {
        return getAllSync();
    }

    /**
     * Aktualisiert eine Entity.
     */
    public void update(TestEntity entity) {
        BuiltQuery update = SqlBuilder
                .update(TEST_TABLE)
                .set(NAME_COL, entity.getName())
                .set(VALUE_COL, entity.getValue())
                .set(CREATED_AT_COL, entity.getCreatedAt() != null
                        ? java.sql.Timestamp.from(entity.getCreatedAt())
                        : null)
                .where(ID_COL, entity.getId())
                .build();
        executeUpdate(update);
    }

    /**
     * Löscht eine Entity anhand der ID.
     */
    public void deleteById(int id) {
        delete(ID_COL, id);
    }

    /**
     * Prüft ob eine Entity mit der gegebenen ID existiert.
     */
    public boolean existsById(int id) {
        return exists(ID_COL, id);
    }

    /**
     * Demo Entity Klasse.
     */
    @Getter
    @RequiredArgsConstructor
    public static class TestEntity {
        private final int id;
        private final String name;
        private final String value;
        private final java.time.Instant createdAt;

        /**
         * Konstruktor für neue Entities (ohne ID, da Auto-Increment).
         */
        public TestEntity(String name, String value) {
            this(0, name, value, java.time.Instant.now());
        }
    }
}
