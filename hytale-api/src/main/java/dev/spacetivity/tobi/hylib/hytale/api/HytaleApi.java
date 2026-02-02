package dev.spacetivity.tobi.hylib.hytale.api;

import dev.spacetivity.tobi.hylib.hytale.api.config.CodecBuilder;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LocalizationService;
import dev.spacetivity.tobi.hylib.hytale.api.message.MessageParser;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;

/**
 * Main API for Hytale-specific functionality (codecs, localization, players, messages).
 * Obtain via {@link HytaleProvider#getApi()} after {@link HytaleProvider#register(HytaleApi)}.
 *
 * @see HytaleProvider
 * @see CodecBuilder
 * @see LocalizationService
 * @since 1.0
 */
public interface HytaleApi {

    /**
     * Creates a new {@link CodecBuilder} for the given configuration class.
     * Config class must have a no-arg constructor and getters/setters for fields.
     *
     * @param <T>   the type of the configuration class
     * @param clazz the configuration class to build a codec for
     * @return a new CodecBuilder instance
     * @throws NullPointerException if clazz is null
     * @throws IllegalArgumentException if clazz has no no-argument constructor
     * @see CodecBuilder
     */
    <T> CodecBuilder<T> newCodec(Class<T> clazz);

    /**
     * Returns the localization service for translations (languages, placeholders, formatted Message).
     *
     * @return the localization service, never null
     * @throws IllegalStateException if localization has not been initialized
     * @see LocalizationService
     */
    LocalizationService getLocalizationService();

    /**
     * Returns the HyPlayer service for player data and language preferences.
     *
     * @return the HyPlayer service, never null
     * @throws IllegalStateException if the service has not been initialized
     * @see HyPlayerService
     */
    HyPlayerService getHyPlayerService();

    /**
     * Returns the message parser for formatting strings into Hytale Message objects.
     *
     * @return the message parser, never null
     * @throws IllegalStateException if the parser has not been initialized
     * @see MessageParser
     */
    MessageParser getMessageParser();

}
