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
 * Base for MariaDB repositories. Handles table creation, connections, CRUD; sync and async via {@link SqlBuilder}.
 * Subclasses must implement {@link #deserializeResultSet(ResultSet)} and {@link #insert(Object)}.
 *
 * @param <T> the domain type
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
     * Returns the table for this repository.
     *
     * @return the Table instance
     */
    protected Table getTable() {
        return table.getTable();
    }

    /**
     * Returns all columns defined for this table.
     *
     * @return list of Column instances
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
     * Executes a SELECT query and maps all rows with the given mapper.
     *
     * @param query  the built query (SQL and params)
     * @param mapper the mapper for ResultSet rows
     * @return list of mapped objects (may be empty, never null)
     * @throws RuntimeException if a database error occurs
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
     * Returns true if the query returns at least one row.
     *
     * @param query the built query
     * @return true if at least one row exists
     * @throws RuntimeException if a database error occurs
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
     * Returns true if a record exists for the given key column and value.
     *
     * @param keyColumn the column (e.g. primary key)
     * @param key       the key value
     * @return true if a record exists
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
     * Returns a CompletableFuture with the record for the given key (async).
     *
     * @param keyColumn the key column
     * @param key       the key value
     * @return CompletableFuture with the mapped object, or null if not found
     * @throws NullPointerException if keyColumn is null
     */
    public CompletableFuture<T> getAsync(Column keyColumn, Object key) {
        CompletableFuture<T> asyncTask = new CompletableFuture<>();
        DatabaseProvider.getApi().getExecutorService().execute(() -> asyncTask.complete(getSync(keyColumn, key)));
        return asyncTask;
    }

    /**
     * Returns a CompletableFuture with all records (async).
     *
     * @return CompletableFuture with list of mapped objects (never null)
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
     * Returns a single record by key (sync). Selects all table columns and maps with {@link #deserializeResultSet(ResultSet)}.
     *
     * @param keyColumn the key column
     * @param key       the key value
     * @return the mapped object, or null if not found
     * @throws NullPointerException if keyColumn is null
     * @throws RuntimeException if a database error occurs
     */
    public T getSync(Column keyColumn, Object key) {
        BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
                .from(getTable())
                .where(keyColumn, key)
                .build();
        return queryOne(query, this::deserializeResultSet).orElse(null);
    }

    /**
     * Returns all records from the table (sync). Maps each row with {@link #deserializeResultSet(ResultSet)}.
     *
     * @return list of mapped objects (never null)
     * @throws RuntimeException if a database error occurs
     */
    public List<T> getAllSync() {
        BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
                .from(getTable())
                .build();
        return query(query, this::deserializeResultSet);
    }

    /**
     * Deletes the record for the given key column and value.
     *
     * @param keyColumn the key column
     * @param key       the key value
     * @throws NullPointerException if keyColumn is null
     * @throws RuntimeException if a database error occurs
     */
    public void delete(Column keyColumn, Object key) {
        BuiltQuery query = SqlBuilder.deleteFrom(getTable())
                .where(keyColumn, key)
                .build();
        executeUpdate(query);
    }


    /**
     * Maps the current ResultSet row to a domain object. Cursor is at the current row.
     *
     * @param resultSet the ResultSet at the current row
     * @return the mapped domain object
     * @throws SQLException if a database error occurs
     */
    public abstract T deserializeResultSet(ResultSet resultSet);

    /**
     * Inserts a new record. Subclasses build and execute an INSERT query (e.g. via SqlBuilder).
     *
     * @param value the domain object to insert
     * @throws RuntimeException if a database error occurs
     */
    public abstract void insert(T value);

}
