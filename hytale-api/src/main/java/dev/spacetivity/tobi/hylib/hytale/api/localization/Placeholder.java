package dev.spacetivity.tobi.hylib.hytale.api.localization;

/**
 * Named placeholder for translation strings. Use {@code {name}} in translation; create via {@link #of(String, Object)}.
 *
 * @see LocalizationService#translate(LangKey, Lang, Placeholder...)
 * @since 1.0
 */
public record Placeholder(String name, Object value) {

    /**
     * Creates a named placeholder.
     *
     * @param name  the placeholder name ({@code {name}} in translation)
     * @param value the value (converted via {@link String#valueOf(Object)})
     * @return a new placeholder
     * @throws NullPointerException if name is null
     */
    public static Placeholder of(String name, Object value) {
        if (name == null) {
            throw new NullPointerException("Placeholder name cannot be null");
        }
        return new Placeholder(name, value);
    }
}
