package dev.spacetivity.tobi.database.common;

import lombok.Getter;
import dev.spacetivity.tobi.database.api.DatabaseApi;
import dev.spacetivity.tobi.database.api.cache.CacheLoader;
import dev.spacetivity.tobi.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.database.api.connection.credentials.impl.MariaDbCredentials;
import dev.spacetivity.tobi.database.api.repository.RepositoryLoader;
import dev.spacetivity.tobi.database.common.api.cache.CacheLoaderImpl;
import dev.spacetivity.tobi.database.common.api.connection.DatabaseConnectionHandlerImpl;
import dev.spacetivity.tobi.database.common.api.repository.RepositoryLoaderImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class DatabaseApiImpl implements DatabaseApi {

    private final ExecutorService executorService;
    private final DatabaseConnectionHandler databaseConnectionHandler;
    private final CacheLoader cacheLoader;
    private final RepositoryLoader repositoryLoader;

    public DatabaseApiImpl(MariaDbCredentials mariaDbCredentials) {
        this.executorService = Executors.newCachedThreadPool();
        this.databaseConnectionHandler = new DatabaseConnectionHandlerImpl(mariaDbCredentials);
        this.cacheLoader = new CacheLoaderImpl();
        this.repositoryLoader = new RepositoryLoaderImpl();
    }

}
