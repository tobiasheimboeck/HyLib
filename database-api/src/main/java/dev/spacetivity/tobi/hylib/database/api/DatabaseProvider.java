package dev.spacetivity.tobi.hylib.database.api;

/**
 * Provider class for accessing the global {@link DatabaseApi} instance.
 * 
 * <p>This class provides a singleton-like access pattern for the database API.
 * Before using the API, you must register an instance using {@link #register(DatabaseApi)}.
 * 
 * <h3>Usage Pattern</h3>
 * 
 * <pre>{@code
 * // 1. Initialize and register the API
 * DatabaseApi api = new DatabaseApiImpl();
 * DatabaseProvider.register(api);
 * 
 * // 2. Access the API from anywhere
 * DatabaseApi api = DatabaseProvider.getApi();
 * }</pre>
 * 
 * <h3>Thread Safety</h3>
 * 
 * <p>This class is thread-safe for reading after initialization. However, care should
 * be taken when registering the API instance - it should typically be done once during
 * application startup.
 * 
 * @see DatabaseApi
 * @since 1.0
 */
public class DatabaseProvider {

    private static DatabaseApi api = null;

    /**
     * Gets the registered {@link DatabaseApi} instance.
     * 
     * <p>This method returns the globally registered API instance. The API must be
     * registered first using {@link #register(DatabaseApi)} before calling this method.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * DatabaseApi api = DatabaseProvider.getApi();
     * }</pre>
     * 
     * @return the registered {@code DatabaseApi} instance
     * @throws IllegalStateException if no API instance has been registered
     * @see #register(DatabaseApi)
     */
    public static DatabaseApi getApi() {
        DatabaseApi api = DatabaseProvider.api;
        if (api == null) throw new IllegalStateException("Api instance is null");
        return api;
    }

    /**
     * Registers a {@link DatabaseApi} instance for global access.
     * 
     * <p>This method should be called once during application initialization to register
     * the database API instance. After registration, the API can be accessed from anywhere
     * using {@link #getApi()}.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * DatabaseApi api = new DatabaseApiImpl();
     * DatabaseProvider.register(api);
     * }</pre>
     * 
     * <p><strong>Note:</strong> Registering a new instance will replace any previously
     * registered instance. This should typically only be done once during application startup.
     * 
     * @param api the {@code DatabaseApi} instance to register
     * @throws NullPointerException if api is null
     * @see #getApi()
     */
    public static void register(DatabaseApi api) {
        DatabaseProvider.api = api;
    }

}
