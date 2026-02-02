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
import dev.spacetivity.tobi.hylib.hytale.api.localization.LocalizationService;
import dev.spacetivity.tobi.hylib.hytale.api.message.MessageParser;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;
import dev.spacetivity.tobi.hylib.hytale.common.api.config.CodecBuilderImpl;
import dev.spacetivity.tobi.hylib.hytale.common.api.localization.LocalizationServiceImpl;
import dev.spacetivity.tobi.hylib.hytale.common.api.message.MessageParserImpl;
import dev.spacetivity.tobi.hylib.hytale.common.api.player.HyPlayerServiceImpl;
import dev.spacetivity.tobi.hylib.hytale.common.repository.player.HyPlayerRepository;
import dev.spacetivity.tobi.hylib.hytale.common.repository.player.cache.HyPlayerCache;
import lombok.SneakyThrows;

import java.sql.Connection;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Default implementation of {@link HytaleApi} (codecs, localization, players).
 *
 * @see HytaleApi
 * @since 1.0
 */
public class HytaleApiImpl implements HytaleApi {

    private final LocalizationService localizationService;
    private final HyPlayerService hyPlayerService;
    private final MessageParser messageParser;

    /**
     * Creates HytaleApiImpl. Database API must be registered and connected.
     *
     * @param classLoader the class loader for language files
     * @throws IllegalStateException if Database API is not available or not connected
     */
    @SneakyThrows
    public HytaleApiImpl(ClassLoader classLoader) {
        this.messageParser = new MessageParserImpl();
        this.localizationService = new LocalizationServiceImpl(classLoader, messageParser);

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
    public LocalizationService getLocalizationService() {
        return this.localizationService;
    }

    @Override
    public HyPlayerService getHyPlayerService() {
        return this.hyPlayerService;
    }

    @Override
    public MessageParser getMessageParser() {
        return this.messageParser;
    }

}
