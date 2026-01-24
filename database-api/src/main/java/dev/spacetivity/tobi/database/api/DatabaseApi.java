package dev.spacetivity.tobi.database.api;

import dev.spacetivity.tobi.database.api.cache.CacheLoader;
import dev.spacetivity.tobi.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.database.api.repository.RepositoryLoader;

import java.util.concurrent.ExecutorService;

public interface DatabaseApi {

    ExecutorService getExecutorService();

    DatabaseConnectionHandler getDatabaseConnectionHandler();

    CacheLoader getCacheLoader();

    RepositoryLoader getRepositoryLoader();

}
