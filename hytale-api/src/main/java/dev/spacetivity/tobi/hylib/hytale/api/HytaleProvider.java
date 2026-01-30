package dev.spacetivity.tobi.hylib.hytale.api;

/**
 * Provider class for accessing the global {@link HytaleApi} instance.
 * 
 * <p>This class provides a singleton-like access pattern for the Hytale API.
 * Before using the API, you must register an instance using {@link #register(HytaleApi)}.
 * 
 * <h3>Usage Pattern</h3>
 * 
 * <pre>{@code
 * // 1. Initialize and register the API
 * HytaleApi api = new HytaleApiImpl();
 * HytaleProvider.register(api);
 * 
 * // 2. Access the API from anywhere
 * HytaleApi api = HytaleProvider.getApi();
 * CodecBuilder<MyConfig> builder = api.newCodec(MyConfig.class);
 * }</pre>
 * 
 * <h3>Thread Safety</h3>
 * 
 * <p>This class is thread-safe for reading after initialization. However, care should
 * be taken when registering the API instance - it should typically be done once during
 * application startup.
 * 
 * @see HytaleApi
 * @since 1.0
 */
public class HytaleProvider {

    private static HytaleApi api = null;

    /**
     * Gets the registered {@link HytaleApi} instance.
     * 
     * <p>This method returns the globally registered API instance. The API must be
     * registered first using {@link #register(HytaleApi)} before calling this method.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * HytaleApi api = HytaleProvider.getApi();
     * CodecBuilder<MyConfig> builder = api.newCodec(MyConfig.class);
     * }</pre>
     * 
     * @return the registered {@code HytaleApi} instance
     * @throws IllegalStateException if no API instance has been registered
     * @see #register(HytaleApi)
     * @see HytaleApi#newCodec(Class)
     */
    public static HytaleApi getApi() {
        HytaleApi api = HytaleProvider.api;
        if (api == null) throw new IllegalStateException("Api instance is null");
        return api;
    }

    /**
     * Registers a {@link HytaleApi} instance for global access.
     * 
     * <p>This method should be called once during application initialization to register
     * the Hytale API instance. After registration, the API can be accessed from anywhere
     * using {@link #getApi()}.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * HytaleApi api = new HytaleApiImpl();
     * HytaleProvider.register(api);
     * }</pre>
     * 
     * <p><strong>Note:</strong> Registering a new instance will replace any previously
     * registered instance. This should typically only be done once during application startup.
     * 
     * @param api the {@code HytaleApi} instance to register
     * @throws NullPointerException if api is null
     * @see #getApi()
     */
    public static void register(HytaleApi api) {
        HytaleProvider.api = api;
    }

}
