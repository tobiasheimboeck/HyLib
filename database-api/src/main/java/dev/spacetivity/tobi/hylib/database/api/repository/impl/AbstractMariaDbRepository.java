package dev.spacetivity.tobi.hylib.database.api.repository.impl;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnector;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseType;
import dev.spacetivity.tobi.hylib.database.api.connection.credentials.DatabaseCredentials;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.RowMapper;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.TableDefinition;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Table;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder.BuiltQuery;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder.SqlBuilder;
import dev.spacetivity.tobi.hylib.database.api.repository.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for MariaDB repository implementations.
 * 
 * <p>This class provides a foundation for implementing database repositories that work
 * with MariaDB databases. It handles table creation, connection management, and provides
 * common CRUD operations with both synchronous and asynchronous execution support.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * public class UserRepository extends AbstractMariaDbRepository<User> {
 *     private static final Column ID_COL = Column.of("id");
 *     private static final Column NAME_COL = Column.of("name");
 *     private static final Column EMAIL_COL = Column.of("email");
 *     
 *     public UserRepository(DatabaseConnectionHandler db) {
 *         super(db, TableDefinition.create(
 *             db.getConnectorNullsafe(DatabaseType.MARIADB).getSafeConnection().getConnection(),
 *             Table.of("users"),
 *             SQLColumn.fromPrimary("id", SQLDataType.INTEGER),
 *             SQLColumn.from("name", SQLDataType.VARCHAR),
 *             SQLColumn.fromNullable("email", SQLDataType.VARCHAR)
 *         ));
 *     }
 *     
 *     @Override
 *     public User deserializeResultSet(ResultSet rs) throws SQLException {
 *         return new User(
 *             rs.getInt("id"),
 *             rs.getString("name"),
 *             rs.getString("email")
 *         );
 *     }
 *     
 *     @Override
 *     public void insert(User user) {
 *         BuiltQuery query = SqlBuilder.insertInto(getTable())
 *             .value(ID_COL, user.getId())
 *             .value(NAME_COL, user.getName())
 *             .value(EMAIL_COL, user.getEmail())
 *             .build();
 *         executeUpdate(query);
 *     }
 * }
 * }</pre>
 * 
 * <h3>Features</h3>
 * 
 * <ul>
 *   <li><strong>Automatic Table Creation:</strong> Tables are created automatically when the repository is instantiated</li>
 *   <li><strong>Type-Safe Queries:</strong> Uses {@link SqlBuilder} for type-safe SQL query construction</li>
 *   <li><strong>Synchronous & Asynchronous:</strong> Provides both sync and async methods for database operations</li>
 *   <li><strong>Connection Management:</strong> Handles database connections automatically</li>
 *   <li><strong>Common Operations:</strong> Provides get, getAll, exists, delete operations out of the box</li>
 * </ul>
 * 
 * <h3>Required Methods</h3>
 * 
 * <p>Subclasses must implement:
 * <ul>
 *   <li>{@link #deserializeResultSet(ResultSet)} - Maps a ResultSet row to a domain object</li>
 *   <li>{@link #insert(Object)} - Inserts a new record into the database</li>
 * </ul>
 * 
 * <h3>Asynchronous Operations</h3>
 * 
 * <p>Asynchronous methods use the executor service from {@link DatabaseApi#getExecutorService()}
 * to execute database operations in a separate thread, returning {@link CompletableFuture}
 * objects for non-blocking operation.
 * 
 * @param <T> the type of domain objects managed by this repository
 * @see Repository
 * @see TableDefinition
 * @see SqlBuilder
 * @see DatabaseApi#getExecutorService()
 * @since 1.0
 */
public abstract class AbstractMariaDbRepository<T> implements Repository {

    private final DatabaseConnectionHandler db;
    private final TableDefinition table;

    public AbstractMariaDbRepository(DatabaseConnectionHandler db, TableDefinition table) {
        this.db = db;
        this.table = table;
        this.table.generate();
    }

    /**
     * Gets the {@link Table} instance for this repository.
     * 
     * <p>This method returns the table identifier used by this repository.
     * It's useful for building queries that reference this repository's table.
     * 
     * @return the {@code Table} instance for this repository
     */
    protected Table getTable() {
        return table.getTable();
    }

    /**
     * Gets all columns defined for this table.
     * 
     * <p>This method returns the list of columns that were defined when creating
     * the table. It's useful for building SELECT queries that select all columns.
     * 
     * @return list of {@code Column} instances for this table
     */
    protected List<Column> getColumns() {
        return table.getTableFields();
    }

    @SneakyThrows
    protected Connection readConnection() {
        DatabaseConnector<HikariDataSource, DatabaseCredentials> databaseConnector = this.db.getConnectorNullsafe(DatabaseType.MARIADB);
        return databaseConnector.getSafeConnection().getConnection();
    }

    /**
     * Executes a query and maps all results using the provided {@link RowMapper}.
     * 
     * <p>This method executes a SELECT query and maps all resulting rows to domain objects.
     * It handles connection management, parameter binding, and exception handling automatically.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
     *     .from(getTable())
     *     .where(NAME_COL, "John")
     *     .build();
     * 
     * List<User> users = query(query, this::deserializeResultSet);
     * }</pre>
     * 
     * @param query  the built query with SQL and parameters
     * @param mapper the mapper to convert ResultSet rows to domain objects
     * @return a list of mapped domain objects (may be empty, never null)
     * @throws RuntimeException if a database access error occurs
     */
    protected List<T> query(BuiltQuery query, RowMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection connection = readConnection();
             PreparedStatement statement = connection.prepareStatement(query.sql())) {
            for (int i = 0; i < query.params().size(); i++) {
                statement.setObject(i + 1, query.params().get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapper.map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query: " + query.sql(), e);
        }
        return results;
    }

    /**
     * Executes a query and maps the first result using the provided {@link RowMapper}.
     * 
     * <p>This method executes a SELECT query and maps the first resulting row to a domain object.
     * If no rows are returned, an empty {@link Optional} is returned.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
     *     .from(getTable())
     *     .where(ID_COL, 123)
     *     .build();
     * 
     * Optional<User> user = queryOne(query, this::deserializeResultSet);
     * }</pre>
     * 
     * @param query  the built query with SQL and parameters
     * @param mapper the mapper to convert ResultSet row to domain object
     * @return an {@link Optional} containing the mapped domain object, or empty if no results
     * @throws RuntimeException if a database access error occurs
     */
    protected Optional<T> queryOne(BuiltQuery query, RowMapper<T> mapper) {
        try (Connection connection = readConnection();
             PreparedStatement statement = connection.prepareStatement(query.sql())) {
            for (int i = 0; i < query.params().size(); i++) {
                statement.setObject(i + 1, query.params().get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapper.map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query: " + query.sql(), e);
        }
        return Optional.empty();
    }

    /**
     * Executes an update query (INSERT, UPDATE, DELETE).
     * 
     * <p>This method executes a non-SELECT query (INSERT, UPDATE, or DELETE) and returns
     * the number of rows affected. It handles connection management, parameter binding,
     * and exception handling automatically.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * BuiltQuery query = SqlBuilder.update(getTable())
     *     .set(NAME_COL, "Jane")
     *     .where(ID_COL, 123)
     *     .build();
     * 
     * int rowsAffected = executeUpdate(query);
     * }</pre>
     * 
     * @param query the built query with SQL and parameters
     * @return the number of affected rows
     * @throws RuntimeException if a database access error occurs
     */
    protected int executeUpdate(BuiltQuery query) {
        try (Connection connection = readConnection();
             PreparedStatement statement = connection.prepareStatement(query.sql())) {
            for (int i = 0; i < query.params().size(); i++) {
                statement.setObject(i + 1, query.params().get(i));
            }
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute update: " + query.sql(), e);
        }
    }

    /**
     * Checks if a query returns any rows (for existence checks).
     * 
     * <p>This method executes a SELECT query and returns {@code true} if at least one row
     * is returned, {@code false} otherwise. It's optimized for existence checks and doesn't
     * fetch all rows - it only checks if any row exists.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * BuiltQuery query = SqlBuilder.select(ID_COL)
     *     .from(getTable())
     *     .where(NAME_COL, "John")
     *     .limit(1)
     *     .build();
     * 
     * boolean exists = existsQuery(query);
     * }</pre>
     * 
     * @param query the built query with SQL and parameters
     * @return {@code true} if at least one row exists, {@code false} otherwise
     * @throws RuntimeException if a database access error occurs
     */
    protected boolean existsQuery(BuiltQuery query) {
        try (Connection connection = readConnection();
             PreparedStatement statement = connection.prepareStatement(query.sql())) {
            for (int i = 0; i < query.params().size(); i++) {
                statement.setObject(i + 1, query.params().get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute existence check: " + query.sql(), e);
        }
    }

    /**
     * Checks if a record exists with the given key value.
     * 
     * <p>This is a convenience method that checks if a record exists by its primary key
     * or any unique column. It uses {@link #existsQuery(BuiltQuery)} internally.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * boolean userExists = exists(ID_COL, 123);
     * }</pre>
     * 
     * @param keyColumn the column to check (typically the primary key)
     * @param key       the key value
     * @return {@code true} if a record exists, {@code false} otherwise
     * @throws NullPointerException if keyColumn is null
     */
    public boolean exists(Column keyColumn, Object key) {
        BuiltQuery query = SqlBuilder.select(keyColumn)
                .from(getTable())
                .where(keyColumn, key)
                .limit(1)
                .build();
        return existsQuery(query);
    }

    /**
     * Gets a single record asynchronously by key.
     * 
     * <p>This method executes {@link #getSync(Column, Object)} in a separate thread
     * and returns a {@link CompletableFuture} for non-blocking operation.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * CompletableFuture<User> future = getAsync(ID_COL, 123);
     * future.thenAccept(user -> {
     *     if (user != null) {
     *         System.out.println("Found user: " + user.getName());
     *     }
     * });
     * }</pre>
     * 
     * @param keyColumn the key column (typically the primary key)
     * @param key       the key value
     * @return a {@link CompletableFuture} with the mapped domain object (may complete with null if not found)
     * @throws NullPointerException if keyColumn is null
     */
    public CompletableFuture<T> getAsync(Column keyColumn, Object key) {
        CompletableFuture<T> asyncTask = new CompletableFuture<>();
        DatabaseProvider.getApi().getExecutorService().execute(() -> asyncTask.complete(getSync(keyColumn, key)));
        return asyncTask;
    }

    /**
     * Gets all records asynchronously.
     * 
     * <p>This method executes {@link #getAllSync()} in a separate thread and returns
     * a {@link CompletableFuture} for non-blocking operation.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * CompletableFuture<List<User>> future = getAllAsync();
     * future.thenAccept(users -> {
     *     System.out.println("Found " + users.size() + " users");
     * });
     * }</pre>
     * 
     * @return a {@link CompletableFuture} with the list of all mapped domain objects (never null, may be empty)
     */
    public CompletableFuture<List<T>> getAllAsync() {
        CompletableFuture<List<T>> asyncTask = new CompletableFuture<>();
        DatabaseProvider.getApi().getExecutorService().execute(() -> asyncTask.complete(getAllSync()));
        return asyncTask;
    }

    /**
     * Executes a query asynchronously and maps all results using the provided RowMapper.
     * @param query the built query with SQL and parameters
     * @param mapper the mapper to convert ResultSet rows to domain objects
     * @return a CompletableFuture with the list of mapped domain objects
     */
    protected CompletableFuture<List<T>> queryAsync(BuiltQuery query, RowMapper<T> mapper) {
        CompletableFuture<List<T>> asyncTask = new CompletableFuture<>();
        DatabaseProvider.getApi().getExecutorService().execute(() -> asyncTask.complete(query(query, mapper)));
        return asyncTask;
    }

    /**
     * Executes a query asynchronously and maps the first result using the provided RowMapper.
     * @param query the built query with SQL and parameters
     * @param mapper the mapper to convert ResultSet row to domain object
     * @return a CompletableFuture with an Optional containing the mapped domain object, or empty if no results
     */
    protected CompletableFuture<Optional<T>> queryOneAsync(BuiltQuery query, RowMapper<T> mapper) {
        CompletableFuture<Optional<T>> asyncTask = new CompletableFuture<>();
        DatabaseProvider.getApi().getExecutorService().execute(() -> asyncTask.complete(queryOne(query, mapper)));
        return asyncTask;
    }

    /**
     * Gets a single record by key using the builder API (synchronous).
     * 
     * <p>This method builds and executes a SELECT query to retrieve a single record
     * by its key. It selects all columns defined for the table and maps the result
     * using {@link #deserializeResultSet(ResultSet)}.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * User user = getSync(ID_COL, 123);
     * if (user != null) {
     *     System.out.println("Found user: " + user.getName());
     * }
     * }</pre>
     * 
     * @param keyColumn the key column (typically the primary key)
     * @param key       the key value
     * @return the mapped domain object, or {@code null} if not found
     * @throws NullPointerException if keyColumn is null
     * @throws RuntimeException    if a database access error occurs
     */
    public T getSync(Column keyColumn, Object key) {
        BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
                .from(getTable())
                .where(keyColumn, key)
                .build();
        return queryOne(query, this::deserializeResultSet).orElse(null);
    }

    /**
     * Gets all records from the table using the builder API (synchronous).
     * 
     * <p>This method builds and executes a SELECT query to retrieve all records from
     * the table. It selects all columns defined for the table and maps each row
     * using {@link #deserializeResultSet(ResultSet)}.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * List<User> users = getAllSync();
     * for (User user : users) {
     *     System.out.println("User: " + user.getName());
     * }
     * }</pre>
     * 
     * @return list of all mapped domain objects (never null, may be empty)
     * @throws RuntimeException if a database access error occurs
     */
    public List<T> getAllSync() {
        BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
                .from(getTable())
                .build();
        return query(query, this::deserializeResultSet);
    }

    /**
     * Deletes a record by key using the builder API.
     * 
     * <p>This method builds and executes a DELETE query to remove a record by its key.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * delete(ID_COL, 123);
     * }</pre>
     * 
     * @param keyColumn the key column (typically the primary key)
     * @param key       the key value
     * @throws NullPointerException if keyColumn is null
     * @throws RuntimeException     if a database access error occurs
     */
    public void delete(Column keyColumn, Object key) {
        BuiltQuery query = SqlBuilder.deleteFrom(getTable())
                .where(keyColumn, key)
                .build();
        executeUpdate(query);
    }


    /**
     * Maps a single row from a {@link ResultSet} to a domain object.
     * 
     * <p>This method is called by query methods to convert database rows into domain objects.
     * The ResultSet cursor is positioned at the current row when this method is called.
     * 
     * <h3>Implementation Example</h3>
     * 
     * <pre>{@code
     * @Override
     * public User deserializeResultSet(ResultSet rs) throws SQLException {
     *     return new User(
     *         rs.getInt("id"),
     *         rs.getString("name"),
     *         rs.getString("email")
     *     );
     * }
     * }</pre>
     * 
     * @param resultSet the ResultSet positioned at the current row
     * @return the mapped domain object
     * @throws SQLException if a database access error occurs
     */
    public abstract T deserializeResultSet(ResultSet resultSet);

    /**
     * Inserts a new record into the database.
     * 
     * <p>This method should build and execute an INSERT query to add a new record.
     * 
     * <h3>Implementation Example</h3>
     * 
     * <pre>{@code
     * @Override
     * public void insert(User user) {
     *     BuiltQuery query = SqlBuilder.insertInto(getTable())
     *         .value(ID_COL, user.getId())
     *         .value(NAME_COL, user.getName())
     *         .value(EMAIL_COL, user.getEmail())
     *         .build();
     *     executeUpdate(query);
     * }
     * }</pre>
     * 
     * @param value the domain object to insert
     * @throws RuntimeException if a database access error occurs
     */
    public abstract void insert(T value);

}
