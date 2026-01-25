package dev.spacetivity.tobi.database.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import dev.spacetivity.tobi.database.api.DatabaseApi;
import dev.spacetivity.tobi.database.api.DatabaseProvider;
import dev.spacetivity.tobi.database.api.connection.credentials.impl.MariaDbCredentials;
import dev.spacetivity.tobi.database.common.DatabaseApiImpl;
import dev.spacetivity.tobi.database.hytale.config.DbConfig;

public class DatabaseHytalePlugin extends JavaPlugin {

    private Config<DbConfig> dbConfig;

    public DatabaseHytalePlugin(JavaPluginInit init) {
        super(init);

        dbConfig = withConfig(DatabaseProvider.getApi().getCodecLoader().codec(DbConfig.class));
    }

    @Override
    protected void setup() {
        super.setup();

        DbConfig config = getDbConfig();
        MariaDbCredentials credentials = new MariaDbCredentials(
                config.getHostname(),
                config.getPort(),
                config.getDatabase(),
                config.getUsername(),
                config.getPassword()
        );

        DatabaseApi dbApi = new DatabaseApiImpl(credentials);
        DatabaseProvider.register(dbApi);
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }

    public DbConfig getDbConfig() {
        return this.dbConfig.get();
    }

}
