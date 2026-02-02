package dev.spacetivity.tobi.hylib.hytale.api;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LangKey;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LanguageComponent;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LocalizationService;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Placeholder;
import dev.spacetivity.tobi.hylib.hytale.api.message.MessageParser;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayer;

import java.util.Set;

/**
 * Static facade for message and localization; delegates to
 * {@link HytaleProvider#getApi()}.
 *
 * @see HytaleProvider
 * @see LocalizationService
 * @see MessageParser
 * @since 1.0
 */
public final class HyMessages {

    private HyMessages() {
    }

    /**
     * Translates a key to the given language and returns a formatted Message.
     *
     * @param key          the translation key
     * @param lang         the language
     * @param placeholders named placeholders
     * @return formatted Message
     * @throws NullPointerException if key or lang is null
     */
    public static Message translate(LangKey key, Lang lang, Placeholder... placeholders) {
        return HytaleProvider.getApi().getLocalizationService().translate(key, lang, placeholders);
    }

    /**
     * Translates a key using the player ref's language (from
     * {@link LanguageComponent}) and returns a formatted Message.
     *
     * @param key          the translation key
     * @param playerRef    the player ref (has ref and store)
     * @param placeholders named placeholders
     * @return formatted Message
     * @throws NullPointerException if key or playerRef is null
     */
    public static Message translate(LangKey key, PlayerRef playerRef, Placeholder... placeholders) {
        return translate(key, getLanguage(playerRef), placeholders);
    }

    /**
     * Translates a key using the player ref's language and returns a formatted
     * Message.
     * Overload for {@link Ref}{@code <EntityStore>} when PlayerRef is not
     * available.
     *
     * @param key          the translation key
     * @param ref          the player entity ref
     * @param placeholders named placeholders
     * @return formatted Message
     * @throws NullPointerException if key or ref is null
     */
    public static Message translate(LangKey key, Ref<EntityStore> ref, Placeholder... placeholders) {
        return translate(key, getLanguage(ref, ref.getStore()), placeholders);
    }

    /**
     * Translates a key using the default language and returns a formatted Message.
     *
     * @param key          the translation key
     * @param placeholders named placeholders
     * @return formatted Message
     * @throws NullPointerException if key is null
     */
    public static Message translate(LangKey key, Placeholder... placeholders) {
        return HytaleProvider.getApi().getLocalizationService().translate(key, placeholders);
    }

    /**
     * Translates a key using the player's language (from {@link LanguageComponent} via
     * {@code player.getReference()}) and sends the formatted Message to the player.
     *
     * @param player       the player entity
     * @param key          the translation key
     * @param placeholders named placeholders
     * @throws NullPointerException if player, key, reference or message is null
     */
    public static void sendTranslated(Player player, LangKey key, Placeholder... placeholders) {
        Ref<EntityStore> reference = player.getReference();

        if (reference == null)
            throw new NullPointerException("Reference cannot be null");

        Store<EntityStore> store = reference.getStore();
        Lang language = getLanguage(reference, store);

        Message message = translate(key, language, placeholders);
        
        if (message == null)
            throw new NullPointerException("Message cannot be null");

        player.sendMessage(message);
    }

    /**
     * Parses a formatted string into a Hytale Message.
     *
     * @param text string with tags (e.g. {@code <red>},
     *             {@code <gradient:red:blue>})
     * @return parsed Message
     * @throws NullPointerException if text is null
     */
    public static Message parse(String text) {
        return HytaleProvider.getApi().getMessageParser().parse(text);
    }

    /**
     * Returns the language of the given HyPlayer.
     *
     * @param player the HyPlayer
     * @return the player's language, never null
     * @throws NullPointerException if player is null
     */
    public static Lang getLanguage(HyPlayer player) {
        if (player == null) {
            throw new NullPointerException("Player cannot be null");
        }
        return player.getLanguage();
    }

    /**
     * Returns the language for the player ref (from online HyPlayer or default).
     *
     * @param playerRef the player ref
     * @return the player's language or default, never null
     * @throws NullPointerException if playerRef is null
     */
    public static Lang getLanguage(PlayerRef playerRef) {
        if (playerRef == null) {
            throw new NullPointerException("PlayerRef cannot be null");
        }
        HyPlayer hyPlayer = getHyPlayer(playerRef);
        return hyPlayer != null ? hyPlayer.getLanguage() : getDefaultLanguage();
    }

    /**
     * Returns the language for the player entity ref. Overload for
     * {@link Ref}{@code <EntityStore>}.
     *
     * @param ref the player entity ref
     * @return the player's language or default, never null
     * @throws NullPointerException if ref is null
     */
    public static Lang getLanguage(Ref<EntityStore> ref) {
        if (ref == null) {
            throw new NullPointerException("Ref cannot be null");
        }
        return getLanguage(ref, ref.getStore());
    }

    /**
     * Returns the online HyPlayer for the given player ref, or null if not online.
     *
     * @param playerRef the player ref
     * @return the HyPlayer, or null if not online
     * @throws NullPointerException if playerRef is null
     */
    public static HyPlayer getHyPlayer(PlayerRef playerRef) {
        if (playerRef == null) {
            throw new NullPointerException("PlayerRef cannot be null");
        }
        return HytaleProvider.getApi().getHyPlayerService().getOnlineHyPlayer(playerRef.getUuid());
    }

    /**
     * Returns the language for the player entity (from {@link LanguageComponent}),
     * or default if missing.
     *
     * @param ref   the player entity ref
     * @param store the entity store
     * @return the player's language or default, never null
     * @throws NullPointerException if ref or store is null
     */
    public static Lang getLanguage(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null) {
            throw new NullPointerException("Ref cannot be null");
        }
        if (store == null) {
            throw new NullPointerException("Store cannot be null");
        }
        try {
            LanguageComponent languageComponent = store.getComponent(ref, LanguageComponent.getComponentType());
            return languageComponent != null ? languageComponent.getLanguage() : getDefaultLanguage();
        } catch (IllegalStateException e) {
            return getDefaultLanguage();
        }
    }

    /**
     * Returns the translated string for a key (placeholders replaced), without
     * parsing to Message.
     *
     * @param key          the translation key
     * @param lang         the language
     * @param placeholders named placeholders
     * @return the translated string (or key if not found)
     * @throws NullPointerException if key or lang is null
     */
    public static String getRawTranslation(LangKey key, Lang lang, Placeholder... placeholders) {
        return HytaleProvider.getApi().getLocalizationService().getRawTranslation(key, lang, placeholders);
    }

    /**
     * Returns the default language.
     *
     * @return default lang, never null
     */
    public static Lang getDefaultLanguage() {
        return HytaleProvider.getApi().getLocalizationService().getDefaultLanguage();
    }

    /**
     * Returns the set of available languages.
     *
     * @return unmodifiable set of available langs, never null
     */
    public static Set<Lang> getAvailableLanguages() {
        return HytaleProvider.getApi().getLocalizationService().getAvailableLanguages();
    }
}
