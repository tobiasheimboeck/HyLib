package dev.spacetivity.tobi.hylib.hytale.plugin.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DbConfig {

    private String hostname = "localhost";
    private Integer port = 5520;
    private String username = "root";
    private String database = "game_db";
    private String password = "password";

    public static BuilderCodec<DbConfig> CODEC = HytaleProvider.getApi().newCodec(DbConfig.class)
            .field("Hostname", Codec.STRING, DbConfig::setHostname, DbConfig::getHostname)
            .withDefault("localhost")
            .and()
            .field("Port", Codec.INTEGER, DbConfig::setPort, DbConfig::getPort)
            .withDefault(5520)
            .and()
            .field("Username", Codec.STRING, DbConfig::setUsername, DbConfig::getUsername)
            .withDefault("root")
            .and()
            .field("Database", Codec.STRING, DbConfig::setDatabase, DbConfig::getDatabase)
            .withDefault("game_db")
            .and()
            .field("Password", Codec.STRING, DbConfig::setPassword, DbConfig::getPassword)
            .withDefault("password")
            .and()
            .build();

}
