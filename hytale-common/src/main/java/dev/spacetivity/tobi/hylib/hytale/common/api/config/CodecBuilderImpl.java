package dev.spacetivity.tobi.hylib.hytale.common.api.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.spacetivity.tobi.hylib.hytale.api.config.CodecBuilder;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Default implementation of {@link CodecBuilder}.
 * 
 * <p>This implementation provides the runtime behavior for building codecs
 * using Hytale's {@code BuilderCodec} system.
 * 
 * <p>Instances are created via {@link #of(Class)} and should not be constructed directly.
 * 
 * @param <T> the type of the configuration object
 * @see CodecBuilder
 * @see dev.spacetivity.tobi.hylib.hytale.api.HytaleApi#newCodec(Class)
 * @since 1.0
 */
public class CodecBuilderImpl<T> implements CodecBuilder<T> {

    private final BuilderCodec.Builder<T> builder;
    private FieldBuilderImpl<T, ?> currentField;

    private CodecBuilderImpl(Class<T> clazz) {
        this.builder = BuilderCodec.builder(clazz, () -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
            }
        });
    }

    /**
     * Creates a new {@code CodecBuilder} instance for the specified class.
     * 
     * <p>This factory method creates a new builder that can be used to build
     * a codec for the given configuration class.
     * 
     * <p>The class must have a public no-argument constructor. If the constructor
     * cannot be accessed or instantiated, a {@code RuntimeException} will be thrown
     * when the codec is built.
     * 
     * @param <T>   the type of the configuration class
     * @param clazz the configuration class to build a codec for
     * @return a new {@code CodecBuilder} instance
     * @throws NullPointerException if clazz is null
     */
    public static <T> CodecBuilder<T> of(Class<T> clazz) {
        return new CodecBuilderImpl<>(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> CodecBuilder.FieldBuilder<T, V> field(String key, Codec<V> codec, BiConsumer<T, V> setter, Function<T, V> getter) {
        if (currentField != null) {
            currentField.finish();
        }
        currentField = new FieldBuilderImpl<>(this, key, codec, setter, getter);
        return (CodecBuilder.FieldBuilder<T, V>) currentField;
    }

    @Override
    public BuilderCodec<T> build() {
        if (currentField != null) {
            currentField.finish();
        }
        return builder.build();
    }

    <V> void appendField(KeyedCodec<V> keyedCodec, BiConsumer<T, V> setter, Function<T, V> getter) {
        builder.append(keyedCodec, (obj, val, info) -> setter.accept(obj, val), (obj, info) -> getter.apply(obj)).add();
    }

    private static class FieldBuilderImpl<T, V> implements CodecBuilder.FieldBuilder<T, V> {
        private final CodecBuilderImpl<T> parent;
        private final String key;
        private final Codec<V> codec;
        private final BiConsumer<T, V> setter;
        private final Function<T, V> getter;
        private V defaultValue;
        private boolean finished = false;

        FieldBuilderImpl(CodecBuilderImpl<T> parent, String key, Codec<V> codec, BiConsumer<T, V> setter, Function<T, V> getter) {
            this.parent = parent;
            this.key = key;
            this.codec = codec;
            this.setter = setter;
            this.getter = getter;
        }

        @Override
        public CodecBuilder.FieldBuilder<T, V> withDefault(V defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        public CodecBuilder<T> and() {
            finish();
            return parent;
        }

        @Override
        public BuilderCodec<T> build() {
            finish();
            return parent.build();
        }

        void finish() {
            if (finished) {
                return;
            }
            finished = true;

            KeyedCodec<V> keyedCodec = new KeyedCodec<>(key, codec);
            
            BiConsumer<T, V> finalSetter;
            if (defaultValue != null) {
                finalSetter = (obj, val) -> setter.accept(obj, val != null ? val : defaultValue);
            } else {
                finalSetter = setter;
            }

            parent.appendField(keyedCodec, finalSetter, getter);
        }
    }
}
