package dev.spacetivity.tobi.hylib.hytale.api.player;

import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;

import java.util.UUID;

/**
 * Hytale player with UUID, username, and language preference.
 *
 * @see HyPlayerService
 * @since 1.0
 */
public interface HyPlayer {

    /**
     * Returns the player's UUID.
     *
     * @return the UUID, never null
     */
    UUID getUniqueId();

    /**
     * Returns the player's username.
     *
     * @return the username, never null
     */
    String getUsername();

    /**
     * Sets the username.
     *
     * @param username the new username
     * @throws NullPointerException if username is null
     */
    void setUsername(String username);

    /**
     * Returns the player's language preference (default lang if not set).
     *
     * @return the lang, never null
     */
    Lang getLanguage();

    /**
     * Sets the language preference.
     *
     * @param lang the lang
     * @throws NullPointerException if lang is null
     */
    void setLanguage(Lang lang);

}
