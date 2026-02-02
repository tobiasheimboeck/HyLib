package dev.spacetivity.tobi.hylib.hytale.api.localization;

/**
 * Type-safe translation key for localization. Create via {@link #of(String)}.
 *
 * @since 1.0
 */
public final class LangKey {

    private final String key;

    private LangKey(String key) {
        this.key = key;
    }

    /**
     * Returns the translation key string.
     *
     * @return the key (e.g. "myplugin.welcome")
     */
    public String getKey() {
        return key;
    }

    /**
     * Creates a translation key from a string.
     *
     * @param key the translation key (e.g. "myplugin.welcome")
     * @return a new LangKey
     * @throws NullPointerException if key is null
     * @throws IllegalArgumentException if key is empty
     */
    public static LangKey of(String key) {
        if (key == null) {
            throw new NullPointerException("Translation key cannot be null");
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Translation key cannot be empty");
        }
        return new LangKey(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LangKey other = (LangKey) obj;
        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "LangKey{" + key + "}";
    }
}
