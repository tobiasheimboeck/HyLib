package dev.spacetivity.tobi.hylib.database.api.connection.credentials.impl;

import dev.spacetivity.tobi.hylib.database.api.connection.credentials.DatabaseCredentials;

/**
 * Credentials for MariaDB database connections.
 * 
 * <p>This record implements {@link DatabaseCredentials} and provides all information
 * required to establish a connection to a MariaDB database server, including hostname,
 * port, username, database name, and password.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * MariaDbCredentials credentials = new MariaDbCredentials(
 *     "localhost",    // hostname
 *     3306,           // port
 *     "myuser",       // username
 *     "mydatabase",   // database name
 *     "mypassword"    // password
 * );
 * 
 * connector.establishConnection(credentials);
 * }</pre>
 * 
 * <h3>Security Considerations</h3>
 * 
 * <p>Credentials contain sensitive information (password). Be careful when:
 * <ul>
 *   <li>Logging credentials (avoid logging passwords)</li>
 *   <li>Serializing credentials (ensure secure serialization)</li>
 *   <li>Storing credentials (use secure storage mechanisms)</li>
 * </ul>
 * 
 * @param hostname the database server hostname or IP address
 * @param port     the database server port (typically 3306 for MariaDB)
 * @param username the database username
 * @param database the database name to connect to
 * @param password the database password
 * @see DatabaseCredentials
 * @see DatabaseConnector#establishConnection(DatabaseCredentials)
 * @since 1.0
 */
public record MariaDbCredentials(String hostname, int port, String username, String database, String password) implements DatabaseCredentials {

}
