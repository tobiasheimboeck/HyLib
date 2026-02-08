package dev.spacetivity.tobi.hylib.database.api;

import dev.spacetivity.tobi.hylib.database.api.cache.CacheLoader;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.hylib.database.api.repository.RepositoryLoader;
import dev.spacetivity.tobi.hylib.database.api.scheduler.TaskScheduler;

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

    /**
     * Returns the task scheduler for delayed and periodic execution.
     *
     * <p>The scheduler provides timing capabilities (delays and fixed-rate execution).
     * Actual task execution is delegated to the {@link #getExecutorService()} and therefore
     * typically runs on virtual threads.</p>
     *
     * <p>The scheduler is provided by this {@link DatabaseApi} but may be used for
     * arbitrary scheduling purposes, not limited to database-specific tasks.</p>
     *
     * <p>Consumers must not shut down or block the scheduler. Long-running or blocking
     * work must be executed via the executor service.</p>
     *
     * @return the task scheduler, never null
     */
    TaskScheduler getScheduler();

}
