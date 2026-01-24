package dev.spacetivity.tobi.database.api.cache;

public interface Cache<K, V> {

    void insert(K key, V value);

    boolean update(K key, V value);

    void remove(K key);

    V getValue(K key);

}
