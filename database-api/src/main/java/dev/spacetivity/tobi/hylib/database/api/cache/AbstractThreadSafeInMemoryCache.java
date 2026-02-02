package dev.spacetivity.tobi.hylib.database.api.cache;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

/**
 * Thread-safe in-memory cache using {@link java.util.concurrent.ConcurrentHashMap}. Supports {@link #compute(Object, BiFunction)}.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @see Cache
 * @see AbstractInMemoryCache
 * @since 1.0
 */
@Getter
public abstract class AbstractThreadSafeInMemoryCache<K, V> implements Cache<K, V> {

    private final ConcurrentMap<K, V> dataMap = new ConcurrentHashMap<>();

    @Override
    public void insert(K key, V value) {
        this.dataMap.put(key, value);
    }

    @Override
    public boolean update(K key, V value) {
        return this.dataMap.replace(key, value) != null;
    }

    @Override
    public void remove(K key) {
        this.dataMap.remove(key);
    }

    @Override
    public V getValue(K key) {
        return this.dataMap.get(key);
    }

    /**
     * Atomically computes a new value for the specified key.
     * 
     * <p>This method provides a thread-safe way to update a cache value based on its
     * current value. The remapping function is applied atomically, ensuring that
     * concurrent updates are handled correctly.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * // Atomically increment a counter
     * cache.compute("counter", (key, value) -> {
     *     return value != null ? value + 1 : 1;
     * });
     * 
     * // Update user's last seen timestamp
     * cache.compute("user-123", (key, user) -> {
     *     if (user != null) {
     *         user.setLastSeen(Instant.now());
     *     }
     *     return user;
     * });
     * }</pre>
     * 
     * <p>If the remapping function returns {@code null}, the mapping is removed from the cache.
     * 
     * @param key                the key whose value is to be computed
     * @param remappingFunction  the function to compute a new value based on the current key and value
     * @return the new value associated with the specified key, or {@code null} if the mapping was removed
     * @throws NullPointerException if key or remappingFunction is null
     */
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.dataMap.compute(key, remappingFunction);
    }

}
