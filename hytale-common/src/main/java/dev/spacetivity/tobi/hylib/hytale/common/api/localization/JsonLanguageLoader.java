package dev.spacetivity.tobi.hylib.hytale.common.api.localization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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
     * Returns the ClassLoader used by this loader.
     *
     * @return the ClassLoader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
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
        
        // Try to find any JSON file in the language directory by scanning for all JSON files
        Set<String> jsonFiles = findJsonFilesInDirectory(langDir);
        if (!jsonFiles.isEmpty()) {
            return true;
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
        
        // Find and load all JSON files in the language directory
        Set<String> jsonFiles = findJsonFilesInDirectory(langDir);
        boolean foundAny = false;
        
        for (String fileName : jsonFiles) {
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
     * Finds all JSON files in a given resource directory.
     * Works with both file system and JAR resources.
     *
     * @param directoryPath the directory path (e.g. "lang/en/")
     * @return set of JSON file names found in the directory
     */
    private Set<String> findJsonFilesInDirectory(String directoryPath) {
        Set<String> jsonFiles = new HashSet<>();
        
        try {
            // Try to get the directory as a resource
            URL dirUrl = classLoader.getResource(directoryPath);
            if (dirUrl == null) {
                return jsonFiles;
            }
            
            URI dirUri = dirUrl.toURI();
            
            // Handle JAR files
            if (dirUri.getScheme().equals("jar")) {
                // Extract the JAR file path from the URI
                String jarPath = dirUri.getSchemeSpecificPart();
                int separatorIndex = jarPath.indexOf("!");
                if (separatorIndex > 0) {
                    URI jarUri = URI.create("jar:" + jarPath.substring(0, separatorIndex));
                    String pathInJar = jarPath.substring(separatorIndex + 1);
                    
                    // Try to get existing file system or create new one
                    FileSystem fileSystem = null;
                    boolean shouldClose = false;
                    try {
                        try {
                            fileSystem = FileSystems.getFileSystem(jarUri);
                        } catch (Exception e) {
                            // FileSystem doesn't exist, create a new one
                            fileSystem = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
                            shouldClose = true;
                        }
                        
                        Path dirPath = fileSystem.getPath(pathInJar);
                        if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
                            try (Stream<Path> paths = Files.list(dirPath)) {
                                paths.filter(Files::isRegularFile)
                                     .filter(path -> path.toString().endsWith(LANG_FILE_EXTENSION))
                                     .forEach(path -> {
                                         String fileName = path.getFileName().toString();
                                         jsonFiles.add(fileName);
                                     });
                            }
                        }
                    } finally {
                        if (shouldClose && fileSystem != null) {
                            fileSystem.close();
                        }
                    }
                }
            } else {
                // Handle file system resources
                Path dirPath = Paths.get(dirUri);
                if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
                    try (Stream<Path> paths = Files.list(dirPath)) {
                        paths.filter(Files::isRegularFile)
                             .filter(path -> path.toString().endsWith(LANG_FILE_EXTENSION))
                             .forEach(path -> {
                                 String fileName = path.getFileName().toString();
                                 jsonFiles.add(fileName);
                             });
                    }
                }
            }
        } catch (Exception e) {
            // If directory scanning fails, return empty set
            // This handles cases where the directory structure isn't directly accessible
        }
        
        return jsonFiles;
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
