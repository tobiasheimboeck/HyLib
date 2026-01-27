package dev.spacetivity.tobi.database.api.cache;

import dev.spacetivity.tobi.database.api.registry.RegistryLoader;

public interface CacheLoader extends RegistryLoader<Cache<?, ?>> {

    /**
     * Gets a cache by its class, supporting subtypes.
     * @param clazz the cache class (can be a subtype of Cache)
     * @param <T> the cache type
     * @return the cache instance, or null if not found
     */
    <T extends Cache<?, ?>> T getCache(Class<T> clazz);

}
