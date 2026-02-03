package dev.spacetivity.tobi.hylib.database.common.api.cache;

import dev.spacetivity.tobi.hylib.database.api.cache.Cache;
import dev.spacetivity.tobi.hylib.database.api.cache.CacheLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CacheLoaderImpl implements CacheLoader {

    private final Map<Class<?>, Cache<?, ?>> registeredCaches = new HashMap<>();

    @Override
    public void register(Cache<?, ?> type, Class<? extends Cache<?, ?>> clazz) {
        this.registeredCaches.put(clazz, type);
    }

    @Override
    public Optional<Cache<?, ?>> get(Class<Cache<?, ?>> clazz) {
        Cache<?, ?> cache = this.registeredCaches.get(clazz);
        if (cache != null) {
            return Optional.of(cache);
        }
        // Fallback: Suche nach Subtypen
        for (Map.Entry<Class<?>, Cache<?, ?>> entry : this.registeredCaches.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    @Override
    public Cache<?, ?> getNullable(Class<Cache<?, ?>> clazz) {
        Cache<?, ?> cache = this.registeredCaches.get(clazz);
        if (cache != null) {
            return cache;
        }
        // Fallback: Suche nach Subtypen
        for (Map.Entry<Class<?>, Cache<?, ?>> entry : this.registeredCaches.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public <T extends Cache<?, ?>> T getCache(Class<T> clazz) {
        // Suche nach exaktem Match
        Cache<?, ?> cache = this.registeredCaches.get(clazz);
        if (clazz.isInstance(cache)) {
            return (T) cache;
        }
        // Fallback: Suche nach Subtypen
        for (Map.Entry<Class<?>, Cache<?, ?>> entry : this.registeredCaches.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey()) && clazz.isInstance(entry.getValue())) {
                return (T) entry.getValue();
            }
        }
        return null;
    }
}
