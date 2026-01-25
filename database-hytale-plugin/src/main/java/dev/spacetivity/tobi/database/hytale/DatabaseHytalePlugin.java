package dev.spacetivity.tobi.database.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import dev.spacetivity.tobi.database.api.DatabaseProvider;
import dev.spacetivity.tobi.database.api.connection.credentials.impl.MariaDbCredentials;
import dev.spacetivity.tobi.database.common.DatabaseApiImpl;
import dev.spacetivity.tobi.database.hytale.config.DbConfig;

public class DatabaseHytalePlugin extends JavaPlugin {

    private final DatabaseApiImpl dbApi;
    private final Config<DbConfig> dbConfig;

    public DatabaseHytalePlugin(JavaPluginInit init) {
        super(init);

        dbApi = new DatabaseApiImpl();
        DatabaseProvider.register(dbApi);

        dbConfig = withConfig("DbConfig", dbApi.getCodecLoader().codec(DbConfig.class));
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
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }

}
