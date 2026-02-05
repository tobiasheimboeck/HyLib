package dev.spacetivity.tobi.hylib.hytale.plugin.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DbConfig {

    private boolean enabled = false;
    private String hostname = "localhost";
    private Integer port = 3306;
    private String username = "root";
    private String database = "game_db";
    private String password = "password";

    public static BuilderCodec<DbConfig> CODEC = BuilderCodec.builder(DbConfig.class, DbConfig::new)
            .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN), (obj, val, info) -> obj.setEnabled(val != null ? val : false), (obj, info) -> obj.isEnabled()).add()
            .append(new KeyedCodec<>("Hostname", Codec.STRING), (obj, val, info) -> obj.setHostname(val != null ? val : "localhost"), (obj, info) -> obj.getHostname()).add()
            .append(new KeyedCodec<>("Port", Codec.INTEGER), (obj, val, info) -> obj.setPort(val != null ? val : 5520), (obj, info) -> obj.getPort()).add()
            .append(new KeyedCodec<>("Username", Codec.STRING), (obj, val, info) -> obj.setUsername(val != null ? val : "root"), (obj, info) -> obj.getUsername()).add()
            .append(new KeyedCodec<>("Database", Codec.STRING), (obj, val, info) -> obj.setDatabase(val != null ? val : "game_db"), (obj, info) -> obj.getDatabase()).add()
            .append(new KeyedCodec<>("Password", Codec.STRING), (obj, val, info) -> obj.setPassword(val != null ? val : "password"), (obj, info) -> obj.getPassword()).add()
            .build();

}
