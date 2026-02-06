package dev.spacetivity.tobi.hylib.hytale.api.localization;

import com.hypixel.hytale.server.core.Message;
import dev.spacetivity.tobi.hymessage.api.placeholder.Placeholder;

import java.util.Set;

/**
 * API for localization and translation (LangKey, placeholders, formatted Message, fallback, reload).
 * Obtain via {@link dev.spacetivity.tobi.hylib.hytale.api.HytaleApi#getLocalizationService()}.
 *
 * @see dev.spacetivity.tobi.hylib.hytale.api.HytaleApi
 * @see LangKey
 * @since 1.0
 */
public interface LocalizationService {

    /**
     * Translates a key to the given language; replaces {@code {name}} placeholders and parses formatting tags to Message.
     *
     * @param key         the translation key
     * @param lang        the language
     * @param placeholders named placeholders
     * @return formatted Message
     * @throws NullPointerException if key or lang is null
     */
    Message translate(LangKey key, Lang lang, Placeholder... placeholders);

    /**
     * Translates a key using the default language and returns a formatted Message.
     *
     * @param key         the translation key
     * @param placeholders named placeholders
     * @return formatted Message
     * @throws NullPointerException if key is null
     * @see #translate(LangKey, Lang, Placeholder...)
     * @see #getDefaultLanguage()
     */
    Message translate(LangKey key, Placeholder... placeholders);

    /**
     * Returns the translated string for a key (placeholders replaced), without parsing to Message.
     *
     * @param key         the translation key
     * @param lang        the language
     * @param placeholders named placeholders
     * @return the translated string (or key if not found)
     * @throws NullPointerException if key or lang is null
     */
    String getRawTranslation(LangKey key, Lang lang, Placeholder... placeholders);

    /**
     * Returns true if a translation exists for the key in the given language (or default).
     *
     * @param key  the translation key
     * @param lang the language
     * @return true if the key exists
     * @throws NullPointerException if key or lang is null
     */
    boolean hasKey(LangKey key, Lang lang);

    /**
     * Returns true if the given language has loaded translation files.
     *
     * @param lang the language
     * @return true if the language is available
     * @throws NullPointerException if lang is null
     */
    boolean isLanguageAvailable(Lang lang);

    /**
     * Returns the set of available langs (those with loaded translation files).
     *
     * @return unmodifiable set of available langs, never null
     */
    Set<Lang> getAvailableLanguages();

    /**
     * Returns the default lang (fallback when translation is missing).
     *
     * @return the default lang, never null
     */
    Lang getDefaultLanguage();

    /**
     * Reloads all language files from the resources directory.
     * Errors are logged; existing translations remain if reload fails.
     */
    void reload();

    /**
     * Registers a plugin's language source. Files in {@code lang/{language}/*.json} are loaded and merged.
     * Later-registered sources override existing keys.
     *
     * @param classLoader the ClassLoader for the plugin's resources
     * @throws NullPointerException if classLoader is null
     * @since 1.0
     */
    void registerLanguageSource(ClassLoader classLoader);

}
