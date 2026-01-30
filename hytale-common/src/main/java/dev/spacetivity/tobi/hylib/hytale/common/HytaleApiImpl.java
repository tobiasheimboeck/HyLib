package dev.spacetivity.tobi.hylib.hytale.common;

import dev.spacetivity.tobi.hylib.hytale.api.HytaleApi;
import dev.spacetivity.tobi.hylib.hytale.api.config.CodecBuilder;
import dev.spacetivity.tobi.hylib.hytale.common.api.config.CodecBuilderImpl;

/**
 * Default implementation of {@link HytaleApi}.
 * 
 * <p>This implementation provides the runtime behavior for Hytale-specific functionality
 * including codec building.
 * 
 * @see HytaleApi
 * @since 1.0
 */
public class HytaleApiImpl implements HytaleApi {

    @Override
    public <T> CodecBuilder<T> newCodec(Class<T> clazz) {
        return CodecBuilderImpl.of(clazz);
    }

}
