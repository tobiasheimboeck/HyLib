package dev.spacetivity.tobi.hylib.hytale.api.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Fluent DSL for building BuilderCodec instances. Created via {@link dev.spacetivity.tobi.hylib.hytale.api.HytaleApi#newCodec(Class)}.
 * Fields use key, codec, setter/getter; optional default via {@link FieldBuilder#withDefault(Object)}. Not thread-safe; result is.
 *
 * @param <T> the configuration type
 * @see dev.spacetivity.tobi.hylib.hytale.api.HytaleApi#newCodec(Class)
 * @see FieldBuilder
 * @since 1.0
 */
public interface CodecBuilder<T> {

    /**
     * Adds a field to the codec (key, codec, setter, getter).
     *
     * @param key    the config key name
     * @param codec  the codec for serialization/deserialization
     * @param setter setter on the config object
     * @param getter getter on the config object
     * @param <V>    the field value type
     * @return FieldBuilder for default value and chaining
     * @throws NullPointerException if any parameter is null
     * @see FieldBuilder#withDefault(Object)
     * @see FieldBuilder#and()
     */
    <V> FieldBuilder<T, V> field(String key, Codec<V> codec, BiConsumer<T, V> setter, Function<T, V> getter);

    /**
     * Builds the BuilderCodec. Do not use the builder after calling this.
     *
     * @return the built BuilderCodec
     * @throws IllegalStateException if no fields have been added
     */
    BuilderCodec<T> build();

    /**
     * Configures a single field (default value, then {@link #and()} or {@link #build()}).
     *
     * @param <T> the configuration type
     * @param <V> the field value type
     * @see CodecBuilder#field(String, Codec, BiConsumer, Function)
     * @since 1.0
     */
    interface FieldBuilder<T, V> {
        
        /**
         * Sets a default value when the field is missing or null.
         *
         * @param defaultValue the default value
         * @return this builder for chaining
         * @see #and()
         * @see #build()
         */
        FieldBuilder<T, V> withDefault(V defaultValue);

        /**
         * Finishes this field and returns the CodecBuilder to add more fields.
         *
         * @return the parent CodecBuilder
         * @see #build()
         */
        CodecBuilder<T> and();

        /**
         * Builds the BuilderCodec from the last field (convenience; equivalent to {@link #and()} then {@link CodecBuilder#build()}).
         *
         * @return the built BuilderCodec
         * @see CodecBuilder#build()
         */
        BuilderCodec<T> build();
    }
}
