package dev.spacetivity.tobi.database.api;

import dev.spacetivity.tobi.database.api.cache.CacheLoader;
import dev.spacetivity.tobi.database.api.config.CodecLoader;
import dev.spacetivity.tobi.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.database.api.repository.RepositoryLoader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface DatabaseApi {

    ExecutorService getExecutorService();

    DatabaseConnectionHandler getDatabaseConnectionHandler();

    CacheLoader getCacheLoader();

    RepositoryLoader getRepositoryLoader();

    CodecLoader getCodecLoader();

    Future<?> execute(Runnable runnable);

}
