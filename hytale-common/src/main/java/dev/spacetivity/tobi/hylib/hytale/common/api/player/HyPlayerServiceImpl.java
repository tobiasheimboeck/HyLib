package dev.spacetivity.tobi.hylib.hytale.common.api.player;

import com.hypixel.hytale.event.EventBus;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.api.cache.CacheLoader;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.UuidUtils;
import dev.spacetivity.tobi.hylib.database.api.repository.RepositoryLoader;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.spacetivity.tobi.hylib.hytale.api.event.events.LanguageChangeEvent;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayer;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;
import dev.spacetivity.tobi.hylib.hytale.common.repository.player.HyPlayerRepository;
import dev.spacetivity.tobi.hylib.hytale.common.repository.player.cache.HyPlayerCache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class HyPlayerServiceImpl implements HyPlayerService {

    private final HyPlayerRepository hyPlayerRepository;
    private final HyPlayerCache hyPlayerCache;

    public HyPlayerServiceImpl(RepositoryLoader repositoryLoader, CacheLoader cacheLoader) {
        this.hyPlayerRepository = repositoryLoader.getRepository(HyPlayerRepository.class);
        this.hyPlayerCache = cacheLoader.getCache(HyPlayerCache.class);
    }

    @Override
    public CompletableFuture<List<HyPlayer>> getOfflineHyPlayers() {
        return this.hyPlayerRepository.getAllAsync();
    }

    @Override
    public CompletableFuture<HyPlayer> getOfflineHyPlayer(UUID uniqueId) {
        byte[] uuidBytes = UuidUtils.uuidToBytes(uniqueId);
        return this.hyPlayerRepository.getAsync(HyPlayerRepository.PLAYER_ID_COL, uuidBytes);
    }

    @Override
    public CompletableFuture<HyPlayer> getOfflineHyPlayer(String username) {
        return this.hyPlayerRepository.getAsync(HyPlayerRepository.PLAYER_NAME_COL, username);
    }

    @Override
    public Set<HyPlayer> getOnlineHyPlayers() {
        return new HashSet<>(this.hyPlayerCache.getDataMap().values());
    }

    @Override
    public HyPlayer getOnlineHyPlayer(UUID uniqueId) {
        return this.hyPlayerCache.getValue(uniqueId);
    }

    @Override
    public HyPlayer getOnlineHyPlayer(PlayerRef playerRef) {
        if (playerRef == null) {
            throw new NullPointerException("PlayerRef cannot be null");
        }
        return getOnlineHyPlayer(playerRef.getUuid());
    }

    @Override
    public HyPlayer getOnlineHyPlayer(String username) {
        return this.hyPlayerCache.getDataMap().values().stream()
                .filter(p -> p.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void createHyPlayer(UUID uniqueId, String username) {
        DatabaseProvider.getApi().execute(() -> {
            HyPlayer hyPlayer = new HyPlayerImpl(uniqueId, username);
            this.hyPlayerRepository.insert(hyPlayer);
            cacheHyPlayer(uniqueId, hyPlayer);
        });
    }

    @Override
    public void deleteHyPlayer(UUID uniqueId) {
        DatabaseProvider.getApi().execute(() -> {
            byte[] uuidBytes = UuidUtils.uuidToBytes(uniqueId);
            this.hyPlayerRepository.delete(HyPlayerRepository.PLAYER_ID_COL, uuidBytes);
            removeCachedHyPlayer(uniqueId);
        });
    }

    @Override
    public void loadHyPlayer(UUID uniqueId, Consumer<HyPlayer> result) {
        this.hyPlayerRepository.getAsync(HyPlayerRepository.PLAYER_ID_COL, UuidUtils.uuidToBytes(uniqueId)).thenAccept(hyPlayer -> {
            if (hyPlayer != null) {
                cacheHyPlayer(uniqueId, hyPlayer);
            }
            result.accept(hyPlayer);
        });
    }

    @Override
    public void cacheHyPlayer(UUID uniqueId, HyPlayer player) {
        this.hyPlayerCache.insert(uniqueId, player);
    }

    @Override
    public void removeCachedHyPlayer(UUID uniqueId) {
        this.hyPlayerCache.remove(uniqueId);
    }

    @Override
    public void changeUsername(UUID uniqueId, String newUsername) {
        DatabaseProvider.getApi().execute(() -> {
            this.hyPlayerRepository.changeUsername(uniqueId, newUsername);

            HyPlayer hyPlayer = getOnlineHyPlayer(uniqueId);
            if (hyPlayer == null) return;

            hyPlayer.setUsername(newUsername);
        });
    }

    @Override
    public void changeLanguage(UUID uniqueId, Lang lang) {
        DatabaseProvider.getApi().execute(() -> {
            this.hyPlayerRepository.changeLanguage(uniqueId, lang);

            HyPlayer hyPlayer = getOnlineHyPlayer(uniqueId);
            if (hyPlayer == null) return;

            hyPlayer.setLanguage(lang);

            EventBus eventBus = HytaleServer.get().getEventBus();
            IEventDispatcher<LanguageChangeEvent, LanguageChangeEvent> dispatcher = eventBus.dispatchFor(LanguageChangeEvent.class);
            if (dispatcher.hasListener()) {
                PlayerRef playerRef = Universe.get().getPlayer(uniqueId);
                if (playerRef == null) return;

                LanguageChangeEvent event = new LanguageChangeEvent(playerRef, lang);
                dispatcher.dispatch(event);
            }
        });
    }


}
