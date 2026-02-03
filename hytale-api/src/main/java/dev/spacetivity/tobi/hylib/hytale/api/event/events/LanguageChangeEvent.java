package dev.spacetivity.tobi.hylib.hytale.api.event.events;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import lombok.Getter;

@Getter
public class LanguageChangeEvent implements IEvent<Lang> {

    private final PlayerRef playerRef;
    private final Lang newLang;

    public LanguageChangeEvent(PlayerRef playerRef, Lang newLang) {
        this.playerRef = playerRef;
        this.newLang = newLang;
    }
}
