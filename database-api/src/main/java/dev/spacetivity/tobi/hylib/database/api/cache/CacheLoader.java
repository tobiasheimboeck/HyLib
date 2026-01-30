package dev.spacetivity.tobi.hylib.database.api.cache;

import dev.spacetivity.tobi.hylib.database.api.registry.RegistryLoader;

/**
 * Registry and loader for {@link Cache} instances.
 * 
 * <p>This interface extends {@link RegistryLoader} to provide cache-specific functionality
 * for registering and retrieving cache instances. Caches can be registered and then
 * retrieved by their class type.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * CacheLoader cacheLoader = DatabaseProvider.getApi().getCacheLoader();
 * 
 * // Register a cache
 * MyUserCache userCache = new MyUserCache();
 * cacheLoader.register(userCache);
 * 
 * // Retrieve a cache
 * MyUserCache retrieved = cacheLoader.getCache(MyUserCache.class);
 * }</pre>
 * 
 * <h3>Inherited Methods</h3>
 * 
 * <p>This interface inherits the following methods from {@link RegistryLoader}:
 * <ul>
 *   <li>{@link RegistryLoader#register(Object)} - Register a cache instance</li>
 *   <li>{@link RegistryLoader#register(Object, Class)} - Register a cache with a specific class</li>
 *   <li>{@link RegistryLoader#get(Class)} - Get a cache wrapped in Optional</li>
 *   <li>{@link RegistryLoader#getNullable(Class)} - Get a cache or null</li>
 * </ul>
 * 
 * @see Cache
 * @see RegistryLoader
 * @see DatabaseApi#getCacheLoader()
 * @since 1.0
 */
public interface CacheLoader extends RegistryLoader<Cache<?, ?>> {

    /**
     * Gets a cache instance by its class, supporting subtypes.
     * 
     * <p>This method searches for a cache that is an instance of the specified class.
     * It supports subtype matching, so you can retrieve a cache using a superclass or
     * interface type.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * // Register a specific cache implementation
     * MyUserCache userCache = new MyUserCache();
     * cacheLoader.register(userCache);
     * 
     * // Retrieve it using the exact class
     * MyUserCache retrieved = cacheLoader.getCache(MyUserCache.class);
     * 
     * // Or using a superclass/interface
     * AbstractInMemoryCache<?, ?> cache = cacheLoader.getCache(AbstractInMemoryCache.class);
     * }</pre>
     * 
     * @param <T>   the cache type (must extend Cache)
     * @param clazz the cache class to search for (can be a subtype of Cache)
     * @return the cache instance if found, or {@code null} if no matching cache exists
     * @throws NullPointerException if clazz is null
     */
    <T extends Cache<?, ?>> T getCache(Class<T> clazz);

}
