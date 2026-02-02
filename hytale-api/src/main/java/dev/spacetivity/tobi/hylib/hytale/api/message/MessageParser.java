package dev.spacetivity.tobi.hylib.hytale.api.message;

import com.hypixel.hytale.server.core.Message;

/**
 * Parses formatted strings (e.g. {@code <red>}, {@code <gradient:red:blue>}, {@code <b>}) into Hytale {@link Message}.
 * Obtain via {@link dev.spacetivity.tobi.hylib.hytale.api.HytaleApi#getMessageParser()}.
 *
 * @see dev.spacetivity.tobi.hylib.hytale.api.HytaleApi#getMessageParser()
 * @see Message
 * @since 1.0
 */
public interface MessageParser {

    /**
     * Parses a string with formatting tags into a Hytale Message.
     *
     * @param text the string with tags (colors, gradients, bold, italic, link, etc.)
     * @return formatted Message
     * @throws NullPointerException if text is null
     * @see Message
     */
    Message parse(String text);

}
