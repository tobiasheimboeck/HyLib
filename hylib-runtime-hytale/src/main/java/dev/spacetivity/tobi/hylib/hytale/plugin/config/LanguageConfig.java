package dev.spacetivity.tobi.hylib.hytale.plugin.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LanguageConfig {

    private boolean languageCommandEnabled = false;
    private String defaultLanguage = "en";

    public static BuilderCodec<LanguageConfig> CODEC = BuilderCodec.builder(LanguageConfig.class, LanguageConfig::new)
            .append(new KeyedCodec<>("LanguageCommandEnabled", Codec.BOOLEAN), (obj, val, info) -> obj.setLanguageCommandEnabled(val != null ? val : true), (obj, info) -> obj.isLanguageCommandEnabled()).add()
            .append(new KeyedCodec<>("DefaultLanguage", Codec.STRING), (obj, val, info) -> obj.setDefaultLanguage(val != null ? val : "en"), (obj, info) -> obj.getDefaultLanguage()).add()
            .build();

}
