package dev.spacetivity.tobi.hylib.hytale.common.api.message;

import com.hypixel.hytale.protocol.MaybeBool;
import com.hypixel.hytale.server.core.Message;
import dev.spacetivity.tobi.hylib.hytale.api.message.MessageParser;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link MessageParser}.
 * 
 * <p>
 * This implementation parses formatted strings containing formatting tags into
 * Hytale Message objects. It supports colors, gradients, formatting styles,
 * links,
 * and nested tags.
 * 
 * @see MessageParser
 * @since 1.0
 */
public class MessageParserImpl implements MessageParser {

    // Matches <tag>, <tag:arg>, </tag>
    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([a-zA-Z0-9_]+)(?::([^>]+))?>");

    private static final Map<String, Color> NAMED_COLORS = new HashMap<>();

    static {
        NAMED_COLORS.put("black", new Color(0, 0, 0));
        NAMED_COLORS.put("dark_blue", new Color(0, 0, 170));
        NAMED_COLORS.put("dark_green", new Color(0, 170, 0));
        NAMED_COLORS.put("dark_aqua", new Color(0, 170, 170));
        NAMED_COLORS.put("dark_red", new Color(170, 0, 0));
        NAMED_COLORS.put("dark_purple", new Color(170, 0, 170));
        NAMED_COLORS.put("gold", new Color(255, 170, 0));
        NAMED_COLORS.put("gray", new Color(170, 170, 170));
        NAMED_COLORS.put("dark_gray", new Color(85, 85, 85));
        NAMED_COLORS.put("blue", new Color(85, 85, 255));
        NAMED_COLORS.put("green", new Color(85, 255, 85));
        NAMED_COLORS.put("aqua", new Color(85, 255, 255));
        NAMED_COLORS.put("red", new Color(255, 85, 85));
        NAMED_COLORS.put("light_purple", new Color(255, 85, 255));
        NAMED_COLORS.put("yellow", new Color(255, 255, 85));
        NAMED_COLORS.put("white", new Color(255, 255, 255));
    }

    private record MessageFormat(
            Color color,
            List<Color> gradient,
            boolean bold,
            boolean italic,
            boolean underlined,
            boolean monospace,
            String link) {

        MessageFormat() {
            this(null, null, false, false, false, false, null);
        }
    }

    @Override
    public Message parse(String text) {
        if (text == null) {
            throw new NullPointerException("Text cannot be null");
        }

        if (!text.contains("<")) {
            return Message.raw(text);
        }

        Message root = Message.empty();

        // Stack keeps track of nested styles.
        // Example: Stack = [Base, Bold, Bold+Red]
        Deque<MessageFormat> stateStack = new ArrayDeque<>();
        stateStack.push(new MessageFormat()); // Start with default empty state

        Matcher matcher = TAG_PATTERN.matcher(text);
        int lastIndex = 0;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Handle text BEFORE this tag (using the state at the top of the stack)
            if (start > lastIndex) {
                String content = text.substring(lastIndex, start);
                Message segmentMsg = createStyledMessage(content, stateStack.peek());
                if (segmentMsg != null) {
                    root.insert(segmentMsg);
                }
            }

            // Process the tag to update the Stack
            boolean isClosing = "/".equals(matcher.group(1));
            String tagName = matcher.group(2).toLowerCase();
            String tagArg = matcher.group(3);

            if (isClosing) {
                handleClosingTag(stateStack);
                lastIndex = end;
                continue;
            }

            MessageFormat newState = processOpeningTag(tagName, tagArg, stateStack.peek());
            stateStack.push(newState);

            lastIndex = end;
        }

        if (lastIndex < text.length()) {
            String content = text.substring(lastIndex);
            Message segmentMsg = createStyledMessage(content, stateStack.peek());
            if (segmentMsg != null) {
                root.insert(segmentMsg);
            }
        }

