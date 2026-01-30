package dev.spacetivity.tobi.hylib.database.api;

import dev.spacetivity.tobi.hylib.database.api.cache.CacheLoader;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.hylib.database.api.repository.RepositoryLoader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Main API interface for database operations.
 * 
 * <p>This interface provides access to all database-related functionality including:
 * <ul>
 *   <li>Database connection handling</li>
 *   <li>Cache management</li>
 *   <li>Repository loading</li>
 *   <li>Asynchronous task execution</li>
 * </ul>
 * 
 * <p>Instances of this interface should be obtained via {@link DatabaseProvider#getApi()}
 * after registering an implementation using {@link DatabaseProvider#register(DatabaseApi)}.
 * 
 * <h3>Example Usage</h3>
 * 
 * <pre>{@code
 * // Initialize and register
 * DatabaseApi api = new DatabaseApiImpl();
 * DatabaseProvider.register(api);
 * 
 * // Use the API
 * DatabaseApi api = DatabaseProvider.getApi();
 * }</pre>
 * 
 * @see DatabaseProvider
 * @since 1.0
 */
public interface DatabaseApi {

    /**
     * Gets the executor service for asynchronous task execution.
     * 
     * <p>The executor service is used internally for asynchronous database operations
     * and can also be used by consumers for their own asynchronous tasks.
     * 
     * @return the executor service, never null
     */
    ExecutorService getExecutorService();

    /**
     * Gets the database connection handler for managing database connections.
     * 
     * <p>The connection handler provides access to database connectors and manages
     * connection pooling. A connection must be established before this returns a non-null value.
     * 
     * @return the database connection handler, may be null if no connection has been established
     * @see DatabaseConnectionHandler
     */
    DatabaseConnectionHandler getDatabaseConnectionHandler();

    /**
     * Gets the cache loader for managing in-memory caches.
     * 
     * <p>The cache loader allows you to register and manage in-memory caches
     * for frequently accessed data.
     * 
     * @return the cache loader, never null
     * @see CacheLoader
     */
    CacheLoader getCacheLoader();

    /**
     * Gets the repository loader for managing database repositories.
     * 
     * <p>The repository loader allows you to register and retrieve database repositories
     * for type-safe database operations.
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
