package dev.spacetivity.tobi.hylib.hytale.common.api.player;

import dev.spacetivity.tobi.hylib.database.api.DatabaseProvider;
import dev.spacetivity.tobi.hylib.database.api.cache.CacheLoader;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.UuidUtils;
import dev.spacetivity.tobi.hylib.database.api.repository.RepositoryLoader;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LanguageComponent;
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
        });
    }

    @Override
    public void setLanguageAndSync(UUID uniqueId, Lang lang, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (uniqueId == null) {
            throw new NullPointerException("uniqueId cannot be null");
        }
        if (lang == null) {
            throw new NullPointerException("Lang cannot be null");
        }
        if (ref == null) {
            throw new NullPointerException("Ref cannot be null");
        }
        if (store == null) {
            throw new NullPointerException("Store cannot be null");
        }
        changeLanguage(uniqueId, lang);
        syncLanguageComponent(ref, store, lang);
    }

    private void syncLanguageComponent(Ref<EntityStore> ref, Store<EntityStore> store, Lang lang) {
        try {
            LanguageComponent comp = store.getComponent(ref, LanguageComponent.getComponentType());
            if (comp == null) {
                store.addComponent(ref, LanguageComponent.getComponentType(), new LanguageComponent(lang));
            } else if (!comp.getLanguage().equals(lang)) {
                comp.setLanguage(lang);
                store.replaceComponent(ref, LanguageComponent.getComponentType(), comp);
            }
        } catch (IllegalStateException ignored) {
            // Component type not registered yet
        }
    }

}
