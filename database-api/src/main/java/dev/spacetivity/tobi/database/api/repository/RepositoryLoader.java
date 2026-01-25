package dev.spacetivity.tobi.database.api.repository;

import dev.spacetivity.tobi.database.api.registry.RegistryLoader;

import java.util.List;

public interface RepositoryLoader extends RegistryLoader<Repository> {

    List<Repository> getRepositories();

    /**
     * Gets a repository by its class, supporting subtypes.
     * @param clazz the repository class (can be a subtype of Repository)
     * @param <T> the repository type
     * @return the repository instance, or null if not found
     */
    <T extends Repository> T getRepository(Class<T> clazz);

}
