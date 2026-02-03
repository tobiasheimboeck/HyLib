package dev.spacetivity.tobi.hylib.database.api.connection;

import dev.spacetivity.tobi.hylib.database.api.connection.credentials.DatabaseCredentials;

/**
 * Handler for database connectors by type. Connectors abstract the DB client (e.g. HikariCP for MariaDB).
 * Register via {@link #registerConnector(DatabaseConnector, DatabaseCredentials)} or when establishing a connection.
 *
 * @see DatabaseConnector
 * @see DatabaseType
 * @see DatabaseCredentials
 * @see dev.spacetivity.tobi.hylib.database.api.DatabaseApi#getDatabaseConnectionHandler()
 * @since 1.0
 */
public interface DatabaseConnectionHandler {

    /**
     * Returns the connector for the given database type, or null if not registered.
     *
     * @param <T>  the client type (e.g. HikariDataSource)
     * @param <C>  the credentials type
     * @param type the database type
     * @return the connector, or null
     * @throws NullPointerException if type is null
     * @see #getConnectorNullsafe(Class, DatabaseType)
     */
    <T, C extends DatabaseCredentials> DatabaseConnector<T, C> getConnectorNullsafe(DatabaseType type);

    /**
     * Returns the connector for the given type and client class, or null if not registered.
     *
     * @param <T>         the client type
     * @param <C>         the credentials type
     * @param clientClass the expected client class
     * @param type        the database type
     * @return the connector, or null
     * @throws NullPointerException if clientClass or type is null
     * @see #getConnectorNullsafe(DatabaseType)
     */
    <T, C extends DatabaseCredentials> DatabaseConnector<T, C> getConnectorNullsafe(Class<T> clientClass, DatabaseType type);

    /**
     * Registers a connector and establishes its connection with the given credentials.
     *
     * @param <T>         the client type
     * @param <C>         the type of credentials
     * @param connector   the connector to register
     * @param credentials the credentials to use for establishing the connection
     * @return the registered connector (same instance as the parameter)
     * @throws NullPointerException if connector or credentials is null
     * @throws RuntimeException     if the connection cannot be established
     */
    <T, C extends DatabaseCredentials> DatabaseConnector<T, C> registerConnector(DatabaseConnector<T, C> connector, DatabaseCredentials credentials);
}
