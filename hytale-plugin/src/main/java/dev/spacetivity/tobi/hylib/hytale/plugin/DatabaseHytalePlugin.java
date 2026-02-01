package dev.spacetivity.tobi.hylib.hytale.plugin;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.api.connection.credentials.impl.MariaDbCredentials;
import dev.spacetivity.tobi.hylib.database.common.DatabaseApiImpl;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.common.HytaleApiImpl;
import dev.spacetivity.tobi.hylib.hytale.plugin.config.DbConfig;

public class DatabaseHytalePlugin extends JavaPlugin {

    private final DatabaseApiImpl dbApi;
    private final Config<DbConfig> dbConfig;

    public DatabaseHytalePlugin(JavaPluginInit init) {
        super(init);

        this.dbApi = new DatabaseApiImpl();
        DatabaseProvider.register(this.dbApi);

        HytaleApiImpl hytaleApi = new HytaleApiImpl();
        HytaleProvider.register(hytaleApi);

        this.dbConfig = withConfig("DbConfig", DbConfig.CODEC);
    }

    @Override
    protected void setup() {
        super.setup();

        this.dbConfig.save();

        DbConfig config = dbConfig.get();
        MariaDbCredentials credentials = new MariaDbCredentials(config.getHostname(), config.getPort(), config.getUsername(), config.getDatabase(), config.getPassword());

        this.dbApi.establishConnection(credentials);
    }

}
