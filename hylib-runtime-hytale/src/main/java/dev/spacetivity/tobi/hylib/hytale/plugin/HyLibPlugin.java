package dev.spacetivity.tobi.hylib.hytale.plugin;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.api.connection.credentials.impl.MariaDbCredentials;
import dev.spacetivity.tobi.hylib.database.common.DatabaseApiImpl;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.common.HytaleApiImpl;
import dev.spacetivity.tobi.hylib.hytale.plugin.config.DbConfig;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LanguageComponent;
import dev.spacetivity.tobi.hylib.hytale.plugin.localization.LocalizationSystem;
import dev.spacetivity.tobi.hylib.hytale.plugin.player.PlayerListener;

public class HyLibPlugin extends JavaPlugin {

    private final DatabaseApiImpl dbApi;
    private final Config<DbConfig> dbConfig;
    private ComponentType<EntityStore, LanguageComponent> languageComponentType;

    public HyLibPlugin(JavaPluginInit init) {
        super(init);

        this.dbApi = new DatabaseApiImpl();
        DatabaseProvider.register(this.dbApi);

        this.dbConfig = withConfig("DbConfig", DbConfig.CODEC);
    }

    @Override
    protected void setup() {
        super.setup();

        this.dbConfig.save();

        DbConfig config = dbConfig.get();
        MariaDbCredentials credentials = new MariaDbCredentials(config.getHostname(), config.getPort(), config.getUsername(), config.getDatabase(), config.getPassword());

        this.dbApi.establishConnection(credentials);

        HytaleProvider.register(new HytaleApiImpl(getClass().getClassLoader()));

        this.languageComponentType = this.getEntityStoreRegistry().registerComponent(LanguageComponent.class, LanguageComponent::new);
        LanguageComponent.setComponentType(this.languageComponentType);

        this.getEntityStoreRegistry().registerSystem(new LocalizationSystem());

        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerListener::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerListener::onPlayerDisconnect);
    }

    /**
     * Gets the LanguageComponent type for use in other parts of the plugin.
     * 
     * @return the LanguageComponent type
     */
    public ComponentType<EntityStore, LanguageComponent> getLanguageComponentType() {
        return languageComponentType;
    }

}
