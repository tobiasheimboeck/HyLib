package dev.spacetivity.tobi.hylib.database.api.connection;

import lombok.Getter;
import lombok.Setter;
import dev.spacetivity.tobi.hylib.database.api.connection.credentials.DatabaseCredentials;

/**
 * Abstract base class for database connectors.
 * 
 * <p>A connector abstracts the underlying database client (e.g., HikariCP DataSource)
 * and provides a unified interface for establishing and accessing database connections.
 * 
 * <p>Each connector is associated with a specific {@link DatabaseType} and manages
 * a single client instance. The client is set after establishing a connection via
 * {@link #establishConnection(DatabaseCredentials)}.
 * 
 * <h3>Implementation Example</h3>
 * 
 * <pre>{@code
 * public class MariaDbConnector extends DatabaseConnector<HikariDataSource, MariaDbCredentials> {
 *     public MariaDbConnector() {
 *         super(DatabaseType.MARIADB);
 *     }
 *     
 *     @Override
 *     public void establishConnection(MariaDbCredentials credentials) {
 *         HikariConfig config = new HikariConfig();
 *         config.setJdbcUrl("jdbc:mariadb://" + credentials.hostname() + ":" + credentials.port() + "/" + credentials.database());
 *         config.setUsername(credentials.username());
 *         config.setPassword(credentials.password());
 *         this.setClient(new HikariDataSource(config));
 *     }
 * }
 * }</pre>
 * 
 * <h3>Usage</h3>
 * 
 * <pre>{@code
 * MariaDbConnector connector = new MariaDbConnector();
 * MariaDbCredentials credentials = new MariaDbCredentials("localhost", 3306, "user", "db", "pass");
 * connector.establishConnection(credentials);
 * 
 * HikariDataSource dataSource = connector.getSafeConnection();
 * Connection connection = dataSource.getConnection();
 * }</pre>
 * 
 * <h3>Thread Safety</h3>
 * 
 * <p>Connectors are not thread-safe. Each connector should be used by a single thread
 * or accessed with external synchronization.
 * 
 * @param <T> the type of the database client (e.g., HikariDataSource)
 * @param <C> the type of credentials required to establish a connection
 * @see DatabaseConnectionHandler
 * @see DatabaseType
 * @see DatabaseCredentials
 * @since 1.0
 */
@Getter
public abstract class DatabaseConnector<T, C extends DatabaseCredentials> {

    /**
     * The database type this connector handles.
     */
    private final DatabaseType type;

    /**
     * The database client instance. Set after {@link #establishConnection(DatabaseCredentials)} is called.
     */
    @Setter
    private T client;

    /**
     * Creates a new database connector for the specified database type.
     * 
     * @param type the database type this connector handles
     * @throws NullPointerException if type is null
     */
    public DatabaseConnector(DatabaseType type) {
        this.type = type;
        this.client = null;
    }

    /**
     * Establishes a connection to the database using the provided credentials.
     * 
     * <p>This method should initialize the database client and set it via
     * {@link #setClient(Object)}. After calling this method, {@link #getSafeConnection()}
     * will return a non-null client instance.
     * 
     * <h3>Implementation Requirements</h3>
     * 
     * <p>Implementations must:
     * <ul>
     *   <li>Create and configure the database client</li>
     *   <li>Call {@link #setClient(Object)} with the initialized client</li>
     *   <li>Handle connection errors appropriately</li>
     * </ul>
     * 
     * @param credentials the credentials to use for establishing the connection
     * @throws NullPointerException if credentials is null
     * @throws RuntimeException     if the connection cannot be established
     */
    public abstract void establishConnection(C credentials);

    /**
     * Gets the database client instance, throwing an exception if not connected.
     * 
     * <p>This method returns the client instance set by {@link #establishConnection(DatabaseCredentials)}.
     * If no connection has been established, this method throws a {@code NullPointerException}.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * HikariDataSource dataSource = connector.getSafeConnection();
     * Connection connection = dataSource.getConnection();
     * }</pre>
     * 
     * @return the database client instance, never null
     * @throws NullPointerException if no connection has been established (client is null)
     * @see #establishConnection(DatabaseCredentials)
     */
    public T getSafeConnection() {
        if (this.client == null) throw new NullPointerException("Database connection is null!");
        return this.client;
    }

}
