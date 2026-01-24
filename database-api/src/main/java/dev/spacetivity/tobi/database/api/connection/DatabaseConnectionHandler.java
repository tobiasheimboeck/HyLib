package dev.spacetivity.tobi.database.api.connection;

import dev.spacetivity.tobi.database.api.connection.credentials.DatabaseCredentials;

public interface DatabaseConnectionHandler {

    <T, C extends DatabaseCredentials> DatabaseConnector<T, C> getConnectorNullsafe(DatabaseType type);

    <T, C extends DatabaseCredentials> DatabaseConnector<T, C> getConnectorNullsafe(Class<T> clientClass, DatabaseType type);

    <T, C extends DatabaseCredentials> DatabaseConnector<T, C> registerConnector(DatabaseConnector<T, C> connector, DatabaseCredentials credentials);
}
