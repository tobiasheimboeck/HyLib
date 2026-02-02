package dev.spacetivity.tobi.hylib.hytale.plugin.localization;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * ECS System that synchronizes LocalComponent with HyPlayer.
 *
 * <p>This system automatically synchronizes language changes between the ECS Component
 * and the database-backed HyPlayer. When a LocalComponent is changed, it updates the
 * corresponding HyPlayer in the database.
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * // In plugin setup:
 * this.getEntityStoreRegistry().registerSystem(new LocalizationSystem());
 *
 * // In your ECS system, you can now directly access the language:
 * LocalComponent local = store.getComponent(ref, LocalComponent.getComponentType());
 * String language = local != null ? local.getLanguage() : "en";
 * }</pre>
 *
 * @since 1.0
 */
public class LocalizationSystem extends RefChangeSystem<EntityStore, LocalComponent> {

    private static final HyPlayerService hyPlayerService = HytaleProvider.getApi().getHyPlayerService();

    @Nonnull
    @Override
    public ComponentType<EntityStore, LocalComponent> componentType() {
        return LocalComponent.getComponentType();
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull LocalComponent localComponent,
                                 @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // Component was added - synchronize with HyPlayer
        UUID uniqueId = getUniqueId(ref, store);
        if (uniqueId != null) {
            hyPlayerService.changeLanguage(uniqueId, localComponent.getLanguage());
        }
    }

    @Override
    public void onComponentSet(@Nonnull Ref<EntityStore> ref, @Nullable LocalComponent oldComponent,
                               @Nonnull LocalComponent newComponent, @Nonnull Store<EntityStore> store,
                               @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // Component was updated - synchronize language change with HyPlayer
        UUID uniqueId = getUniqueId(ref, store);
        if (uniqueId != null) {
            hyPlayerService.changeLanguage(uniqueId, newComponent.getLanguage());
        }
    }

    @Override
    public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull LocalComponent localComponent,
                                   @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // Component was removed - reset to default language in HyPlayer
        UUID uniqueId = getUniqueId(ref, store);
        if (uniqueId != null) {
            hyPlayerService.changeLanguage(uniqueId, "en"); // Default language
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return LocalComponent.getComponentType();
    }

    private UUID getUniqueId(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        return uuidComponent != null ? uuidComponent.getUuid() : null;
    }

}
