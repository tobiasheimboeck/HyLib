package net.neptunsworld.elytra.database.common.api.cache;

import net.neptunsworld.elytra.database.api.cache.Cache;
import net.neptunsworld.elytra.database.api.cache.CacheLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CacheLoaderImpl implements CacheLoader {

    private final Map<Class<?>, Cache> registeredCaches = new HashMap<>();

    @Override
    public void register(Cache type, Class<? extends Cache> clazz) {
        this.registeredCaches.put(clazz, type);
    }

    @Override
    public Optional<Cache> get(Class<Cache> clazz) {
        return Optional.ofNullable(this.registeredCaches.get(clazz));
    }

    @Override
    public Cache getNullable(Class<Cache> clazz) {
        return this.registeredCaches.get(clazz);
    }
}