        return root;
    }

    private void handleClosingTag(Deque<MessageFormat> stateStack) {
        if (stateStack.size() > 1) {
            stateStack.pop();
        }
    }

    private MessageFormat processOpeningTag(String tagName, String tagArg, MessageFormat currentState) {
        // Check for named colors first
        if (NAMED_COLORS.containsKey(tagName)) {
            return createFormatWithColor(NAMED_COLORS.get(tagName), currentState);
        }

        return switch (tagName) {
            case "color", "c", "colour" -> processColorTag(tagArg, currentState);
            case "grnt", "gradient" -> processGradientTag(tagArg, currentState);
            case "bold", "b" -> createFormatWithBold(true, currentState);
            case "italic", "i", "em" -> createFormatWithItalic(true, currentState);
            case "underline", "u" -> createFormatWithUnderlined(true, currentState);
            case "monospace", "mono" -> createFormatWithMonospace(true, currentState);
            case "link", "url" -> processLinkTag(tagArg, currentState);
            case "reset", "r" -> new MessageFormat();
            default -> currentState;
        };
    }

    private MessageFormat processColorTag(String tagArg, MessageFormat currentState) {
        Color color = parseColorArg(tagArg);
        return color != null ? createFormatWithColor(color, currentState) : currentState;
    }

    private MessageFormat processGradientTag(String tagArg, MessageFormat currentState) {
        if (tagArg == null) {
            return currentState;
        }
        List<Color> colors = parseGradientColors(tagArg);
        return !colors.isEmpty() ? createFormatWithGradient(colors, currentState) : currentState;
    }

    private MessageFormat processLinkTag(String tagArg, MessageFormat currentState) {
        return tagArg != null ? createFormatWithLink(tagArg, currentState) : currentState;
    }

    private MessageFormat createFormatWithColor(Color color, MessageFormat currentState) {
        return new MessageFormat(color, null, currentState.bold(), currentState.italic(),
                currentState.underlined(), currentState.monospace(), currentState.link());
    }

    private MessageFormat createFormatWithGradient(List<Color> gradient, MessageFormat currentState) {
        return new MessageFormat(null, gradient, currentState.bold(), currentState.italic(),
                currentState.underlined(), currentState.monospace(), currentState.link());
    }

    private MessageFormat createFormatWithBold(boolean bold, MessageFormat currentState) {
        return new MessageFormat(currentState.color(), currentState.gradient(), bold,
                currentState.italic(), currentState.underlined(), currentState.monospace(), currentState.link());
    }

    private MessageFormat createFormatWithItalic(boolean italic, MessageFormat currentState) {
        return new MessageFormat(currentState.color(), currentState.gradient(), currentState.bold(),
                italic, currentState.underlined(), currentState.monospace(), currentState.link());
    }

    private MessageFormat createFormatWithUnderlined(boolean underlined, MessageFormat currentState) {
        return new MessageFormat(currentState.color(), currentState.gradient(), currentState.bold(),
                currentState.italic(), underlined, currentState.monospace(), currentState.link());
    }

    private MessageFormat createFormatWithMonospace(boolean monospace, MessageFormat currentState) {
        return new MessageFormat(currentState.color(), currentState.gradient(), currentState.bold(),
                currentState.italic(), currentState.underlined(), monospace, currentState.link());
    }

    private MessageFormat createFormatWithLink(String link, MessageFormat currentState) {
        return new MessageFormat(currentState.color(), currentState.gradient(), currentState.bold(),
                currentState.italic(), currentState.underlined(), currentState.monospace(), link);
    }

    private Message createStyledMessage(String content, MessageFormat state) {
        // If we have a gradient, we must return a container with char-by-char coloring
        if (state.gradient != null && !state.gradient.isEmpty()) {
            return applyGradient(content, state);
        }

        Message msg = Message.raw(content);

        if (state.color != null)
            msg.color(state.color);
        if (state.bold)
            msg.bold(true);
        if (state.italic)
            msg.italic(true);
        if (state.monospace)
            msg.monospace(true);
        if (state.underlined)
            msg.getFormattedMessage().underlined = MaybeBool.True;
        if (state.link != null)
            msg.link(state.link);

        return msg;
    }

    private Message applyGradient(String text, MessageFormat state) {
        Message container = Message.empty();
        List<Color> colors = state.gradient;
        int length = text.length();

        for (int index = 0; index < length; index++) {
            char ch = text.charAt(index);
            float progress = index / (float) Math.max(length - 1, 1);
            Color color = interpolateColor(colors, progress);

            Message charMsg = Message.raw(String.valueOf(ch)).color(color);

            if (state.bold)
                charMsg.bold(true);
            if (state.italic)
                charMsg.italic(true);
            if (state.monospace)
                charMsg.monospace(true);
            if (state.underlined)
                charMsg.getFormattedMessage().underlined = MaybeBool.True;
            if (state.link != null)
                charMsg.link(state.link);

            container.insert(charMsg);
        }
        return container;
    }

    private Color parseColorArg(String arg) {
        if (arg == null)
            return null;
        return NAMED_COLORS.containsKey(arg) ? NAMED_COLORS.get(arg) : parseHexColor(arg);
    }

    private List<Color> parseGradientColors(String arg) {
        List<Color> colors = new ArrayList<>();
        for (String part : arg.split(":")) {
            Color c = parseColorArg(part);
            if (c != null)
                colors.add(c);
        }
        return colors;
    }

    private Color parseHexColor(String hex) {
        try {
            String clean = hex.replace("#", "");
            if (clean.length() == 6) {
                int r = Integer.parseInt(clean.substring(0, 2), 16);
                int g = Integer.parseInt(clean.substring(2, 4), 16);
                int b = Integer.parseInt(clean.substring(4, 6), 16);
                return new Color(r, g, b);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Color interpolateColor(List<Color> colors, float progress) {
        float clampedProgress = Math.max(0f, Math.min(1f, progress));
        float scaledProgress = clampedProgress * (colors.size() - 1);
        int index = Math.min((int) scaledProgress, colors.size() - 2);
        float localProgress = scaledProgress - index;

        Color c1 = colors.get(index);
        Color c2 = colors.get(index + 1);

        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * localProgress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * localProgress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * localProgress);

        return new Color(r, g, b);
    }

}
