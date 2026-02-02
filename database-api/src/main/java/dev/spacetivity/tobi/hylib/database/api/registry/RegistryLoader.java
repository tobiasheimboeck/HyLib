package dev.spacetivity.tobi.hylib.database.api.registry;

import java.util.Optional;

/**
 * Registry for objects by class type; supports subtype matching. Base for CacheLoader and RepositoryLoader.
 *
 * @param <T> the managed type
 * @see dev.spacetivity.tobi.hylib.database.api.cache.CacheLoader
 * @see dev.spacetivity.tobi.hylib.database.api.repository.RepositoryLoader
 * @since 1.0
 */
public interface RegistryLoader<T> {

    /**
     * Registers an object using its runtime class (convenience for {@link #register(Object, Class)}).
     *
     * @param type the object to register
     * @throws NullPointerException if type is null
     * @see #register(Object, Class)
     */
    @SuppressWarnings("unchecked")
    default void register(T type) {
        register(type, (Class<? extends T>) type.getClass());
    }

    /**
     * Registers an object under a specific class type (may differ from runtime class).
     *
     * @param type  the object to register
     * @param clazz the class to register it as
     * @throws NullPointerException if type or clazz is null
     * @throws IllegalArgumentException if type is not an instance of clazz
     */
    void register(T type, Class<? extends T> clazz);

    /**
     * Returns an object by class, wrapped in Optional. Supports subtype matching.
     *
     * @param clazz the class to search for
     * @return Optional with the instance, or empty
     * @throws NullPointerException if clazz is null
     * @see #getNullable(Class)
     */
    Optional<T> get(Class<T> clazz);

    /**
     * Returns an object by class, or null if not found. Supports subtype matching.
     *
     * @param clazz the class to search for
     * @return the instance, or null
     * @throws NullPointerException if clazz is null
     * @see #get(Class)
     */
    T getNullable(Class<T> clazz);

}
