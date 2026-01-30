package dev.spacetivity.tobi.hylib.hytale.api.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Fluent DSL interface for building codecs programmatically.
 * 
 * <p>Codecs are created via {@link dev.spacetivity.tobi.hylib.hytale.api.HytaleApi#newCodec(Class)} and provide a type-safe
 * way to serialize and deserialize configuration objects using Hytale's {@code BuilderCodec} system.
 * 
 * <p>This interface provides a fluent API for building codecs with method references,
 * allowing you to define how each field in your config class should be serialized/deserialized.
 * 
 * <h3>Example Usage</h3>
 * 
 * <pre>{@code
 * // Get CodecBuilder from HytaleApi
 * CodecBuilder<MyConfig> builder = HytaleProvider.getApi().newCodec(MyConfig.class);
 * 
 * // Build the codec
 * BuilderCodec<MyConfig> codec = builder
 *     .field("hostname", Codec.STRING, MyConfig::setHostname, MyConfig::getHostname)
 *         .withDefault("localhost")
 *     .and()
 *     .field("port", Codec.INTEGER, MyConfig::setPort, MyConfig::getPort)
 *         .withDefault(3306)
 *     .and()
 *     .field("api-key", Codec.STRING, MyConfig::setApiKey, MyConfig::getApiKey)
 *     .build();
 * }</pre>
 * 
 * <h3>Field Configuration</h3>
 * 
 * <p>Each field can be configured with:
 * <ul>
 *   <li>A config key name (e.g., "hostname", "max-players")</li>
 *   <li>A codec for serialization/deserialization (e.g., {@code Codec.STRING}, {@code Codec.INTEGER})</li>
 *   <li>Method references for getting and setting the value</li>
 *   <li>An optional default value using {@link FieldBuilder#withDefault(Object)}</li>
 * </ul>
 * 
 * <h3>Default Values</h3>
 * 
 * <p>Fields with default values will use the default if the field is missing or null in the config.
 * Fields without defaults are required and must be present in the config.
 * 
 * <h3>Thread Safety</h3>
 * 
 * <p>CodecBuilder instances are not thread-safe and should not be shared between threads.
 * However, the resulting {@code BuilderCodec} is thread-safe and can be reused.
 * 
 * @param <T> the type of the configuration object this codec builder is for
 * @see dev.spacetivity.tobi.hylib.hytale.api.HytaleApi#newCodec(Class)
 * @see FieldBuilder
 * @since 1.0
 */
public interface CodecBuilder<T> {

    /**
     * Adds a field to the codec.
     * 
     * <p>This method defines how a single field in your configuration class should be
     * serialized and deserialized. The field is identified by its config key name,
     * and uses the provided codec for type conversion.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * .field("hostname", Codec.STRING, MyConfig::setHostname, MyConfig::getHostname)
     * }</pre>
     * 
     * <p>This will:
     * <ul>
     *   <li>Read the "hostname" key from the config file</li>
     *   <li>Deserialize it as a String using {@code Codec.STRING}</li>
     *   <li>Set it on the config object using {@code MyConfig::setHostname}</li>
     *   <li>Serialize it back using {@code MyConfig::getHostname}</li>
     * </ul>
     * 
     * @param key    the key name in the config file/object (e.g., "hostname", "max-players")
     * @param codec  the codec to use for serialization/deserialization (e.g., {@code Codec.STRING}, {@code Codec.INTEGER})
     * @param setter method reference to set the value on the config object (e.g., {@code MyConfig::setHostname})
     * @param getter method reference to get the value from the config object (e.g., {@code MyConfig::getHostname})
     * @param <V>    the type of the field value
     * @return a {@link FieldBuilder} for configuring this field (e.g., setting default values)
     * @throws NullPointerException if any parameter is null
     * @see FieldBuilder#withDefault(Object)
     * @see FieldBuilder#and()
     */
    <V> FieldBuilder<T, V> field(String key, Codec<V> codec, BiConsumer<T, V> setter, Function<T, V> getter);

    /**
     * Builds the final {@code BuilderCodec}.
     * 
     * <p>This method finalizes the codec builder and returns a complete {@code BuilderCodec}
     * that can be used with Hytale's {@code Config<T>} system.
     * 
     * <p>After calling this method, the builder should not be used anymore.
     * 
     * @return the built {@code BuilderCodec} ready for use
     * @throws IllegalStateException if no fields have been added
     */
    BuilderCodec<T> build();

    /**
     * Builder for configuring a single field in the codec.
     * 
     * <p>This interface provides methods to configure a field after it has been added
     * via {@link CodecBuilder#field(String, Codec, BiConsumer, Function)}.
     * 
     * <h3>Usage Pattern</h3>
     * 
     * <pre>{@code
     * builder.field("hostname", Codec.STRING, MyConfig::setHostname, MyConfig::getHostname)
     *     .withDefault("localhost")  // Optional: set default value
     *     .and()                     // Continue to next field
     * }</pre>
     * 
     * <p>Or for the last field:
     * 
     * <pre>{@code
     * builder.field("port", Codec.INTEGER, MyConfig::setPort, MyConfig::getPort)
     *     .withDefault(3306)
     *     .build()  // Finish and build the codec
     * }</pre>
     * 
     * @param <T> the type of the configuration object
     * @param <V> the type of the field value
     * @see CodecBuilder#field(String, Codec, BiConsumer, Function)
     * @since 1.0
     */
    interface FieldBuilder<T, V> {
        
        /**
         * Sets a default value for this field.
         * 
         * <p>If the field is missing in the config file or its value is null,
         * this default value will be used instead.
         * 
         * <h3>Example</h3>
         * 
         * <pre>{@code
         * .field("port", Codec.INTEGER, MyConfig::setPort, MyConfig::getPort)
         *     .withDefault(3306)
         * }</pre>
         * 
         * <p>If the config file doesn't contain a "port" field, the value 3306 will be used.
         * 
         * <h3>Default Value Behavior</h3>
         * 
         * <ul>
         *   <li>If the config contains the field and it's not null, the config value is used</li>
         *   <li>If the config contains the field but it's null, the default value is used</li>
         *   <li>If the config doesn't contain the field, the default value is used</li>
         * </ul>
         * 
         * @param defaultValue the default value to use if the field is missing or null
         * @return this FieldBuilder for method chaining
         * @see #and()
         * @see #build()
         */
        FieldBuilder<T, V> withDefault(V defaultValue);

        /**
         * Returns to the parent {@code CodecBuilder} to add more fields.
         * 
         * <p>This method finishes the current field configuration and returns the parent
         * builder, allowing you to chain multiple field definitions.
         * 
         * <h3>Example</h3>
         * 
         * <pre>{@code
         * builder.field("hostname", Codec.STRING, MyConfig::setHostname, MyConfig::getHostname)
         *     .withDefault("localhost")
         *     .and()  // Finish this field, return to builder
         *     .field("port", Codec.INTEGER, MyConfig::setPort, MyConfig::getPort)
         *     .withDefault(3306)
         *     .and()  // Continue chaining...
         * }</pre>
         * 
         * @return the parent {@code CodecBuilder} for adding more fields
         * @see #build()
         */
        CodecBuilder<T> and();

        /**
         * Finishes this field and builds the final {@code BuilderCodec}.
         * 
         * <p>This is a convenience method that allows calling {@code build()} directly
         * on the last field without needing to call {@link #and()} first.
         * 
         * <h3>Example</h3>
         * 
         * <pre>{@code
         * BuilderCodec<MyConfig> codec = builder
         *     .field("hostname", Codec.STRING, MyConfig::setHostname, MyConfig::getHostname)
         *         .withDefault("localhost")
         *     .and()
         *     .field("port", Codec.INTEGER, MyConfig::setPort, MyConfig::getPort)
         *         .withDefault(3306)
         *     .build();  // Build directly from the last field
         * }</pre>
         * 
         * <p>This is equivalent to:
         * 
         * <pre>{@code
         * BuilderCodec<MyConfig> codec = builder
         *     .field("hostname", Codec.STRING, MyConfig::setHostname, MyConfig::getHostname)
         *         .withDefault("localhost")
         *     .and()
         *     .field("port", Codec.INTEGER, MyConfig::setPort, MyConfig::getPort)
         *         .withDefault(3306)
         *     .and()
         *     .build();  // Build from the builder
         * }</pre>
         * 
         * @return the built {@code BuilderCodec} ready for use
         * @see CodecBuilder#build()
         */
        BuilderCodec<T> build();
    }
}
