package dev.spacetivity.tobi.database.common.api.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.spacetivity.tobi.database.api.config.CodecLoader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CodecLoaderImpl implements CodecLoader {

    private final ConcurrentMap<Class<?>, BuilderCodec<?>> codecCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> BuilderCodec<T> codec(Class<T> clazz) {
        return (BuilderCodec<T>) codecCache.computeIfAbsent(clazz, this::loadCodec);
    }

    @SuppressWarnings("unchecked")
    private <T> BuilderCodec<T> loadCodec(Class<T> clazz) {
        try {
            Class<?> generated = Class.forName(clazz.getName() + "_Codec");
            return (BuilderCodec<T>) generated.getField("CODEC").get(null);
        } catch (Exception e) {
            throw new IllegalStateException("No generated codec found for " + clazz.getName(), e);
        }
    }
}
