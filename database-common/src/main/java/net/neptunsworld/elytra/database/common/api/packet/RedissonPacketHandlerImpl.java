package net.neptunsworld.elytra.database.common.api.packet;

import lombok.AllArgsConstructor;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnectionHandler;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnector;
import net.neptunsworld.elytra.database.api.connection.DatabaseType;
import net.neptunsworld.elytra.database.api.connection.credentials.DatabaseCredentials;
import net.neptunsworld.elytra.database.api.packet.RedissonPacket;
import net.neptunsworld.elytra.database.api.packet.RedissonPacketHandler;
import net.neptunsworld.elytra.database.api.packet.RedissonPacketReceiver;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.function.Consumer;

@AllArgsConstructor
public class RedissonPacketHandlerImpl implements RedissonPacketHandler {

    private final DatabaseConnectionHandler db;
    private final RedissonPacketReceiver acceptedReceiver;

    @Override
    public RTopic getChannel(String channelName) {
        DatabaseConnector<RedissonClient, DatabaseCredentials> connector = this.db.getConnectorNullsafe(DatabaseType.REDIS);
        return connector.getSafeConnection().getTopic(channelName);
    }

    @Override
    public <P extends RedissonPacket> void sendPacket(String channelName, P packet) {
        getChannel(channelName).publishAsync(packet);
    }

    @Override
    public <P extends RedissonPacket> void listenForPacket(String channelName, Class<P> packetClass, Consumer<P> result) {
        listenForPacket(getChannel(channelName), packetClass, result);
    }

    @Override
    public <P extends RedissonPacket> void listenForPacket(RTopic channel, Class<P> packetClass, Consumer<P> result) {
        channel.addListenerAsync(packetClass, (channel1, packet) -> {
            if (!packet.isValid(this.acceptedReceiver)) return;
            result.accept(packet);
        });
    }

}
