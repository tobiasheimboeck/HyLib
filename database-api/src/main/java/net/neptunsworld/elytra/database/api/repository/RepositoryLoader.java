package net.neptunsworld.elytra.database.api.repository;

import net.neptunsworld.elytra.database.api.registry.RegistryLoader;

import java.util.List;

public interface RepositoryLoader extends RegistryLoader<Repository> {

    List<Repository> getRepositories();

}
