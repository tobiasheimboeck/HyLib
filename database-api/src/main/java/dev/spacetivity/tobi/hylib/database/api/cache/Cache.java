package dev.spacetivity.tobi.hylib.database.api.cache;

/**
 * In-memory key-value cache. Extend {@link AbstractInMemoryCache} or {@link AbstractThreadSafeInMemoryCache}.
 * Thread safety is implementation-dependent.
 *
 * @param <K> the key type
 * @param <V> the value type
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
