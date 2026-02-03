package dev.spacetivity.tobi.hylib.database.common.api.repository;

import dev.spacetivity.tobi.hylib.database.api.repository.Repository;
import dev.spacetivity.tobi.hylib.database.api.repository.RepositoryLoader;

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
        Repository repo = this.repositories.get(clazz);
        if (repo != null) {
            return Optional.of(repo);
        }
        // Fallback: Suche nach Subtypen
        // Wenn clazz z.B. Repository.class ist, suche nach allen registrierten Repositories
        // Wenn clazz z.B. TestRepository.class (gecastet) ist, suche nach TestRepository
        for (Map.Entry<Class<?>, Repository> entry : this.repositories.entrySet()) {
            // Prüfe ob die registrierte Klasse eine Instanz der gesuchten Klasse ist
            if (clazz.isAssignableFrom(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    @Override
    public Repository getNullable(Class<Repository> clazz) {
        Repository repo = this.repositories.get(clazz);
        if (repo != null) {
            return repo;
        }
        // Fallback: Suche nach Subtypen
        for (Map.Entry<Class<?>, Repository> entry : this.repositories.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public <T extends Repository> T getRepository(Class<T> clazz) {
        // Suche nach exaktem Match
        Repository repo = this.repositories.get(clazz);
        if (repo != null && clazz.isInstance(repo)) {
            return (T) repo;
        }
        // Fallback: Suche nach Subtypen
        for (Map.Entry<Class<?>, Repository> entry : this.repositories.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey()) && clazz.isInstance(entry.getValue())) {
                return (T) entry.getValue();
            }
        }
        return null;
    }

}
