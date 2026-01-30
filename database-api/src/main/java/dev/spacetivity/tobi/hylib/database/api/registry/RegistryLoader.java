package dev.spacetivity.tobi.hylib.database.api.registry;

import java.util.Optional;

/**
 * Generic registry interface for loading and retrieving objects by their class type.
 * 
 * <p>This interface provides a type-safe registry pattern for managing instances of
 * a specific type. It is used as a base interface for more specific loaders like
 * {@link dev.spacetivity.tobi.database.api.cache.CacheLoader} and
 * {@link dev.spacetivity.tobi.database.api.repository.RepositoryLoader}.
 * 
 * <h3>Usage Pattern</h3>
 * 
 * <pre>{@code
 * RegistryLoader<MyType> loader = ...;
 * 
 * // Register an instance
 * MyType instance = new MyType();
 * loader.register(instance);
 * 
 * // Retrieve using Optional
 * Optional<MyType> opt = loader.get(MyType.class);
 * MyType retrieved = opt.orElse(null);
 * 
 * // Or retrieve directly (may return null)
 * MyType direct = loader.getNullable(MyType.class);
 * }</pre>
 * 
 * <h3>Subtype Support</h3>
 * 
 * <p>This interface supports subtype matching, meaning you can register a subtype
 * and retrieve it using a superclass or interface type.
 * 
 * @param <T> the type of objects managed by this registry
 * @see dev.spacetivity.tobi.database.api.cache.CacheLoader
 * @see dev.spacetivity.tobi.database.api.repository.RepositoryLoader
 * @since 1.0
 */
public interface RegistryLoader<T> {

    /**
     * Registers an object instance using its runtime class.
     * 
     * <p>This is a convenience method that calls {@link #register(Object, Class)} with
     * the object's runtime class. The object is registered and can be retrieved using
     * its class or any superclass/interface it implements.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * MyType instance = new MyType();
     * loader.register(instance);  // Registered as MyType.class
     * }</pre>
     * 
     * @param type the object instance to register
     * @throws NullPointerException if type is null
     * @see #register(Object, Class)
     */
    @SuppressWarnings("unchecked")
    default void register(T type) {
        register(type, (Class<? extends T>) type.getClass());
    }

    /**
     * Registers an object instance with a specific class type.
     * 
     * <p>This method allows you to register an object with a specific class type,
     * which may differ from the object's runtime class. This is useful when you
     * want to register an object under a superclass or interface type.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * MySubType instance = new MySubType();
     * loader.register(instance, MySuperType.class);  // Registered as MySuperType
     * 
     * // Can be retrieved as MySuperType
     * MySuperType retrieved = loader.getNullable(MySuperType.class);
     * }</pre>
     * 
     * @param type  the object instance to register
     * @param clazz the class type to register it as
     * @throws NullPointerException if type or clazz is null
     * @throws IllegalArgumentException if type is not an instance of clazz
     */
    void register(T type, Class<? extends T> clazz);

    /**
     * Gets an object instance by its class, wrapped in an {@link Optional}.
     * 
     * <p>This method searches for an object that is an instance of the specified class.
     * It supports subtype matching, so you can retrieve an object using a superclass or
     * interface type.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * Optional<MyType> opt = loader.get(MyType.class);
     * if (opt.isPresent()) {
     *     MyType instance = opt.get();
     *     // Use instance
     * }
     * }</pre>
     * 
     * @param clazz the class to search for
     * @return an Optional containing the instance if found, or empty Optional if not found
     * @throws NullPointerException if clazz is null
     * @see #getNullable(Class)
     */
    Optional<T> get(Class<T> clazz);

    /**
     * Gets an object instance by its class, returning {@code null} if not found.
     * 
     * <p>This method is similar to {@link #get(Class)} but returns {@code null} instead
     * of an empty Optional. Use this method when you prefer null checks over Optional handling.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * MyType instance = loader.getNullable(MyType.class);
     * if (instance != null) {
     *     // Use instance
     * }
     * }</pre>
     * 
     * @param clazz the class to search for
     * @return the instance if found, or {@code null} if not found
     * @throws NullPointerException if clazz is null
     * @see #get(Class)
     */
    T getNullable(Class<T> clazz);

}
