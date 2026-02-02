package dev.spacetivity.tobi.hylib.hytale.api.localization;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.api.HyMessages;

import javax.annotation.Nullable;

/**
 * ECS component storing a player's language preference.
 * Use {@link HyMessages#getLanguage(com.hypixel.hytale.component.Ref, com.hypixel.hytale.component.Store)} to read it.
 *
 * @since 1.0
 */
public class LanguageComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, LanguageComponent> componentType;

    /**
     * Returns the component type. Must be set via {@link #setComponentType(ComponentType)} (e.g. in plugin setup).
     *
     * @return the component type
     * @throws IllegalStateException if the component type has not been set
     */
    public static ComponentType<EntityStore, LanguageComponent> getComponentType() {
        ComponentType<EntityStore, LanguageComponent> type = LanguageComponent.componentType;
        if (type == null) {
            throw new IllegalStateException("LanguageComponent type has not been set. Make sure to register it in your plugin setup.");
        }
        return type;
    }

    /**
     * Sets the component type (called by the plugin after registering the component).
     *
     * @param type the component type
     */
    public static void setComponentType(ComponentType<EntityStore, LanguageComponent> type) {
        LanguageComponent.componentType = type;
    }

    /** Builder codec for LanguageComponent; Lang is serialized as ISO 639-1 string. */
    public static final BuilderCodec<LanguageComponent> CODEC = HytaleProvider.getApi()
            .newCodec(LanguageComponent.class)
            .field("Language", Codec.STRING,
                    (component, code) -> component.setLanguage(Lang.of(code)),
                    component -> component.getLanguage().getCode())
            .withDefault("en")
            .and()
            .build();

    private Lang language;

    /** Creates a LanguageComponent with the default language. */
    public LanguageComponent() {
        this.language = HyMessages.getDefaultLanguage();
    }

    /**
     * Creates a LanguageComponent with the given lang.
     *
     * @param lang the lang
     * @throws NullPointerException if lang is null
     */
    public LanguageComponent(Lang lang) {
        if (lang == null) {
            throw new NullPointerException("Lang cannot be null");
        }
        this.language = lang;
    }

    /** Creates a LanguageComponent by copying another. */
    public LanguageComponent(LanguageComponent other) {
        this.language = other.language;
    }

    public Lang getLanguage() {
        return language;
    }

    public void setLanguage(Lang language) {
        this.language = language;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new LanguageComponent(this);
    }
}
