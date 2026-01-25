package dev.spacetivity.tobi.database.api.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public interface CodecLoader {

    <T> BuilderCodec<T> codec(Class<T> clazz);

}
