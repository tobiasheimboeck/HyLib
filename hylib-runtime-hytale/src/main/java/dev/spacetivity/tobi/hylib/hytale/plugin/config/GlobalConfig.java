package dev.spacetivity.tobi.hylib.hytale.plugin.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlobalConfig {

    private boolean languageCommandEnabled;

    public static BuilderCodec<GlobalConfig> CODEC = BuilderCodec.builder(GlobalConfig.class, GlobalConfig::new)
            .append(new KeyedCodec<>("LanguageCommandEnabled", Codec.BOOLEAN), (obj, val, info) -> obj.setLanguageCommandEnabled(val != null ? val : true), (obj, info) -> obj.isLanguageCommandEnabled()).add()
            .build();

}
