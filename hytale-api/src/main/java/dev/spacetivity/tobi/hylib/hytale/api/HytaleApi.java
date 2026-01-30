package dev.spacetivity.tobi.hylib.hytale.api;

import dev.spacetivity.tobi.hylib.hytale.api.config.CodecBuilder;

/**
 * Main API interface for Hytale-specific functionality.
 * 
 * <p>This interface provides access to Hytale-specific features including:
 * <ul>
 *   <li>Codec building for type-safe configuration</li>
 * </ul>
 * 
 * <p>Instances of this interface should be obtained via {@link HytaleProvider#getApi()}
 * after registering an implementation using {@link HytaleProvider#register(HytaleApi)}.
 * 
 * <h3>Example Usage</h3>
 * 
 * <pre>{@code
 * // Initialize and register
 * HytaleApi api = new HytaleApiImpl();
 * HytaleProvider.register(api);
 * 
 * // Use the API
 * HytaleApi api = HytaleProvider.getApi();
 * CodecBuilder<MyConfig> builder = api.newCodec(MyConfig.class);
 * }</pre>
 * 
 * @see HytaleProvider
 * @see CodecBuilder
 * @since 1.0
 */
public interface HytaleApi {

    /**
     * Creates a new {@link CodecBuilder} for the specified configuration class.
     * 
     * <p>This method provides a fluent DSL for building codecs that can serialize
     * and deserialize configuration objects using Hytale's {@code BuilderCodec} system.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * CodecBuilder<MyConfig> builder = api.newCodec(MyConfig.class);
     * 
     * BuilderCodec<MyConfig> codec = builder
     *     .field("hostname", Codec.STRING, MyConfig::setHostname, MyConfig::getHostname)
     *         .withDefault("localhost")
     *     .and()
     *     .field("port", Codec.INTEGER, MyConfig::setPort, MyConfig::getPort)
     *         .withDefault(3306)
     *     .build();
     * }</pre>
     * 
     * <h3>Requirements</h3>
     * 
     * <p>The configuration class must:
     * <ul>
     *   <li>Have a public no-argument constructor</li>
     *   <li>Have getter and setter methods for all fields you want to serialize</li>
     * </ul>
     * 
     * <p>It's recommended to use Lombok's {@code @Getter} and {@code @Setter} annotations
     * to avoid boilerplate code.
     * 
     * @param <T>   the type of the configuration class
     * @param clazz the configuration class to build a codec for
     * @return a new {@code CodecBuilder} instance for building the codec
     * @throws NullPointerException if clazz is null
     * @throws IllegalArgumentException if clazz doesn't have a no-argument constructor
     * @see CodecBuilder
     * @see CodecBuilder#field(String, com.hypixel.hytale.codec.Codec, java.util.function.BiConsumer, java.util.function.Function)
     */
    <T> CodecBuilder<T> newCodec(Class<T> clazz);

}
