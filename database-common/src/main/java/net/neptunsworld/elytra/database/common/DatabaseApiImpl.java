package net.neptunsworld.elytra.database.common;

import lombok.Getter;
import net.neptunsworld.elytra.database.api.DatabaseApi;
import net.neptunsworld.elytra.database.api.cache.CacheLoader;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnectionHandler;
import net.neptunsworld.elytra.database.api.connection.credentials.impl.MariaDbCredentials;
import net.neptunsworld.elytra.database.api.connection.credentials.impl.RedisCredentials;
import net.neptunsworld.elytra.database.api.packet.RedissonPacketHandler;
import net.neptunsworld.elytra.database.api.packet.RedissonPacketReceiver;
import net.neptunsworld.elytra.database.api.repository.RepositoryLoader;
import net.neptunsworld.elytra.database.common.api.cache.CacheLoaderImpl;
import net.neptunsworld.elytra.database.common.api.connection.DatabaseConnectionHandlerImpl;
import net.neptunsworld.elytra.database.common.api.packet.RedissonPacketHandlerImpl;
import net.neptunsworld.elytra.database.common.api.repository.RepositoryLoaderImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class DatabaseApiImpl implements DatabaseApi {

    private final ExecutorService executorService;
    private final DatabaseConnectionHandler databaseConnectionHandler;
    private final RedissonPacketHandler packetHandler;
    private final CacheLoader cacheLoader;
    private final RepositoryLoader repositoryLoader;

    public DatabaseApiImpl(MariaDbCredentials mariaDbCredentials, RedisCredentials redisCredentials, RedissonPacketReceiver packetReceiver) {
        this.executorService = Executors.newCachedThreadPool();
        this.databaseConnectionHandler = new DatabaseConnectionHandlerImpl(mariaDbCredentials, redisCredentials);
        this.packetHandler = new RedissonPacketHandlerImpl(this.databaseConnectionHandler, packetReceiver);
        this.cacheLoader = new CacheLoaderImpl();
        this.repositoryLoader = new RepositoryLoaderImpl();
    }

}
