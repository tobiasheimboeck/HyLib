package dev.spacetivity.tobi.hylib.hytale.api;

/**
 * Global access to {@link HytaleApi}. Register via {@link #register(HytaleApi)} before {@link #getApi()}.
 * Thread-safe for reads after initialization.
 *
 * @see HytaleApi
 * @since 1.0
 */
public class HytaleProvider {

    private static HytaleApi api = null;

    /**
     * Returns the registered {@link HytaleApi} instance.
     *
     * @return the registered API
     * @throws IllegalStateException if no API has been registered
     * @see #register(HytaleApi)
     */
    public static HytaleApi getApi() {
        HytaleApi api = HytaleProvider.api;
        if (api == null) throw new IllegalStateException("Api instance is null");
        return api;
    }

    /**
     * Registers the {@link HytaleApi} for global access. Replaces any previous instance.
     *
     * @param api the API to register
     * @throws NullPointerException if api is null
     * @see #getApi()
     */
    public static void register(HytaleApi api) {
        HytaleProvider.api = api;
    }

}
