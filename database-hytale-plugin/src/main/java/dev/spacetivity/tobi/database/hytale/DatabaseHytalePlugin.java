package dev.spacetivity.tobi.database.hytale;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import dev.spacetivity.tobi.database.api.DatabaseProvider;
import dev.spacetivity.tobi.database.api.connection.credentials.impl.MariaDbCredentials;
import dev.spacetivity.tobi.database.common.DatabaseApiImpl;
import dev.spacetivity.tobi.database.hytale.command.TestDbCommand;
import dev.spacetivity.tobi.database.hytale.config.DbConfig;
import dev.spacetivity.tobi.database.hytale.repository.TestRepository;

public class DatabaseHytalePlugin extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private DatabaseApiImpl dbApi;
    private Config<DbConfig> dbConfig;

    @SuppressWarnings("null")
    public DatabaseHytalePlugin(JavaPluginInit init) {
        super(init);

        dbApi = new DatabaseApiImpl();
        DatabaseProvider.register(dbApi);

        BuilderCodec<DbConfig> codec = dbApi.getCodecLoader().codec(DbConfig.class);
        dbConfig = withConfig("DbConfig", codec);
    }

    @Override
    protected void setup() {
        super.setup();

        this.dbConfig.save();

        DbConfig config = dbConfig.get();
        MariaDbCredentials credentials = new MariaDbCredentials(
                config.getHostname(),
                config.getPort(),
                config.getDatabase(),
                config.getUsername(),
                config.getPassword());

        dbApi.establishConnection(credentials);

        TestRepository testRepository = new TestRepository(dbApi.getDatabaseConnectionHandler());
        DatabaseProvider.getApi().getRepositoryLoader().register(testRepository);

        getCommandRegistry().registerCommand(new TestDbCommand());
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }

    public DbConfig getDbConfig() {
        return this.dbConfig.get();
    }

}
