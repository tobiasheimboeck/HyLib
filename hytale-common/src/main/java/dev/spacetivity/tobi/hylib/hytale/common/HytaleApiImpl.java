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
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LocalizationService;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;
import dev.spacetivity.tobi.hymessage.api.HyMessageProvider;
import dev.spacetivity.tobi.hymessage.api.message.HyMessageBuilder;
import dev.spacetivity.tobi.hymessage.api.message.MessageParser;
import dev.spacetivity.tobi.hylib.hytale.common.api.config.CodecBuilderImpl;
import dev.spacetivity.tobi.hylib.hytale.common.api.localization.LocalizationServiceImpl;
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
    private HyPlayerService hyPlayerService;
    private final MessageParser messageParser;

    /**
     * Creates HytaleApiImpl. Works with or without database connection.
     * If database is not available, HyPlayerService will be null.
     *
     * @param classLoader the class loader for language files
     * @param defaultLanguage optional default language, or null to auto-detect
     */
    @SneakyThrows
    public HytaleApiImpl(ClassLoader classLoader, Lang defaultLanguage) {
        // Get MessageParser from HyMessage
        this.messageParser = HyMessageProvider.getApi().getMessageParser();
        this.localizationService = new LocalizationServiceImpl(classLoader, messageParser, defaultLanguage);
        initializeDatabase();
    }
    
    /**
     * Creates HytaleApiImpl with a custom message parser configuration.
     * Works with or without database connection.
     * If database is not available, HyPlayerService will be null.
     *
     * @param classLoader the class loader for language files
     * @param defaultLanguage optional default language, or null to auto-detect
     * @param builder the builder for configuring the message parser
     * @throws NullPointerException if builder is null
     */
    @SneakyThrows
    public HytaleApiImpl(ClassLoader classLoader, Lang defaultLanguage, HyMessageBuilder builder) {
        if (builder == null) {
            throw new NullPointerException("Builder cannot be null");
        }
        // Create MessageParser from builder
        this.messageParser = HyMessageProvider.getApi().createMessageParser(builder);
        this.localizationService = new LocalizationServiceImpl(classLoader, messageParser, defaultLanguage);
        initializeDatabase();
    }
    
    @SneakyThrows
    private void initializeDatabase() {

        // Check if database is available and connected
        DatabaseApi dbApi = null;
        try {
            dbApi = DatabaseProvider.getApi();
        } catch (IllegalStateException e) {
            // Database API not registered
            this.hyPlayerService = null;
            return;
        }
        
        DatabaseConnectionHandler dbConnectionHandler = dbApi.getDatabaseConnectionHandler();
        if (dbConnectionHandler != null) {
            DatabaseConnector<HikariDataSource, DatabaseCredentials> connector = dbConnectionHandler.getConnectorNullsafe(DatabaseType.MARIADB);
            if (connector != null) {
                Connection connection = connector.getSafeConnection().getConnection();
                if (connection != null) {
                    RepositoryLoader repositoryLoader = dbApi.getRepositoryLoader();
                    repositoryLoader.register(new HyPlayerRepository(dbConnectionHandler, connection));

                    CacheLoader cacheLoader = dbApi.getCacheLoader();
                    cacheLoader.register(new HyPlayerCache());

                    this.hyPlayerService = new HyPlayerServiceImpl(repositoryLoader, cacheLoader);
                    return;
                }
            }
        }
        
        // Database not available or not connected
        this.hyPlayerService = null;
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
        return this.hyPlayerService; // May be null if database is not configured
    }

}
