package dev.spacetivity.tobi.database.common.api.repository;

import dev.spacetivity.tobi.database.api.repository.Repository;
import dev.spacetivity.tobi.database.api.repository.RepositoryLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RepositoryLoaderImpl implements RepositoryLoader {

    private final Map<Class<?>, Repository> repositories = new HashMap<>();

    @Override
    public List<Repository> getRepositories() {
        return this.repositories.values().stream().toList();
    }

    @Override
    public void register(Repository type, Class<? extends Repository> clazz) {
        this.repositories.put(clazz, type);
    }

    @Override
    public Optional<Repository> get(Class<Repository> clazz) {
        return Optional.ofNullable(this.repositories.get(clazz));
    }

    @Override
    public Repository getNullable(Class<Repository> clazz) {
        return this.repositories.get(clazz);
    }

}
