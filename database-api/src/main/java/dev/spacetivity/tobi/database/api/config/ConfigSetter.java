package dev.spacetivity.tobi.database.api.config;

@FunctionalInterface
public interface ConfigSetter<T, V> {

    void set(T instance, V value);

}
