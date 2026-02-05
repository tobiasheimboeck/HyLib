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
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import dev.spacetivity.tobi.hylib.hytale.common.HytaleApiImpl;
import dev.spacetivity.tobi.hylib.hytale.plugin.command.LanguageCommand;
import dev.spacetivity.tobi.hylib.hytale.plugin.command.FormattingTestCommand;
import dev.spacetivity.tobi.hylib.hytale.plugin.config.DbConfig;
import dev.spacetivity.tobi.hylib.hytale.plugin.config.LanguageConfig;
import dev.spacetivity.tobi.hylib.hytale.plugin.player.PlayerListener;

public class HyLibPlugin extends JavaPlugin {

    private DatabaseApiImpl dbApi;

    private final Config<DbConfig> dbConfig;
    private final Config<LanguageConfig> languageConfig;

    public HyLibPlugin(JavaPluginInit init) {
        super(init);

        this.dbConfig = withConfig("DbConfig", DbConfig.CODEC);
        this.languageConfig = withConfig("LanguageConfig", LanguageConfig.CODEC);
    }

    @Override
    protected void setup() {
        super.setup();

        this.dbConfig.save();
        this.languageConfig.save();

        DbConfig dbConfigValue = dbConfig.get();
        LanguageConfig languageConfigValue = languageConfig.get();

        // Initialize database only if enabled
        if (dbConfigValue.isEnabled()) {
            this.dbApi = new DatabaseApiImpl();
            DatabaseProvider.register(this.dbApi);

            MariaDbCredentials credentials = new MariaDbCredentials(
                    dbConfigValue.getHostname(),
                    dbConfigValue.getPort(),
                    dbConfigValue.getUsername(),
                    dbConfigValue.getDatabase(),
                    dbConfigValue.getPassword()
            );

            this.dbApi.establishConnection(credentials);
        }

        // Always initialize HytaleApi (works without database)
        Lang defaultLanguage = Lang.of(languageConfigValue.getDefaultLanguage());
        HytaleProvider.register(new HytaleApiImpl(getClassLoader(), defaultLanguage));

        if (dbConfigValue.isEnabled() && languageConfigValue.isLanguageCommandEnabled()) {
            getCommandRegistry().registerCommand(new LanguageCommand());
        }
        
        // Register formatting test command for screenshots
        getCommandRegistry().registerCommand(new FormattingTestCommand());

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerListener::onPlayerReady);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerListener::onPlayerDisconnect);
    }

}
