package dev.spacetivity.tobi.hylib.database.api.cache;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap-based in-memory cache. Not thread-safe; use {@link AbstractThreadSafeInMemoryCache} for concurrency.
 *
 * @param <K> the key type
 * @param <V> the value type
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
