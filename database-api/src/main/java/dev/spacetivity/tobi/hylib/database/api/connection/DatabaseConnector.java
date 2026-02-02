package dev.spacetivity.tobi.hylib.database.api.connection;

import lombok.Getter;
import lombok.Setter;
import dev.spacetivity.tobi.hylib.database.api.connection.credentials.DatabaseCredentials;

/**
 * Base for database connectors. Abstracts the DB client; client is set via {@link #establishConnection(DatabaseCredentials)}.
 * Not thread-safe.
 *
 * @param <T> the client type (e.g. HikariDataSource)
 * @param <C> the credentials type
 * @see DatabaseConnectionHandler
 * @see DatabaseType
 * @see DatabaseCredentials
 * @since 1.0
 */
@Getter
public abstract class DatabaseConnector<T, C extends DatabaseCredentials> {

    /** The database type this connector handles. */
    private final DatabaseType type;

    /** The database client; set after {@link #establishConnection(DatabaseCredentials)}. */
    @Setter
    private T client;

    /**
     * Creates a connector for the given database type.
     *
     * @param type the database type
     * @throws NullPointerException if type is null
     */
    public DatabaseConnector(DatabaseType type) {
        this.type = type;
        this.client = null;
    }

    /**
     * Establishes a connection; implementations must create the client and call {@link #setClient(Object)}.
     *
     * @param credentials the credentials
     * @throws NullPointerException if credentials is null
     * @throws RuntimeException if connection cannot be established
     */
    public abstract void establishConnection(C credentials);

    /**
     * Returns the database client; throws if not connected.
     *
     * @return the client, never null
     * @throws NullPointerException if no connection has been established
     * @see #establishConnection(DatabaseCredentials)
     */
    public T getSafeConnection() {
        if (this.client == null) throw new NullPointerException("Database connection is null!");
        return this.client;
    }

}
