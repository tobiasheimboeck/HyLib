package dev.spacetivity.tobi.database.api.config;

@FunctionalInterface
public interface ConfigGetter<T, V> {

    V get(T instance);

}
