package net.neptunsworld.elytra.database.api.packet;

import org.redisson.api.RTopic;

import java.util.function.Consumer;

public interface RedissonPacketHandler {

    RTopic getChannel(String channelName);

    <P extends RedissonPacket> void sendPacket(String channelName, P packet);

    <P extends RedissonPacket> void listenForPacket(String channelName, Class<P> packetClass, Consumer<P> result);

    <P extends RedissonPacket> void listenForPacket(RTopic channel, Class<P> packetClass, Consumer<P> result);

}
