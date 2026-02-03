package dev.spacetivity.tobi.hylib.hytale.common.api.localization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Loads language files from JSON resources ({@code lang/{language}/*.json}). Keys from all files per language are merged.
 *
 * @since 1.0
 */
public class JsonLanguageLoader {

    private static final String LANG_DIRECTORY = "lang/";
    private static final String LANG_FILE_EXTENSION = ".json";
    private static final String DEFAULT_LANGUAGE = "en";

    private final ClassLoader classLoader;

    /**
     * Creates a JSON language loader.
     *
     * @param classLoader the class loader for resources
     */
    public JsonLanguageLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Loads all language files from {@code lang/{language}/*.json}; keys per language are merged.
     *
     * @return map of language code to translation map
     */
    public Map<String, Map<String, String>> loadAllLanguages() {
        Map<String, Map<String, String>> languages = new HashMap<>();
        Set<String> availableLanguages = discoverAvailableLanguages();
        
        for (String language : availableLanguages) {
            Map<String, String> translations = loadLanguage(language);
            if (translations != null && !translations.isEmpty()) {
                languages.put(language, translations);
            }
        }

        return languages;
    }

    /**
     * Discovers language codes that have at least one translation file.
     *
     * @return set of available language codes
     */
    public Set<String> discoverAvailableLanguages() {
        Set<String> languages = new HashSet<>();
        
        // Try common language codes
        String[] commonLanguages = {"en", "de", "fr", "es", "it", "pt", "ru", "zh", "ja", "ko"};
        
        for (String lang : commonLanguages) {
            if (hasLanguageFiles(lang)) {
                languages.add(lang);
            }
        }
        
        return languages;
    }

    /**
     * Returns whether the language has at least one JSON translation file.
     *
     * @param language the language code
     * @return true if at least one file exists
     */
    private boolean hasLanguageFiles(String language) {
        String langDir = LANG_DIRECTORY + language + "/";
        
        // Try to find any JSON file in the language directory
        // We check for common file names
        String[] commonFiles = {"player.json", "command.json", "error.json", "messages.json", "common.json"};
        
        for (String fileName : commonFiles) {
            String resourcePath = langDir + fileName;
            if (classLoader.getResource(resourcePath) != null) {
                return true;
            }
        }
        
        // Fallback: try old format lang/{language}.json for backwards compatibility
        String legacyPath = LANG_DIRECTORY + language + LANG_FILE_EXTENSION;
        return classLoader.getResource(legacyPath) != null;
    }

    /**
     * Loads all translation files for a language; keys are merged. Supports legacy {@code lang/{lang}.json}.
     *
     * @param language the language code (e.g. "en", "de")
     * @return map of key to translation, or null if no files exist
     */
    public Map<String, String> loadLanguage(String language) {
        Map<String, String> translations = new HashMap<>();
        String langDir = LANG_DIRECTORY + language + "/";
        
        // Try to load common JSON files in the language directory
        String[] commonFiles = {"player.json", "command.json", "error.json", "messages.json", "common.json"};
        boolean foundAny = false;
        
        for (String fileName : commonFiles) {
            String resourcePath = langDir + fileName;
            Map<String, String> fileTranslations = loadJsonFile(resourcePath);
            if (fileTranslations != null && !fileTranslations.isEmpty()) {
                translations.putAll(fileTranslations);
                foundAny = true;
            }
        }
        
        // If no files found in new format, try legacy format
        if (!foundAny) {
            Map<String, String> legacyTranslations = loadLanguageLegacy(language);
            if (legacyTranslations != null) {
                translations.putAll(legacyTranslations);
                foundAny = true;
            }
        }
        
        return foundAny ? translations : null;
    }

    /**
     * Loads a single JSON file from a resource path.
     *
     * @param resourcePath the resource path
     * @return map of key to translation, or null if loading fails
     */
    private Map<String, String> loadJsonFile(String resourcePath) {
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                
                if (!jsonElement.isJsonObject()) {
                    return null;
                }

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                Map<String, String> translations = new HashMap<>();

                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    String key = entry.getKey();
                    JsonElement value = entry.getValue();
                    
                    if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                        translations.put(key, value.getAsString());
                    }
                }

                return translations;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Loads language from legacy format {@code lang/{language}.json}.
     *
     * @param language the language code
     * @return map of key to translation, or null if file does not exist
     */
    private Map<String, String> loadLanguageLegacy(String language) {
        String resourcePath = LANG_DIRECTORY + language + LANG_FILE_EXTENSION;
        
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                
                if (!jsonElement.isJsonObject()) {
                    return null;
                }

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                Map<String, String> translations = new HashMap<>();

                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    String key = entry.getKey();
                    JsonElement value = entry.getValue();
                    
                    if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                        translations.put(key, value.getAsString());
                    }
                }

                return translations;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the default language code.
     *
     * @return the default language code
     */
    public String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

}
