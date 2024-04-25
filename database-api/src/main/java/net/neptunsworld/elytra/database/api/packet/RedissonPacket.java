package net.neptunsworld.elytra.database.api.packet;

import java.io.Serializable;

public abstract class RedissonPacket implements Serializable {

    private final RedissonPacketReceiver receiver;

    public RedissonPacket(RedissonPacketReceiver receiver) {
        this.receiver = receiver;
    }

    public boolean isValid(RedissonPacketReceiver acceptedReceiver) {
        return this.receiver.isAll() || this.receiver == acceptedReceiver;
    }

}
