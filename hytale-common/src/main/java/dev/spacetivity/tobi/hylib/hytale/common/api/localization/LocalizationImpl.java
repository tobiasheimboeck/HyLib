package dev.spacetivity.tobi.hylib.hytale.common.api.localization;

import dev.spacetivity.tobi.hylib.hytale.api.localization.Localization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link Localization}.
 * 
 * <p>This implementation loads language files from JSON resources and provides
 * translation functionality with placeholder support and fallback mechanisms.
 * 
 * @see Localization
 * @since 1.0
 */
public class LocalizationImpl implements Localization {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\d+)\\}");
    
    private final JsonLanguageLoader loader;
    private final Map<String, Map<String, String>> translations;
    private final String defaultLanguage;

    /**
     * Creates a new localization implementation.
     * 
     * @param classLoader the class loader to use for loading language files
     */
    public LocalizationImpl(ClassLoader classLoader) {
        this.loader = new JsonLanguageLoader(classLoader);
        this.translations = new HashMap<>();
        this.defaultLanguage = loader.getDefaultLanguage();
        loadAllLanguages();
    }

    @Override
    public String translate(String key, String language, Object... args) {
        if (key == null) {
            throw new NullPointerException("Translation key cannot be null");
        }
        if (language == null) {
            throw new NullPointerException("Language cannot be null");
        }

        // Try requested language first
        String translation = getTranslation(key, language);
        
        // Fallback to default language if not found
        if (translation == null && !language.equals(defaultLanguage)) {
            translation = getTranslation(key, defaultLanguage);
        }
        
        // Fallback to key itself if still not found
        if (translation == null) {
            translation = key;
        }

        // Replace placeholders
        return replacePlaceholders(translation, args);
    }

    @Override
    public String translate(String key, Object... args) {
        return translate(key, defaultLanguage, args);
    }

    @Override
    public Set<String> getAvailableLanguages() {
        return Collections.unmodifiableSet(translations.keySet());
    }

    @Override
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    @Override
    public void reload() {
        translations.clear();
        loadAllLanguages();
    }

    /**
     * Gets a translation for a specific key and language.
     * 
     * @param key      the translation key
     * @param language the language code
     * @return the translation string, or null if not found
     */
    private String getTranslation(String key, String language) {
        Map<String, String> langTranslations = translations.get(language);
        if (langTranslations == null) {
            return null;
        }
        return langTranslations.get(key);
    }

    /**
     * Replaces placeholders in a translation string with provided arguments.
     * 
     * <p>Placeholders are in the format {@code {0}}, {@code {1}}, etc., where
     * the number corresponds to the index in the args array. Arguments are
     * converted to strings using {@link String#valueOf(Object)}.
     * 
     * @param translation the translation string with placeholders
     * @param args        the arguments to replace placeholders with
     * @return the string with placeholders replaced
     */
    private String replacePlaceholders(String translation, Object... args) {
        if (args == null || args.length == 0) {
            return translation;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(translation);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String placeholder = matcher.group(0); // e.g., "{0}"
            int index = Integer.parseInt(matcher.group(1)); // e.g., 0

            String replacement;
            if (index >= 0 && index < args.length) {
                replacement = String.valueOf(args[index]);
            } else {
                // Keep placeholder if index is out of bounds
                replacement = placeholder;
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Loads all available language files.
     */
    private void loadAllLanguages() {
        Map<String, Map<String, String>> loadedLanguages = loader.loadAllLanguages();
        translations.putAll(loadedLanguages);
    }

}
