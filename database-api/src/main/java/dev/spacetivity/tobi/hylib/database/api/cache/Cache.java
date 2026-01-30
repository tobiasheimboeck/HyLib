package dev.spacetivity.tobi.hylib.database.api.cache;

/**
 * Interface for in-memory cache implementations.
 * 
 * <p>A cache provides a simple key-value storage mechanism for frequently accessed data.
 * Caches are typically used to reduce database queries by storing recently accessed
 * data in memory.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * Cache<String, User> userCache = new MyUserCache();
 * 
 * // Insert a value
 * userCache.insert("user-123", user);
 * 
 * // Get a value
 * User user = userCache.getValue("user-123");
 * 
 * // Update an existing value
 * boolean updated = userCache.update("user-123", updatedUser);
 * 
 * // Remove a value
 * userCache.remove("user-123");
 * }</pre>
 * 
 * <h3>Implementation Classes</h3>
 * 
 * <p>For convenience, you can extend one of the provided abstract implementations:
 * <ul>
 *   <li>{@link AbstractInMemoryCache} - Simple HashMap-based cache (not thread-safe)</li>
 *   <li>{@link AbstractThreadSafeInMemoryCache} - ConcurrentHashMap-based cache (thread-safe)</li>
 * </ul>
 * 
 * <h3>Thread Safety</h3>
 * 
 * <p>This interface does not specify thread safety requirements. Implementations should
 * document their thread safety guarantees. For thread-safe implementations, use
 * {@link AbstractThreadSafeInMemoryCache}.
 * 
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @see AbstractInMemoryCache
 * @see AbstractThreadSafeInMemoryCache
 * @see CacheLoader
 * @since 1.0
 */
public interface Cache<K, V> {

    /**
     * Inserts a key-value pair into the cache.
     * 
     * <p>If the key already exists, the value will be replaced with the new value.
     * 
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @throws NullPointerException if key or value is null (implementation-dependent)
     */
    void insert(K key, V value);

    /**
     * Updates an existing key-value pair in the cache.
     * 
     * <p>This method only updates the value if the key already exists in the cache.
     * 
     * @param key   the key whose value is to be updated
     * @param value the new value to be associated with the specified key
     * @return {@code true} if the key existed and was updated, {@code false} if the key did not exist
     * @throws NullPointerException if key or value is null (implementation-dependent)
     */
    boolean update(K key, V value);

    /**
     * Removes the key-value pair for the specified key from the cache.
     * 
     * <p>If the key does not exist, this method does nothing.
     * 
     * @param key the key whose mapping is to be removed
     * @throws NullPointerException if key is null (implementation-dependent)
     */
    void remove(K key);

    /**
     * Returns the value associated with the specified key.
     * 
     * <p>If the key does not exist in the cache, this method returns {@code null}.
     * 
     * @param key the key whose associated value is to be returned
     * @return the value associated with the specified key, or {@code null} if no mapping exists
     * @throws NullPointerException if key is null (implementation-dependent)
     */
    V getValue(K key);

}
