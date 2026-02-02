package dev.spacetivity.tobi.hylib.hytale.common.api.localization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Loads language files from JSON resources.
 * 
 * <p>This class scans the resources directory for language files in the format
 * {@code lang/{language}.json} and loads them into memory. Language files should
 * contain flat key-value pairs where keys are translation keys and values are
 * translation strings with optional placeholders.
 * 
 * <h3>Example Language File</h3>
 * 
 * <pre>{@code
 * {
 *   "player.welcome": "Welcome {0}!",
 *   "player.goodbye": "Goodbye!",
 *   "error.not_found": "Player {0} not found"
 * }
 * }</pre>
 * 
 * @since 1.0
 */
public class JsonLanguageLoader {

    private static final String LANG_DIRECTORY = "lang/";
    private static final String LANG_FILE_EXTENSION = ".json";
    private static final String DEFAULT_LANGUAGE = "en";

    private final Gson gson;
    private final ClassLoader classLoader;

    /**
     * Creates a new JSON language loader.
     * 
     * @param classLoader the class loader to use for loading resources
     */
    public JsonLanguageLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.gson = new Gson();
    }

    /**
     * Loads all language files from the resources directory.
     * 
     * <p>This method scans for files matching the pattern {@code lang/*.json}
     * and loads them into a map where keys are language codes and values are
     * maps of translation keys to translation strings.
     * 
     * @return a map of language codes to their translation maps
     */
    public Map<String, Map<String, String>> loadAllLanguages() {
        Map<String, Map<String, String>> languages = new HashMap<>();

        // Try to load common language codes
        String[] commonLanguages = {"en", "de", "fr", "es", "it", "pt", "ru", "zh", "ja", "ko"};
        
        for (String lang : commonLanguages) {
            Map<String, String> translations = loadLanguage(lang);
            if (translations != null && !translations.isEmpty()) {
                languages.put(lang, translations);
            }
        }

        return languages;
    }

    /**
     * Loads a specific language file.
     * 
     * @param language the language code (e.g., "en", "de")
     * @return a map of translation keys to translation strings, or null if the file doesn't exist
     */
    public Map<String, String> loadLanguage(String language) {
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
            // Log error but don't fail - return null to indicate failure
            return null;
        }
    }

    /**
     * Gets the default language code.
     * 
     * @return the default language code
     */
    public String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

}
