package dev.spacetivity.tobi.hylib.database.api.cache;

import dev.spacetivity.tobi.hylib.database.api.registry.RegistryLoader;

/**
 * Registry and loader for {@link Cache} instances. Extends {@link RegistryLoader}.
 *
 * @see Cache
 * @see RegistryLoader
 * @see dev.spacetivity.tobi.hylib.database.api.DatabaseApi#getCacheLoader()
 * @since 1.0
 */
public interface CacheLoader extends RegistryLoader<Cache<?, ?>> {

    /**
     * Returns a cache instance by class; supports subtype matching.
     *
     * @param <T>   the cache type
     * @param clazz the cache class (or supertype)
     * @return the cache, or null if none found
     * @throws NullPointerException if clazz is null
     */
    <T extends Cache<?, ?>> T getCache(Class<T> clazz);

}
