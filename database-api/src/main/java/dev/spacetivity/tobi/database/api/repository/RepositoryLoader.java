package dev.spacetivity.tobi.database.api.repository;

import dev.spacetivity.tobi.database.api.registry.RegistryLoader;

import java.util.List;

public interface RepositoryLoader extends RegistryLoader<Repository> {

    List<Repository> getRepositories();

}
