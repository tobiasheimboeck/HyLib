package net.neptunsworld.elytra.database.api.cache;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class AbstractInMemoryCache<K, V> implements Cache {

    private final Map<K, V> dataMap = new HashMap<>();

    public void insert(K key, V value) {
        this.dataMap.put(key, value);
    }

    public void remove(K key) {
        this.dataMap.remove(key);
    }

    public V getValue(K key) {
        return this.dataMap.get(key);
    }

}
