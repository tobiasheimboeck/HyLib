package dev.spacetivity.tobi.hylib.hytale.common.api.localization;

import com.hypixel.hytale.server.core.Message;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LangKey;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LocalizationService;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Placeholder;
import dev.spacetivity.tobi.hylib.hytale.api.message.MessageParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link LocalizationService} (JSON language files, placeholders, fallback).
 *
 * @see LocalizationService
 * @since 1.0
 */
public class LocalizationServiceImpl implements LocalizationService {

    /** Matches named placeholders like {player}, {input}, {count}. */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");
    
    private final List<JsonLanguageLoader> loaders;
    private final Map<String, Map<String, String>> translations;
    private final Lang defaultLanguage;
    private final MessageParser messageParser;

    /**
     * Creates a localization service implementation.
     *
     * @param classLoader   the class loader for language files
     * @param messageParser the message parser for translating to Message
     */
    public LocalizationServiceImpl(ClassLoader classLoader, MessageParser messageParser) {
        this.messageParser = messageParser;
        this.loaders = new ArrayList<>();
        this.translations = new HashMap<>();
        
        // Register the initial class loader
        JsonLanguageLoader initialLoader = new JsonLanguageLoader(classLoader);
        
        this.loaders.add(initialLoader);
        loadAllLanguages();
        
        // Determine default language: use "en" if available, otherwise first available language
        Lang defaultLang = null;
        if (translations.containsKey("en")) {
            defaultLang = Lang.of("en");
        } else if (!translations.isEmpty()) {
            String firstLang = translations.keySet().iterator().next();
            defaultLang = Lang.of(firstLang);
        } else {
            // Fallback: create "en" even if no files exist
            defaultLang = Lang.of("en");
        }

        this.defaultLanguage = defaultLang;
    }

    @Override
    public Message translate(LangKey key, Lang lang, Placeholder... placeholders) {
        String translated = getTranslatedString(key, lang, placeholders);
        return messageParser.parse(translated);
    }

    @Override
    public Message translate(LangKey key, Placeholder... placeholders) {
        return translate(key, defaultLanguage, placeholders);
    }

    @Override
    public String getRawTranslation(LangKey key, Lang lang, Placeholder... placeholders) {
        return getTranslatedString(key, lang, placeholders);
    }

    @Override
    public boolean hasKey(LangKey key, Lang lang) {
        if (key == null || lang == null) {
            throw new NullPointerException("key and lang cannot be null");
        }
        if (getTranslation(key, lang.getCode()) != null) {
            return true;
        }
        if (!lang.equals(defaultLanguage)) {
            return getTranslation(key, defaultLanguage.getCode()) != null;
        }
        return false;
    }

    @Override
    public boolean isLanguageAvailable(Lang lang) {
        if (lang == null) {
            throw new NullPointerException("lang cannot be null");
        }
        return translations.containsKey(lang.getCode());
    }

    /**
     * Returns the translated string for a key (with placeholders replaced), without parsing to Message.
     */
    private String getTranslatedString(LangKey key, Lang lang, Placeholder... placeholders) {
        if (key == null) {
            throw new NullPointerException("Translation key cannot be null");
        }
        if (lang == null) {
            throw new NullPointerException("Lang cannot be null");
        }

        String languageCode = lang.getCode();
        String translation = getTranslation(key, languageCode);
        if (translation == null && !lang.equals(defaultLanguage)) {
            translation = getTranslation(key, defaultLanguage.getCode());
        }
        if (translation == null) {
            translation = key.getKey();
        }
        return replacePlaceholders(translation, placeholders);
    }

    @Override
    public Set<Lang> getAvailableLanguages() {
        Set<Lang> langs = new HashSet<>();
        for (String code : translations.keySet()) {
            langs.add(Lang.of(code));
        }
        return Collections.unmodifiableSet(langs);
    }

    @Override
    public Lang getDefaultLanguage() {
        return defaultLanguage;
    }

    @Override
    public void reload() {
        translations.clear();
        loadAllLanguages();
    }

    @Override
    public void registerLanguageSource(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("ClassLoader cannot be null");
        }
        
        JsonLanguageLoader loader = new JsonLanguageLoader(classLoader);
        this.loaders.add(loader);
        
        // Load and merge translations from the new source
        Map<String, Map<String, String>> newTranslations = loader.loadAllLanguages();
        mergeTranslations(newTranslations);
    }

    /**
     * Gets a translation for a specific key and language.
     *
     * @param key      the type-safe translation key
     * @param language the language code
     * @return the translation string, or null if not found
     */
    private String getTranslation(LangKey key, String language) {
        Map<String, String> langTranslations = translations.get(language);
        if (langTranslations == null) {
            return null;
        }
        return langTranslations.get(key.getKey());
    }

    /**
     * Replaces named placeholders in a translation string (e.g. {@code {player}}, {@code {input}}).
     * Values are taken from the given placeholders and converted via {@link String#valueOf(Object)}.
     */
    private String replacePlaceholders(String translation, Placeholder... placeholders) {
        if (placeholders == null || placeholders.length == 0) {
            return translation;
        }
        Map<String, String> byName = new HashMap<>();
        for (Placeholder p : placeholders) {
            if (p != null) {
                byName.put(p.name(), String.valueOf(p.value()));
            }
        }
        if (byName.isEmpty()) {
            return translation;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(translation);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String fullMatch = matcher.group(0);   // e.g. "{player}"
            String name = matcher.group(1);         // e.g. "player"
            String replacement = byName.getOrDefault(name, fullMatch);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Loads all available language files from all registered sources.
     */
    private void loadAllLanguages() {
        translations.clear();
        for (JsonLanguageLoader loader : loaders) {
            Map<String, Map<String, String>> loadedLanguages = loader.loadAllLanguages();
            mergeTranslations(loadedLanguages);
        }
    }

    /**
     * Merges translations from a source into the main translations map.
     *
     * <p>If a translation key already exists, the new value will overwrite the old one.
     * This allows plugins to override base translations if needed.
     *
     * @param newTranslations the translations to merge
     */
    private void mergeTranslations(Map<String, Map<String, String>> newTranslations) {
        for (Map.Entry<String, Map<String, String>> langEntry : newTranslations.entrySet()) {
            String language = langEntry.getKey();
            Map<String, String> langTranslations = langEntry.getValue();
            
            translations.computeIfAbsent(language, k -> new HashMap<>())
                    .putAll(langTranslations);
        }
    }

}
