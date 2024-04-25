package net.neptunsworld.elytra.database.api.repository.impl;

import net.neptunsworld.elytra.database.api.connection.DatabaseConnectionHandler;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnector;
import net.neptunsworld.elytra.database.api.connection.DatabaseType;
import net.neptunsworld.elytra.database.api.connection.credentials.DatabaseCredentials;
import net.neptunsworld.elytra.database.api.repository.Repository;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;

import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractRedissonMapRepository<K, V> implements Repository {

    private final RMapCache<K, V> dataMap;

    public AbstractRedissonMapRepository(DatabaseConnectionHandler db, String cacheName) {
        DatabaseConnector<RedissonClient, DatabaseCredentials> databaseConnector = db.getConnectorNullsafe(DatabaseType.REDIS);
        this.dataMap = databaseConnector.getSafeConnection().getMapCache(cacheName);
    }

    public void insert(K key, V value) {
        this.dataMap.put(key, value);
    }

    public void remove(K key) {
        this.dataMap.remove(key);
    }

    public void clear() {
        this.dataMap.clear();
    }

    public Duration getRemainingTimeToLife(K key) {
        return Duration.ofMillis(this.dataMap.remainTimeToLive(key));
    }

    public V getValue(K key) {
        return this.dataMap.get(key);
    }

    public void updateValue(K key, V newValue) {
        this.dataMap.replace(key, newValue);
    }

    public List<V> getValues() {
        return this.dataMap.values().stream().toList();
    }

    public List<K> getKeys() {
        return this.dataMap.keySet().stream().toList();
    }

    public void withAddedListener(BiConsumer<K, V> result) {
        this.dataMap.addListener((EntryCreatedListener<K, V>) event -> result.accept(event.getKey(), event.getValue()));
    }

    public void withRemovedListener(BiConsumer<K, V> result) {
        this.dataMap.addListener((EntryRemovedListener<K, V>) event -> result.accept(event.getKey(), event.getValue()));
    }

    public void withUpdatedListener(BiConsumer<K, V> result) {
        this.dataMap.addListener((EntryUpdatedListener<K, V>) event -> result.accept(event.getKey(), event.getValue()));
    }

    public RMapCache<K, V> getData() {
        return this.dataMap;
    }

}
