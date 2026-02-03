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
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LanguageComponent;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LocalizationService;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * ECS system that synchronizes LanguageComponent with HyPlayer (database).
 *
 * @since 1.0
 */
public class LocalizationSystem extends RefChangeSystem<EntityStore, LanguageComponent> {

    private static final HyPlayerService hyPlayerService = HytaleProvider.getApi().getHyPlayerService();

    @Nonnull
    @Override
    public ComponentType<EntityStore, LanguageComponent> componentType() {
        return LanguageComponent.getComponentType();
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull LanguageComponent languageComponent,
                                 @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // Component was added - synchronize with HyPlayer
        UUID uniqueId = getUniqueId(ref, store);
        if (uniqueId != null) {
            hyPlayerService.changeLanguage(uniqueId, languageComponent.getLanguage());
        }
    }

    @Override
    public void onComponentSet(@Nonnull Ref<EntityStore> ref, @Nullable LanguageComponent oldComponent,
                               @Nonnull LanguageComponent newComponent, @Nonnull Store<EntityStore> store,
                               @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // Component was updated - synchronize language change with HyPlayer
        UUID uniqueId = getUniqueId(ref, store);
        if (uniqueId != null) {
            hyPlayerService.changeLanguage(uniqueId, newComponent.getLanguage());
        }
    }

    @Override
    public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull LanguageComponent languageComponent,
                                   @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // Component was removed - reset to default lang in HyPlayer
        UUID uniqueId = getUniqueId(ref, store);
        if (uniqueId != null) {
            LocalizationService localizationService = HytaleProvider.getApi().getLocalizationService();
            Lang defaultLang = localizationService.getDefaultLanguage();
            hyPlayerService.changeLanguage(uniqueId, defaultLang);
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return LanguageComponent.getComponentType();
    }

    private UUID getUniqueId(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        return uuidComponent != null ? uuidComponent.getUuid() : null;
    }

}
