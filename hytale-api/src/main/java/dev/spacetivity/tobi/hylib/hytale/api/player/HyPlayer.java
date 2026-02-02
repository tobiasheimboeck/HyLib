package dev.spacetivity.tobi.hylib.hytale.api.player;

import java.util.UUID;

/**
 * Represents a Hytale player with their associated data.
 * 
 * <p>This interface provides access to player-specific information including
 * their unique identifier, username, and language preference.
 * 
 * <h3>Example Usage</h3>
 * 
 * <pre>{@code
 * HyPlayerService service = HytaleProvider.getApi().getHyPlayerService();
 * HyPlayer player = service.getOnlineHyPlayer(uuid);
 * String language = player.getLanguage();
 * }</pre>
 * 
 * @see HyPlayerService
 * @since 1.0
 */
public interface HyPlayer {

    /**
     * Gets the unique identifier of this player.
     * 
     * @return the player's UUID, never null
     */
    UUID getUniqueId();

    /**
     * Gets the username of this player.
     * 
     * @return the player's username, never null
     */
    String getUsername();

    /**
     * Sets the username of this player.
     * 
     * @param username the new username
     * @throws NullPointerException if username is null
     */
    void setUsername(String username);

    /**
     * Gets the language preference of this player.
     * 
     * <p>The language code follows ISO 639-1 format (e.g., "en", "de", "fr").
     * If no language is set, this should return the default language.
     * 
     * @return the language code (e.g., "en", "de"), never null
     */
    String getLanguage();

    /**
     * Sets the language preference of this player.
     * 
     * @param language the language code (e.g., "en", "de")
     * @throws NullPointerException if language is null
     */
    void setLanguage(String language);

}
