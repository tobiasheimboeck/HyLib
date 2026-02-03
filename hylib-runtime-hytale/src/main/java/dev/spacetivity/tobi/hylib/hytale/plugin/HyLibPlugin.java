package dev.spacetivity.tobi.hylib.hytale.plugin;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.api.connection.credentials.impl.MariaDbCredentials;
import dev.spacetivity.tobi.hylib.database.common.DatabaseApiImpl;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.common.HytaleApiImpl;
import dev.spacetivity.tobi.hylib.hytale.plugin.command.LanguageCommand;
import dev.spacetivity.tobi.hylib.hytale.plugin.config.DbConfig;
import dev.spacetivity.tobi.hylib.hytale.plugin.config.GlobalConfig;
import dev.spacetivity.tobi.hylib.hytale.plugin.player.PlayerListener;

public class HyLibPlugin extends JavaPlugin {

    private final DatabaseApiImpl dbApi;

    private final Config<DbConfig> dbConfig;
    private final Config<GlobalConfig> globalConfig;

    public HyLibPlugin(JavaPluginInit init) {
        super(init);

        this.dbApi = new DatabaseApiImpl();
        DatabaseProvider.register(this.dbApi);

        this.dbConfig = withConfig("DbConfig", DbConfig.CODEC);
        this.globalConfig = withConfig("GlobalConfig", GlobalConfig.CODEC);
    }

    @Override
    protected void setup() {
        super.setup();

        this.dbConfig.save();
        this.globalConfig.save();

        DbConfig config = dbConfig.get();
        MariaDbCredentials credentials = new MariaDbCredentials(config.getHostname(), config.getPort(), config.getUsername(), config.getDatabase(), config.getPassword());

        this.dbApi.establishConnection(credentials);

        HytaleProvider.register(new HytaleApiImpl(getClassLoader()));

        if (globalConfig.get().isLanguageCommandEnabled()) {
            getCommandRegistry().registerCommand(new LanguageCommand());
        }

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerListener::onPlayerReady);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerListener::onPlayerDisconnect);
    }

}
