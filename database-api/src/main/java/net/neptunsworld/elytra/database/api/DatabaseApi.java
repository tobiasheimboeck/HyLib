package net.neptunsworld.elytra.database.api;

import net.neptunsworld.elytra.database.api.cache.CacheLoader;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnectionHandler;
import net.neptunsworld.elytra.database.api.packet.RedissonPacketHandler;
import net.neptunsworld.elytra.database.api.repository.RepositoryLoader;

import java.util.concurrent.ExecutorService;

public interface DatabaseApi {

    ExecutorService getExecutorService();

    DatabaseConnectionHandler getDatabaseConnectionHandler();

    RedissonPacketHandler getPacketHandler();

    CacheLoader getCacheLoader();

    RepositoryLoader getRepositoryLoader();

}
