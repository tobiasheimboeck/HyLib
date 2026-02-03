package dev.spacetivity.tobi.hylib.database.api.repository;

import dev.spacetivity.tobi.hylib.database.api.registry.RegistryLoader;

import java.util.List;

/**
 * Registry and loader for {@link Repository} instances. Extends {@link RegistryLoader}.
 *
 * @see Repository
 * @see RegistryLoader
 * @see dev.spacetivity.tobi.hylib.database.api.DatabaseApi#getRepositoryLoader()
 * @since 1.0
 */
public interface RepositoryLoader extends RegistryLoader<Repository> {

    /**
     * Returns all registered repositories (snapshot).
     *
     * @return list of repositories, never null
     */
    List<Repository> getRepositories();

    /**
     * Returns a repository by class; supports subtype matching.
     *
     * @param <T>   the repository type
     * @param clazz the repository class (or supertype)
     * @return the repository, or null if none found
     * @throws NullPointerException if clazz is null
     */
    <T extends Repository> T getRepository(Class<T> clazz);

}
