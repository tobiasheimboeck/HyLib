package dev.spacetivity.tobi.hylib.hytale.common.api.player;

import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class HyPlayerImpl implements HyPlayer {

    private final UUID uniqueId;

    @Setter
    private String username;

    @Setter
    private Lang language;

    public HyPlayerImpl(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.language = HytaleProvider.getApi().getLocalizationService().getDefaultLanguage();
    }

}
