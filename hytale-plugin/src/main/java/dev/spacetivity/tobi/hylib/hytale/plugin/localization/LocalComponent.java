package dev.spacetivity.tobi.hylib.hytale.plugin.localization;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * ECS Component that stores a player's language preference.
 * 
 * <p>This component can be attached to player entities to store their preferred
 * language for localization. If a player entity doesn't have this component,
 * the default language will be used.
 * 
 * <h3>Example Usage</h3>
 * 
 * <pre>{@code
 * // Get player's language
 * LocalComponent local = store.getComponent(ref, LocalComponent.getComponentType());
 * String language = local != null ? local.getLanguage() : "en";
 * 
 * // Set player's language
 * LocalComponent newLocal = new LocalComponent("de");
 * store.addComponent(ref, LocalComponent.getComponentType(), newLocal);
 * }</pre>
 * 
 * @since 1.0
 */
@Getter
@Setter
public class LocalComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, LocalComponent> componentType;

    /**
     * Gets the component type for this component.
     * 
     * <p>This method returns the component type that was registered during plugin setup.
     * The component type must be set using {@link #setComponentType(ComponentType)} before
     * this method is called.
     * 
     * @return the component type for LocalComponent
     * @throws IllegalStateException if the component type has not been set
     */
    public static ComponentType<EntityStore, LocalComponent> getComponentType() {
        ComponentType<EntityStore, LocalComponent> type = LocalComponent.componentType;
        if (type == null) {
            throw new IllegalStateException("LocalComponent type has not been set. Make sure to register it in your plugin setup.");
        }
        return type;
    }

    /**
     * Sets the component type for this component.
     * 
     * <p>This method should be called during plugin setup after registering the component
     * with the EntityStoreRegistry.
     * 
     * @param type the component type to set
     */
    public static void setComponentType(ComponentType<EntityStore, LocalComponent> type) {
        LocalComponent.componentType = type;
    }

    /**
     * Builder codec for serializing and deserializing LocalComponent.
     */
    public static final BuilderCodec<LocalComponent> CODEC = HytaleProvider.getApi()
            .newCodec(LocalComponent.class)
            .field("Language", Codec.STRING, LocalComponent::setLanguage, LocalComponent::getLanguage)
            .withDefault("en")
            .and()
            .build();

    private String language;

    /**
     * Creates a new LocalComponent with the default language ("en").
     */
    public LocalComponent() {
        this("en");
    }

    /**
     * Creates a new LocalComponent with the specified language.
     * 
     * @param language the language code (e.g., "en", "de", "fr")
     */
    public LocalComponent(String language) {
        this.language = language;
    }

    /**
     * Creates a new LocalComponent by copying another.
     * 
     * @param other the LocalComponent to copy
     */
    public LocalComponent(LocalComponent other) {
        this.language = other.language;
    }


    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new LocalComponent(this);
    }

}
