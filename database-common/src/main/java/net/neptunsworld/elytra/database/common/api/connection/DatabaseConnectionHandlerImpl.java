package net.neptunsworld.elytra.database.common.api.connection;

import lombok.SneakyThrows;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnectionHandler;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnector;
import net.neptunsworld.elytra.database.api.connection.DatabaseType;
import net.neptunsworld.elytra.database.api.connection.credentials.DatabaseCredentials;
import net.neptunsworld.elytra.database.api.connection.credentials.impl.MariaDbCredentials;
import net.neptunsworld.elytra.database.api.connection.credentials.impl.RedisCredentials;
import net.neptunsworld.elytra.database.api.connection.impl.redis.RedissonConnector;
import net.neptunsworld.elytra.database.api.connection.impl.sql.MariaDbConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@SuppressWarnings("unchecked")
public class DatabaseConnectionHandlerImpl implements DatabaseConnectionHandler {

    private final List<DatabaseConnector<?, ?>> connectors;

    public DatabaseConnectionHandlerImpl(MariaDbCredentials mariaDbCredentials, RedisCredentials redisCredentials) {
        this.connectors = new ArrayList<>();
        registerConnector(new MariaDbConnector(), mariaDbCredentials);
        registerConnector(new RedissonConnector(), redisCredentials);
    }

    @SuppressWarnings("unchecked")
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
