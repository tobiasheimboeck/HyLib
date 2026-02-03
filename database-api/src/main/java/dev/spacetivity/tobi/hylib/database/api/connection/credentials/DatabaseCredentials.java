package dev.spacetivity.tobi.hylib.database.api.connection.credentials;

/**
 * Interface for database connection credentials.
 * 
 * <p>This interface provides the basic information required to establish a database
 * connection: hostname and port. Specific database implementations may extend this
 * interface to include additional credentials such as username, password, and database name.
 * 
 * <h3>Usage</h3>
 * 
 * <pre>{@code
 * DatabaseCredentials credentials = new MariaDbCredentials(
 *     "localhost", 3306, "username", "database", "password"
 * );
 * 
 * connector.establishConnection(credentials);
 * }</pre>
 * 
 * <h3>Implementations</h3>
 * 
 * <ul>
 *   <li>{@link dev.spacetivity.tobi.hylib.database.api.connection.credentials.impl.MariaDbCredentials} - Credentials for MariaDB connections</li>
 * </ul>
 * 
 * @see dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnector#establishConnection(DatabaseCredentials)
 * @see dev.spacetivity.tobi.hylib.database.api.connection.credentials.impl.MariaDbCredentials
 * @since 1.0
 */
public interface DatabaseCredentials {

    /**
     * Gets the hostname or IP address of the database server.
     * 
     * @return the database hostname (e.g., "localhost", "192.168.1.100", "db.example.com")
     */
    String hostname();

    /**
     * Gets the port number on which the database server is listening.
     * 
     * @return the database port (e.g., 3306 for MariaDB/MySQL)
     */
    int port();

}
