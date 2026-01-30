package dev.spacetivity.tobi.hylib.database.api.cache;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for simple in-memory cache implementations using a {@link HashMap}.
 * 
 * <p>This class provides a basic implementation of the {@link Cache} interface using
 * a standard {@code HashMap} for storage. It is <strong>not thread-safe</strong> and
 * should only be used in single-threaded contexts or when external synchronization
 * is provided.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * public class UserCache extends AbstractInMemoryCache<String, User> {
 *     // Optional: Add custom logic here
 * }
 * 
 * UserCache cache = new UserCache();
 * cache.insert("user-123", user);
 * User user = cache.getValue("user-123");
 * }</pre>
 * 
 * <h3>Thread Safety</h3>
 * 
 * <p><strong>This implementation is not thread-safe.</strong> For thread-safe caches,
 * use {@link AbstractThreadSafeInMemoryCache} instead.
 * 
 * <h3>Implementation Details</h3>
 * 
 * <p>The internal storage uses a {@code HashMap}, which provides O(1) average time
 * complexity for insert, update, remove, and get operations.
 * 
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @see Cache
 * @see AbstractThreadSafeInMemoryCache
 * @since 1.0
 */
@Getter
public abstract class AbstractInMemoryCache<K, V> implements Cache<K, V> {

    private final Map<K, V> dataMap = new HashMap<>();

    @Override
    public void insert(K key, V value) {
        this.dataMap.put(key, value);
    }

    @Override
    public boolean update(K key, V value) {
        if (!this.dataMap.containsKey(key)) {
            return false;
        }
        this.dataMap.put(key, value);
        return true;
    }

    @Override
    public void remove(K key) {
        this.dataMap.remove(key);
    }

    @Override
    public V getValue(K key) {
        return this.dataMap.get(key);
    }

}
