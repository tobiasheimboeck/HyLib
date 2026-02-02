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
import dev.spacetivity.tobi.hylib.hytale.plugin.localization.LocalComponent;
import dev.spacetivity.tobi.hylib.hytale.plugin.localization.LocalizationSystem;
import dev.spacetivity.tobi.hylib.hytale.plugin.player.PlayerListener;


public class DatabaseHytalePlugin extends JavaPlugin {

    private final DatabaseApiImpl dbApi;
    private final Config<DbConfig> dbConfig;
    private ComponentType<EntityStore, LocalComponent> localComponentType;

    public DatabaseHytalePlugin(JavaPluginInit init) {
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

        // Initialize HytaleApiImpl after database connection is established
        // This will register repositories, caches, and initialize services
        HytaleProvider.register(new HytaleApiImpl(getClass().getClassLoader()));

        // Register LocalComponent
        this.localComponentType = this.getEntityStoreRegistry()
                .registerComponent(LocalComponent.class, LocalComponent::new);
        LocalComponent.setComponentType(this.localComponentType);

        // Register LocalizationSystem to synchronize LocalComponent with HyPlayer
        this.getEntityStoreRegistry().registerSystem(new LocalizationSystem());

        // Register event listeners
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerListener::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerListener::onPlayerDisconnect);
    }

    /**
     * Gets the LocalComponent type for use in other parts of the plugin.
     * 
     * @return the LocalComponent type
     */
    public ComponentType<EntityStore, LocalComponent> getLocalComponentType() {
        return localComponentType;
    }

}
