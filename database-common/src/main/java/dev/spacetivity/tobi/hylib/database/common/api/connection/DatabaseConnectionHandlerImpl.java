package dev.spacetivity.tobi.hylib.database.common.api.connection;

import lombok.SneakyThrows;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnector;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseType;
import dev.spacetivity.tobi.hylib.database.api.connection.credentials.DatabaseCredentials;
import dev.spacetivity.tobi.hylib.database.api.connection.credentials.impl.MariaDbCredentials;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.MariaDbConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class DatabaseConnectionHandlerImpl implements DatabaseConnectionHandler {

    private final List<DatabaseConnector<?, ?>> connectors;

    public DatabaseConnectionHandlerImpl(MariaDbCredentials mariaDbCredentials) {
        this.connectors = new ArrayList<>();
        registerConnector(new MariaDbConnector(), mariaDbCredentials);
    }

    @SneakyThrows
    @Override
    public <T, C extends DatabaseCredentials> DatabaseConnector<T, C> getConnectorNullsafe(DatabaseType type) {
        Optional<DatabaseConnector<?, ?>> connector = this.connectors.stream()
                .filter(currentConnector -> currentConnector.getType().equals(type))
                .findFirst();

        return (DatabaseConnector<T, C>) connector.orElseThrow(() -> new ClassNotFoundException("Connector not found!"));
    }

    @Override
    public <T, C extends DatabaseCredentials> DatabaseConnector<T, C> getConnectorNullsafe(Class<T> clientClass, DatabaseType type) {
        DatabaseConnector<Object, DatabaseCredentials> databaseConnector = getConnectorNullsafe(type);
        return (DatabaseConnector<T, C>) databaseConnector;
    }

    @Override
    public <T, C extends DatabaseCredentials> DatabaseConnector<T, C> registerConnector(DatabaseConnector<T, C> connector, DatabaseCredentials credentials) {
        this.connectors.add(connector);
        connector.establishConnection((C) credentials);
        return connector;
    }

}
