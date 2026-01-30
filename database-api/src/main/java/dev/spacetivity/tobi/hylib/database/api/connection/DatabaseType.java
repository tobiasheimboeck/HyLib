package dev.spacetivity.tobi.hylib.database.api.connection;

/**
 * Enumeration of supported database types.
 * 
 * <p>This enum identifies the type of database system being used. It is used
 * by {@link DatabaseConnectionHandler} to retrieve the appropriate connector
 * for a specific database type.
 * 
 * <h3>Usage</h3>
 * 
 * <pre>{@code
 * DatabaseConnector<?, ?> connector = handler.getConnectorNullsafe(DatabaseType.MARIADB);
 * }</pre>
 * 
 * @see DatabaseConnectionHandler
 * @see DatabaseConnector
 * @since 1.0
 */
public enum DatabaseType {

    /**
     * MariaDB database type.
     * 
     * <p>MariaDB is a community-developed fork of MySQL. Connectors for MariaDB
     * typically use the MariaDB JDBC driver and HikariCP connection pool.
     */
    MARIADB

}
