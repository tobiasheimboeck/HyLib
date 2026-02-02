package dev.spacetivity.tobi.hylib.hytale.api.localization;

import java.util.Set;

/**
 * Main API interface for localization and translation functionality.
 * 
 * <p>This interface provides access to localization features including:
 * <ul>
 *   <li>Translation of keys with placeholder support</li>
 *   <li>Language management and fallback handling</li>
 *   <li>Dynamic reloading of language files</li>
 * </ul>
 * 
 * <p>Instances of this interface should be obtained via {@link dev.spacetivity.tobi.hylib.hytale.api.HytaleApi#getLocalization()}
 * after initializing the localization in the Hytale API implementation.
 * 
 * <h3>Example Usage</h3>
 * 
 * <pre>{@code
 * // Get localization from HytaleApi
 * HytaleApi api = HytaleProvider.getApi();
 * Localization loc = api.getLocalization();
 * String message = loc.translate("player.welcome", "en", player.getName());
 * }</pre>
 * 
 * @see dev.spacetivity.tobi.hylib.hytale.api.HytaleApi
 * @since 1.0
 */
public interface Localization {

    /**
     * Translates a key to the specified language with optional placeholder arguments.
     * 
     * <p>This method translates the given key using the specified language. If the translation
     * is not found in the requested language, it falls back to the default language. If still
     * not found, the key itself is returned.
     * 
     * <p>Placeholders in the format {@code {0}}, {@code {1}}, etc. are replaced with the
     * corresponding arguments. Arguments are converted to strings using {@link String#valueOf(Object)}.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * // Translation: "Welcome {0}!"
     * String msg = loc.translate("player.welcome", "en", "PlayerName");
     * // Result: "Welcome PlayerName!"
     * }</pre>
     * 
     * @param key      the translation key (e.g., "player.welcome")
     * @param language the language code (e.g., "en", "de")
     * @param args     optional arguments for placeholder replacement
     * @return the translated string with placeholders replaced, or the key if translation not found
     * @throws NullPointerException if key or language is null
     */
    String translate(String key, String language, Object... args);

    /**
     * Translates a key using the default language with optional placeholder arguments.
     * 
     * <p>This is a convenience method that calls {@link #translate(String, String, Object...)}
     * with the default language.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * String msg = loc.translate("player.welcome", "PlayerName");
     * }</pre>
     * 
     * @param key  the translation key
     * @param args optional arguments for placeholder replacement
     * @return the translated string with placeholders replaced, or the key if translation not found
     * @throws NullPointerException if key is null
     * @see #translate(String, String, Object...)
     * @see #getDefaultLanguage()
     */
    String translate(String key, Object... args);

    /**
     * Gets the set of available language codes.
     * 
     * <p>The returned set contains all language codes for which translation files have been loaded.
     * The set is unmodifiable and may be empty if no language files have been loaded.
     * 
     * @return an unmodifiable set of available language codes, never null
     */
    Set<String> getAvailableLanguages();

    /**
     * Gets the default language code.
     * 
     * <p>The default language is used as a fallback when a translation is not found in the
     * requested language. Typically this is "en" (English).
     * 
     * @return the default language code, never null
     */
    String getDefaultLanguage();

    /**
     * Reloads all language files from the resources directory.
     * 
     * <p>This method scans the resources directory for language files and reloads them into
     * memory. This is useful for development or when language files are updated at runtime.
     * 
     * <p>Any errors during reloading are logged but do not prevent the method from completing.
     * Previously loaded translations remain available if reloading fails.
     */
    void reload();

}
