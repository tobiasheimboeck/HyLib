package dev.spacetivity.tobi.database.api.repository.impl;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import dev.spacetivity.tobi.database.api.DatabaseProvider;
import dev.spacetivity.tobi.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.database.api.connection.DatabaseConnector;
import dev.spacetivity.tobi.database.api.connection.DatabaseType;
import dev.spacetivity.tobi.database.api.connection.credentials.DatabaseCredentials;
import dev.spacetivity.tobi.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.database.api.connection.impl.sql.RowMapper;
import dev.spacetivity.tobi.database.api.connection.impl.sql.TableDefinition;
import dev.spacetivity.tobi.database.api.connection.impl.sql.Table;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.BuiltQuery;
import dev.spacetivity.tobi.database.api.connection.impl.sql.builder.SqlBuilder;
import dev.spacetivity.tobi.database.api.repository.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractMariaDbRepository<T> implements Repository {

    private final DatabaseConnectionHandler db;
    private final TableDefinition table;

    public AbstractMariaDbRepository(DatabaseConnectionHandler db, TableDefinition table) {
        this.db = db;
        this.table = table;
        this.table.generate();
    }

    /**
     * Gets the Table instance for this repository.
     * @return the Table instance
     */
    protected Table getTable() {
        return table.getTable();
    }

    /**
     * Gets all columns defined for this table.
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
     * Executes a query and maps all results using the provided RowMapper.
     * @param query the built query with SQL and parameters
     * @param mapper the mapper to convert ResultSet rows to domain objects
     * @return a list of mapped domain objects
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
     * Executes a query and maps the first result using the provided RowMapper.
     * @param query the built query with SQL and parameters
     * @param mapper the mapper to convert ResultSet row to domain object
     * @return an Optional containing the mapped domain object, or empty if no results
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
     * @param query the built query with SQL and parameters
     * @return the number of affected rows
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
     * @param query the built query with SQL and parameters
     * @return true if at least one row exists, false otherwise
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
     * @param keyColumn the column to check
     * @param key the key value
     * @return true if a record exists, false otherwise
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
     * @param keyColumn the key column
     * @param key the key value
     * @return a CompletableFuture with the mapped domain object
     */
    public CompletableFuture<T> getAsync(Column keyColumn, Object key) {
        CompletableFuture<T> asyncTask = new CompletableFuture<>();
        DatabaseProvider.getApi().getExecutorService().execute(() -> asyncTask.complete(getSync(keyColumn, key)));
        return asyncTask;
    }

    /**
     * Gets all records asynchronously.
     * @return a CompletableFuture with the list of mapped domain objects
     */
    public CompletableFuture<List<T>> getAllAsync() {
        CompletableFuture<List<T>> asyncTask = new CompletableFuture<>();
        DatabaseProvider.getApi().getExecutorService().execute(() -> asyncTask.complete(getAllSync()));
        return asyncTask;
    }

    /**
     * Gets a single record by key using the builder API.
     * @param keyColumn the key column
     * @param key the key value
     * @return the mapped domain object, or null if not found
     */
    public T getSync(Column keyColumn, Object key) {
        BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
                .from(getTable())
                .where(keyColumn, key)
                .build();
        return queryOne(query, this::deserializeResultSet).orElse(null);
    }

    /**
     * Gets all records from the table using the builder API.
     * @return list of all mapped domain objects
     */
    public List<T> getAllSync() {
        BuiltQuery query = SqlBuilder.select(getColumns().toArray(new Column[0]))
                .from(getTable())
                .build();
        return query(query, this::deserializeResultSet);
    }

    /**
     * Deletes a record by key using the builder API.
     * @param keyColumn the key column
     * @param key the key value
     */
    public void delete(Column keyColumn, Object key) {
        BuiltQuery query = SqlBuilder.deleteFrom(getTable())
                .where(keyColumn, key)
                .build();
        executeUpdate(query);
    }


    public abstract T deserializeResultSet(ResultSet resultSet);

    public abstract void insert(T value);

}
