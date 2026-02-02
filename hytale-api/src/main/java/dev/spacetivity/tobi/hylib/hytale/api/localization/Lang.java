package dev.spacetivity.tobi.hylib.hytale.api.localization;

/**
 * Type-safe language code for localization (ISO 639-1).
 * Create via {@link #of(String)}.
 *
 * @see LocalizationService#getAvailableLanguages()
 * @since 1.0
 */
public final class Lang {

    private final String code;

    private Lang(String code) {
        this.code = code;
    }

    /**
     * Returns the ISO 639-1 language code string.
     *
     * @return the language code (e.g. "en", "de")
     */
    public String getCode() {
        return code;
    }

    /**
     * Creates a Lang from an ISO 639-1 language code.
     *
     * @param code the language code (e.g. "en", "de")
     * @return a new Lang
     * @throws NullPointerException if code is null
     * @throws IllegalArgumentException if code is empty or invalid
     */
    public static Lang of(String code) {
        if (code == null) {
            throw new NullPointerException("Language code cannot be null");
        }
        if (code.isEmpty()) {
            throw new IllegalArgumentException("Language code cannot be empty");
        }
        return new Lang(code);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Lang other = (Lang) obj;
        return code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return "Lang{" + code + "}";
    }
}
