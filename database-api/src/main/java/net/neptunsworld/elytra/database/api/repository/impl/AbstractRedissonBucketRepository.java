package net.neptunsworld.elytra.database.api.repository.impl;

import net.neptunsworld.elytra.database.api.repository.Repository;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnectionHandler;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnector;
import net.neptunsworld.elytra.database.api.connection.DatabaseType;
import net.neptunsworld.elytra.database.api.connection.credentials.DatabaseCredentials;
import org.redisson.api.DeletedObjectListener;
import org.redisson.api.ExpiredObjectListener;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.SetObjectListener;

import java.time.Duration;
import java.util.function.Consumer;

public class AbstractRedissonBucketRepository<V> implements Repository {

    private final RBucket<V> dataBucket;

    public AbstractRedissonBucketRepository(DatabaseConnectionHandler db, String cacheName) {
        DatabaseConnector<RedissonClient, DatabaseCredentials> databaseConnector = db.getConnectorNullsafe(DatabaseType.REDIS);
        this.dataBucket = databaseConnector.getSafeConnection().getBucket(cacheName);
    }

    public void set(V value) {
        this.dataBucket.set(value);
    }

    public void setIfAbsent(V value) {
        this.dataBucket.setIfAbsent(value);
    }

    public void remove() {
        this.dataBucket.delete();
    }

    public Duration getRemainingTimeToLife() {
        return Duration.ofMillis(this.dataBucket.remainTimeToLive());
    }

    public V getValue() {
        return this.dataBucket.get();
    }

    public void withAddedListener(Consumer<V> result) {
        this.dataBucket.addListener((SetObjectListener) name -> {
            System.out.println("SET OBJECT WITH NAME >> " + name);
        });
    }

    public void withRemovedListener(Consumer<V> result) {
        this.dataBucket.addListener((DeletedObjectListener) name -> {
            System.out.println("DELETED OBJECT WITH NAME >> " + name);
        });
    }

    public void withUpdatedListener(Consumer<V> result) {
        this.dataBucket.addListener(new ExpiredObjectListener() {
            @Override
            public void onExpired(String name) {
                System.out.println("EXPIRED OBJECT WITH NAME >> " + name);

            }
        });
    }

    public RBucket<V> getData() {
        return this.dataBucket;
    }

}
