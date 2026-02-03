package dev.spacetivity.tobi.hylib.hytale.api.player;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service for HyPlayer data (online/offline, create/update/delete, language).
 *
 * @see HyPlayer
 * @since 1.0
 */
public interface HyPlayerService {

    /**
     * Gets all offline players from the database.
     * 
     * @return a CompletableFuture that completes with a list of all offline players
     */
    CompletableFuture<List<HyPlayer>> getOfflineHyPlayers();

    /**
     * Gets an offline player by their unique identifier.
     * 
     * @param uniqueId the player's UUID
     * @return a CompletableFuture that completes with the player, or null if not found
     */
    CompletableFuture<HyPlayer> getOfflineHyPlayer(UUID uniqueId);

    /**
     * Gets an offline player by their username.
     * 
     * @param username the player's username
     * @return a CompletableFuture that completes with the player, or null if not found
     */
    CompletableFuture<HyPlayer> getOfflineHyPlayer(String username);

    /**
     * Gets all currently online players.
     * 
     * @return a set of online players, never null
     */
    Set<HyPlayer> getOnlineHyPlayers();

    /**
     * Gets an online player by their unique identifier.
     *
     * @param uniqueId the player's UUID
     * @return the online player, or null if not online
     */
    HyPlayer getOnlineHyPlayer(UUID uniqueId);

    /**
     * Gets the online HyPlayer for the given player ref, or null if not online.
     *
     * @param playerRef the player ref
     * @return the online HyPlayer, or null if not online
     * @throws NullPointerException if playerRef is null
     */
    HyPlayer getOnlineHyPlayer(PlayerRef playerRef);

    /**
     * Gets an online player by their username.
     * 
     * @param username the player's username
     * @return the online player, or null if not online
     */
    HyPlayer getOnlineHyPlayer(String username);

    /**
     * Creates a new player in the database.
     * 
     * <p>This method creates a new player with the default language.
     * The player will be automatically cached if they are online.
     * 
     * @param uniqueId the player's UUID
     * @param username the player's username
     */
    void createHyPlayer(UUID uniqueId, String username);

    /**
     * Deletes a player from the database.
     * 
     * <p>This also removes the player from the cache if they are online.
     * 
     * @param uniqueId the player's UUID
     */
    void deleteHyPlayer(UUID uniqueId);

    /**
     * Loads a player from the database and caches them.
     * 
     * @param uniqueId the player's UUID
     * @param result   callback that receives the loaded player
     */
    void loadHyPlayer(UUID uniqueId, Consumer<HyPlayer> result);

    /**
     * Caches a player for fast access.
     * 
     * @param uniqueId the player's UUID
     * @param player   the player to cache
     */
    void cacheHyPlayer(UUID uniqueId, HyPlayer player);

    /**
     * Removes a player from the cache.
     * 
     * @param uniqueId the player's UUID
     */
    void removeCachedHyPlayer(UUID uniqueId);

    /**
     * Changes the username of a player.
     * 
     * @param uniqueId   the player's UUID
     * @param newUsername the new username
     */
    void changeUsername(UUID uniqueId, String newUsername);

    /**
     * Changes the language preference of a player.
     *
     * @param uniqueId the player's UUID
     * @param lang     the new lang
     * @throws NullPointerException if lang is null
     */
    void changeLanguage(UUID uniqueId, Lang lang);

}
