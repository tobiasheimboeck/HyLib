package dev.spacetivity.tobi.hylib.database.api.connection;

import dev.spacetivity.tobi.hylib.database.api.connection.credentials.DatabaseCredentials;

/**
 * Handler for managing database connections and connectors.
 * 
 * <p>This interface provides methods to retrieve and register database connectors
 * for different database types. Connectors abstract the underlying database client
 * (e.g., HikariCP DataSource for MariaDB) and provide type-safe access to connections.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * DatabaseConnectionHandler handler = DatabaseProvider.getApi().getDatabaseConnectionHandler();
 * 
 * // Get a connector for MariaDB
 * DatabaseConnector<HikariDataSource, MariaDbCredentials> connector = 
 *     handler.getConnectorNullsafe(DatabaseType.MARIADB);
 * 
 * // Get a safe connection (throws if not connected)
 * HikariDataSource dataSource = connector.getSafeConnection();
 * Connection connection = dataSource.getConnection();
 * }</pre>
 * 
 * <h3>Connector Registration</h3>
 * 
 * <p>Connectors are typically registered automatically when establishing a connection
 * via {@link DatabaseConnector#establishConnection(DatabaseCredentials)}. However, you
 * can also register connectors manually using {@link #registerConnector(DatabaseConnector, DatabaseCredentials)}.
 * 
 * @see DatabaseConnector
 * @see DatabaseType
 * @see DatabaseCredentials
 * @see DatabaseApi#getDatabaseConnectionHandler()
 * @since 1.0
 */
public interface DatabaseConnectionHandler {

    /**
     * Gets a database connector for the specified database type.
     * 
     * <p>This method returns a connector that matches the specified database type.
     * If no connector is registered for the given type, this method returns {@code null}
     * instead of throwing an exception.
     * 
     * <p>The returned connector uses the default client class for the database type.
     * For MariaDB, this is typically {@code HikariDataSource}.
     * 
     * @param <T>  the type of the database client (e.g., HikariDataSource)
     * @param <C>  the type of credentials required by the connector
     * @param type the database type (e.g., {@link DatabaseType#MARIADB})
     * @return the connector for the specified type, or {@code null} if not registered
     * @throws NullPointerException if type is null
     * @see #getConnectorNullsafe(Class, DatabaseType)
     */
    <T, C extends DatabaseCredentials> DatabaseConnector<T, C> getConnectorNullsafe(DatabaseType type);

    /**
     * Gets a database connector for the specified database type and client class.
     * 
     * <p>This method allows you to specify the exact client class type you expect.
     * This is useful when you need type-safe access to a specific client implementation.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * DatabaseConnector<HikariDataSource, MariaDbCredentials> connector = 
     *     handler.getConnectorNullsafe(HikariDataSource.class, DatabaseType.MARIADB);
     * }</pre>
     * 
     * @param <T>         the type of the database client (e.g., HikariDataSource)
     * @param <C>         the type of credentials required by the connector
     * @param clientClass the expected client class
     * @param type        the database type (e.g., {@link DatabaseType#MARIADB})
     * @return the connector for the specified type and client class, or {@code null} if not registered
     * @throws NullPointerException if clientClass or type is null
     * @see #getConnectorNullsafe(DatabaseType)
     */
    <T, C extends DatabaseCredentials> DatabaseConnector<T, C> getConnectorNullsafe(Class<T> clientClass, DatabaseType type);

    /**
     * Registers a database connector with the specified credentials.
     * 
     * <p>This method registers a connector instance and establishes its connection
     * using the provided credentials. After registration, the connector can be
     * retrieved using {@link #getConnectorNullsafe(DatabaseType)}.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * MariaDbConnector connector = new MariaDbConnector();
     * MariaDbCredentials credentials = new MariaDbCredentials(
     *     "localhost", 3306, "user", "database", "password"
     * );
     * 
     * DatabaseConnector<?, ?> registered = handler.registerConnector(connector, credentials);
     * }</pre>
     * 
     * @param <T>         the type of the database client
     * @param <C>         the type of credentials
     * @param connector   the connector to register
     * @param credentials the credentials to use for establishing the connection
     * @return the registered connector (same instance as the parameter)
     * @throws NullPointerException if connector or credentials is null
     * @throws RuntimeException     if the connection cannot be established
     */
    <T, C extends DatabaseCredentials> DatabaseConnector<T, C> registerConnector(DatabaseConnector<T, C> connector, DatabaseCredentials credentials);
}
