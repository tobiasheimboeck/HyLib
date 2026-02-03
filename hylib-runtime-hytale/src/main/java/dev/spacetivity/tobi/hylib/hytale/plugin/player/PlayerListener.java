package dev.spacetivity.tobi.hylib.hytale.plugin.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayer;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;

import java.util.UUID;

public class PlayerListener {

    private static final HyPlayerService hyPlayerService = HytaleProvider.getApi().getHyPlayerService();

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        assert player.getWorld() != null;
        World world = player.getWorld();
        
        world.execute(() -> {
            Ref<EntityStore> ref = event.getPlayerRef();
            UUID uniqueId = getUniqueId(ref);

            hyPlayerService.loadHyPlayer(uniqueId, hyPlayer -> {
                world.execute(() -> {
                    String username = player.getDisplayName();
                    if (hyPlayer == null) {
                        handleNewPlayer(uniqueId, username);
                    } else {
                        handleExistingPlayer(uniqueId, username, hyPlayer);
                    }
                });
            });
        });
    }

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        UUID uniqueId = event.getPlayerRef().getUuid();
        hyPlayerService.removeCachedHyPlayer(uniqueId);
    }

    private static void handleNewPlayer(UUID uniqueId, String username) {
        hyPlayerService.createHyPlayer(uniqueId, username);
    }

    private static void handleExistingPlayer(UUID uniqueId, String username, HyPlayer hyPlayer) {
        if (!username.equals(hyPlayer.getUsername())) {
            hyPlayerService.changeUsername(uniqueId, username);
        }
    }

    private static UUID getUniqueId(Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

        assert uuidComponent != null;
        return uuidComponent.getUuid();
    }

}
