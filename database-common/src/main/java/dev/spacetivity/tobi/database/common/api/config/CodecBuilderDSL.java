package dev.spacetivity.tobi.database.common.api.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.spacetivity.tobi.database.api.config.ConfigGetter;
import dev.spacetivity.tobi.database.api.config.ConfigSetter;

public class CodecBuilderDSL<T> {

    private final BuilderCodec.Builder<T> builder;

    private CodecBuilderDSL(Class<T> clazz) {
        this.builder = BuilderCodec.builder(clazz, () -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> CodecBuilderDSL<T> of(Class<T> clazz) {
        return new CodecBuilderDSL<>(clazz);
    }

    public <V> CodecBuilderDSL<T> field(String key, Codec<V> codec, ConfigSetter<T, V> setter, ConfigGetter<T, V> getter) {
        builder.append(new KeyedCodec<>(key, codec), (obj, val, info) -> setter.set(obj, val), (obj, info) -> getter.get(obj)).add();
        return this;
    }

    public BuilderCodec<T> build() {
        return builder.build();
    }

}
