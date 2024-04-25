package net.neptunsworld.elytra.database.api.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedissonPacketReceiver {

    PROXY(false),
    BUKKIT(false),
    DISCORD(false),
    CONTROLLER(false),
    ALL(true);

    private final boolean all;

}
