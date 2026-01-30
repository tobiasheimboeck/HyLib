package dev.spacetivity.tobi.hylib.database.api.cache;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

/**
 * Abstract base class for thread-safe in-memory cache implementations using a {@link ConcurrentHashMap}.
 * 
 * <p>This class provides a thread-safe implementation of the {@link Cache} interface using
 * a {@code ConcurrentHashMap} for storage. It is safe to use in multi-threaded environments
 * without external synchronization.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * public class UserCache extends AbstractThreadSafeInMemoryCache<String, User> {
 *     // Optional: Add custom logic here
 * }
 * 
 * UserCache cache = new UserCache();
 * cache.insert("user-123", user);
 * User user = cache.getValue("user-123");
 * 
 * // Thread-safe atomic operations
 * cache.compute("user-123", (key, value) -> {
 *     return value != null ? value.updateLastSeen() : null;
 * });
 * }</pre>
 * 
 * <h3>Thread Safety</h3>
 * 
 * <p><strong>This implementation is thread-safe.</strong> All operations are atomic and
 * can be safely called from multiple threads concurrently. The underlying {@code ConcurrentHashMap}
 * provides lock-free reads and fine-grained locking for writes.
 * 
 * <h3>Additional Features</h3>
 * 
 * <p>This class provides an additional {@link #compute(Object, BiFunction)} method for
 * atomic compute operations, which is useful for thread-safe value updates.
 * 
 * <h3>Performance</h3>
 * 
 * <p>The internal storage uses a {@code ConcurrentHashMap}, which provides:
 * <ul>
 *   <li>O(1) average time complexity for all operations</li>
 *   <li>Lock-free reads for better performance in read-heavy scenarios</li>
 *   <li>Fine-grained locking for writes</li>
 * </ul>
 * 
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
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
