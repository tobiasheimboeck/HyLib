package dev.spacetivity.tobi.hylib.database.api;

import dev.spacetivity.tobi.hylib.database.api.cache.CacheLoader;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.hylib.database.api.repository.RepositoryLoader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Main API for database operations (connections, cache, repositories, executor).
 * Obtain via {@link DatabaseProvider#getApi()} after {@link DatabaseProvider#register(DatabaseApi)}.
 *
 * @see DatabaseProvider
 * @since 1.0
 */
public interface DatabaseApi {

    /**
     * Returns the executor service for asynchronous tasks.
     *
     * @return the executor service, never null
     */
    ExecutorService getExecutorService();

    /**
     * Returns the database connection handler (connectors, pooling). Null until a connection is established.
     *
     * @return the connection handler, or null
     * @see DatabaseConnectionHandler
     */
    DatabaseConnectionHandler getDatabaseConnectionHandler();

    /**
     * Returns the cache loader for in-memory caches.
     *
     * @return the cache loader, never null
     * @see CacheLoader
     */
    CacheLoader getCacheLoader();

    /**
     * Returns the repository loader for database repositories.
     *
     * @return the repository loader, never null
     * @see RepositoryLoader
     */
    RepositoryLoader getRepositoryLoader();

    /**
     * Executes a task asynchronously using the executor service.
     * 
     * @param runnable the task to execute
     * @return a Future representing the task
     * @throws NullPointerException if runnable is null
     */
    Future<?> execute(Runnable runnable);

}
