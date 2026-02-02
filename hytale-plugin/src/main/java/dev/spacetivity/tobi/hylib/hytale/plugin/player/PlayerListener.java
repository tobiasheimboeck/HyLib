package dev.spacetivity.tobi.hylib.hytale.plugin.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayer;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;
import dev.spacetivity.tobi.hylib.hytale.plugin.localization.LocalComponent;

import java.util.UUID;

public class PlayerListener {

    private static final HyPlayerService hyPlayerService = HytaleProvider.getApi().getHyPlayerService();

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        assert player.getWorld() != null;
        player.getWorld().execute(() -> {
            UUID uniqueId = getUniqueId(event.getPlayerRef());
            Ref<EntityStore> ref = event.getPlayerRef();
            Store<EntityStore> store = ref.getStore();

            hyPlayerService.loadHyPlayer(uniqueId, hyPlayer -> {
                String username = player.getDisplayName();

                if (hyPlayer == null) {
                    handleNewPlayer(uniqueId, username, ref, store);
                } else {
                    handleExistingPlayer(uniqueId, username, hyPlayer, ref, store);
                }
            });
        });
    }

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        UUID uniqueId = event.getPlayerRef().getUuid();
        hyPlayerService.removeCachedHyPlayer(uniqueId);
    }

    private static void handleNewPlayer(UUID uniqueId, String username, Ref<EntityStore> ref, Store<EntityStore> store) {
        hyPlayerService.createHyPlayer(uniqueId, username);
        LocalComponent localComponent = new LocalComponent("en");
        store.addComponent(ref, LocalComponent.getComponentType(), localComponent);
    }

    private static void handleExistingPlayer(UUID uniqueId, String username, HyPlayer hyPlayer, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (!username.equals(hyPlayer.getUsername())) {
            hyPlayerService.changeUsername(uniqueId, username);
        }
        
        synchronizeLocalComponent(hyPlayer.getLanguage(), ref, store);
    }

    private static void synchronizeLocalComponent(String language, Ref<EntityStore> ref, Store<EntityStore> store) {
        LocalComponent localComponent = store.getComponent(ref, LocalComponent.getComponentType());
        
        if (localComponent == null) {
            localComponent = new LocalComponent(language);
            store.addComponent(ref, LocalComponent.getComponentType(), localComponent);
        } else if (!localComponent.getLanguage().equals(language)) {
            localComponent.setLanguage(language);
            store.replaceComponent(ref, LocalComponent.getComponentType(), localComponent);
        }
    }

    private static UUID getUniqueId(Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

        assert uuidComponent != null;
        return uuidComponent.getUuid();
    }

}
