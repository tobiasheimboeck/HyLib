package dev.spacetivity.tobi.hylib.database.api;

/**
 * Global access to {@link DatabaseApi}. Register via {@link #register(DatabaseApi)} before {@link #getApi()}.
 * Thread-safe for reads after initialization.
 *
 * @see DatabaseApi
 * @since 1.0
 */
public class DatabaseProvider {

    private static DatabaseApi api = null;

    /**
     * Returns the registered {@link DatabaseApi} instance.
     *
     * @return the registered API
     * @throws IllegalStateException if no API has been registered
     * @see #register(DatabaseApi)
     */
    public static DatabaseApi getApi() {
        DatabaseApi api = DatabaseProvider.api;
        if (api == null) throw new IllegalStateException("Api instance is null");
        return api;
    }

    /**
     * Registers the {@link DatabaseApi} for global access. Replaces any previous instance.
     *
     * @param api the API to register
     * @throws NullPointerException if api is null
     * @see #getApi()
     */
    public static void register(DatabaseApi api) {
        DatabaseProvider.api = api;
    }

}
