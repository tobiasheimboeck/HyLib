package net.neptunsworld.elytra.database.api.registry;

import java.util.Optional;

public interface RegistryLoader<T> {

    @SuppressWarnings("unchecked")
    default void register(T type) {
        register(type, (Class<? extends T>) type.getClass());
    }

    void register(T type, Class<? extends T> clazz);

    Optional<T> get(Class<T> clazz);

    T getNullable(Class<T> clazz);

}
