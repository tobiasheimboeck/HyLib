package dev.spacetivity.tobi.database.api.cache;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

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

    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.dataMap.compute(key, remappingFunction);
    }

}
