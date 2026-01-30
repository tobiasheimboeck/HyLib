package dev.spacetivity.tobi.hylib.database.api.repository;

import dev.spacetivity.tobi.hylib.database.api.registry.RegistryLoader;

import java.util.List;

/**
 * Registry and loader for {@link Repository} instances.
 * 
 * <p>This interface extends {@link RegistryLoader} to provide repository-specific functionality
 * for registering and retrieving repository instances. Repositories can be registered and then
 * retrieved by their class type.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * RepositoryLoader loader = DatabaseProvider.getApi().getRepositoryLoader();
 * 
 * // Register a repository
 * UserRepository userRepo = new UserRepository(...);
 * loader.register(userRepo);
 * 
 * // Retrieve a repository
 * UserRepository retrieved = loader.getRepository(UserRepository.class);
 * 
 * // Get all repositories
 * List<Repository> allRepos = loader.getRepositories();
 * }</pre>
 * 
 * <h3>Inherited Methods</h3>
 * 
 * <p>This interface inherits the following methods from {@link RegistryLoader}:
 * <ul>
 *   <li>{@link RegistryLoader#register(Object)} - Register a repository instance</li>
 *   <li>{@link RegistryLoader#register(Object, Class)} - Register a repository with a specific class</li>
 *   <li>{@link RegistryLoader#get(Class)} - Get a repository wrapped in Optional</li>
 *   <li>{@link RegistryLoader#getNullable(Class)} - Get a repository or null</li>
 * </ul>
 * 
 * @see Repository
 * @see RegistryLoader
 * @see DatabaseApi#getRepositoryLoader()
 * @since 1.0
 */
public interface RepositoryLoader extends RegistryLoader<Repository> {

    /**
     * Gets all registered repositories.
     * 
     * <p>This method returns a list of all repositories that have been registered
     * with this loader. The returned list is a snapshot and will not reflect
     * changes made after this method returns.
     * 
     * @return a list of all registered repositories, never null (may be empty)
     */
    List<Repository> getRepositories();

    /**
     * Gets a repository instance by its class, supporting subtypes.
     * 
     * <p>This method searches for a repository that is an instance of the specified class.
     * It supports subtype matching, so you can retrieve a repository using a superclass or
     * interface type.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * // Register a specific repository implementation
     * UserRepository userRepo = new UserRepository(...);
     * loader.register(userRepo);
     * 
     * // Retrieve it using the exact class
     * UserRepository retrieved = loader.getRepository(UserRepository.class);
     * 
     * // Or using a superclass/interface
     * AbstractMariaDbRepository<?> repo = loader.getRepository(AbstractMariaDbRepository.class);
     * }</pre>
     * 
     * @param <T>   the repository type (must extend Repository)
     * @param clazz the repository class to search for (can be a subtype of Repository)
     * @return the repository instance if found, or {@code null} if no matching repository exists
     * @throws NullPointerException if clazz is null
     */
    <T extends Repository> T getRepository(Class<T> clazz);

}
