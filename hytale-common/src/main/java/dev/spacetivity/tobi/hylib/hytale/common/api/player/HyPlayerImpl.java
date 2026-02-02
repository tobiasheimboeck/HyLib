package dev.spacetivity.tobi.hylib.hytale.common.api.player;

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
    private String language;

    public HyPlayerImpl(UUID uniqueId, String username) {
        this(uniqueId, username, "en"); // Default language
    }

}
