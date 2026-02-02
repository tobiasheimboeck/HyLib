package dev.spacetivity.tobi.hylib.hytale.common;

import dev.spacetivity.tobi.hylib.database.api.DatabaseApi;
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.api.cache.CacheLoader;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnector;
import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseType;
import dev.spacetivity.tobi.hylib.database.api.connection.credentials.DatabaseCredentials;
import dev.spacetivity.tobi.hylib.database.api.repository.RepositoryLoader;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleApi;
import dev.spacetivity.tobi.hylib.hytale.api.config.CodecBuilder;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Localization;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;
import dev.spacetivity.tobi.hylib.hytale.common.api.config.CodecBuilderImpl;
import dev.spacetivity.tobi.hylib.hytale.common.api.localization.LocalizationImpl;
import dev.spacetivity.tobi.hylib.hytale.common.api.player.HyPlayerServiceImpl;
import dev.spacetivity.tobi.hylib.hytale.common.repository.player.HyPlayerRepository;
import dev.spacetivity.tobi.hylib.hytale.common.repository.player.cache.HyPlayerCache;
import lombok.SneakyThrows;

import java.sql.Connection;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Default implementation of {@link HytaleApi}.
 * 
 * <p>This implementation provides the runtime behavior for Hytale-specific functionality
 * including codec building, localization, and player management.
 * 
 * @see HytaleApi
 * @since 1.0
 */
public class HytaleApiImpl implements HytaleApi {

    private final Localization localization;
    private final HyPlayerService hyPlayerService;

    /**
     * Creates a new HytaleApiImpl instance.
     * 
     * <p>This constructor initializes all services including localization and player management.
     * It requires the Database API to be registered and a connection to be established before
     * this instance is created.
     * 
     * @param classLoader the class loader for loading language files
     * @throws IllegalStateException if Database API is not available or connection is not established
     */
    @SneakyThrows
    public HytaleApiImpl(ClassLoader classLoader) {
        this.localization = new LocalizationImpl(classLoader);

        DatabaseApi dbApi = DatabaseProvider.getApi();

        DatabaseConnectionHandler dbConnectionHandler = dbApi.getDatabaseConnectionHandler();
        DatabaseConnector<HikariDataSource, DatabaseCredentials> connector = dbConnectionHandler.getConnectorNullsafe(DatabaseType.MARIADB);
        Connection connection = connector.getSafeConnection().getConnection();

        RepositoryLoader repositoryLoader = dbApi.getRepositoryLoader();
        repositoryLoader.register(new HyPlayerRepository(dbConnectionHandler, connection));

        CacheLoader cacheLoader = dbApi.getCacheLoader();
        cacheLoader.register(new HyPlayerCache());

        this.hyPlayerService = new HyPlayerServiceImpl(repositoryLoader, cacheLoader);
    }

    @Override
    public <T> CodecBuilder<T> newCodec(Class<T> clazz) {
        return CodecBuilderImpl.of(clazz);
    }

    @Override
    public Localization getLocalization() {
        return this.localization;
    }

    @Override
    public HyPlayerService getHyPlayerService() {
        return this.hyPlayerService;
    }

}
