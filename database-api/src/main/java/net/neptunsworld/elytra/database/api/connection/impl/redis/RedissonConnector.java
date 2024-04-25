package net.neptunsworld.elytra.database.api.connection.impl.redis;

import net.neptunsworld.elytra.database.api.connection.DatabaseConnector;
import net.neptunsworld.elytra.database.api.connection.DatabaseType;
import net.neptunsworld.elytra.database.api.connection.codec.SerializationCodec;
import net.neptunsworld.elytra.database.api.connection.credentials.impl.RedisCredentials;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.text.MessageFormat;

public class RedissonConnector extends DatabaseConnector<RedissonClient, RedisCredentials> {

    public RedissonConnector() {
        super(DatabaseType.REDIS);
    }

    @Override
    public void establishConnection(RedisCredentials credentials) {
        this.setClient(Redisson.create(this.createClientSettings(credentials.hostname(), credentials.port(), credentials.password())));
    }

    private Config createClientSettings(String hostname, int port, String password) {
        Config config = new Config();
        config.setCodec(new SerializationCodec());
        config.setNettyThreads(4);
        config.useSingleServer().setAddress(MessageFormat.format("redis://{0}:{1}", hostname, String.valueOf(port))).setPassword(password);
        return config;
    }

}
