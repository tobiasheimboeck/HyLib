package dev.spacetivity.tobi.database.hytale.config;

import dev.spacetivity.tobi.database.api.config.AutoCodec;
import dev.spacetivity.tobi.database.api.config.CodecField;
import lombok.Getter;
import lombok.Setter;

@AutoCodec
@Getter
@Setter
public class DbConfig {

    @CodecField(value = "hostname", hasDefault = true, defaultValue = "localhost")
    private String hostname;

    @CodecField(value = "port", hasDefault = true, defaultValue = "5520")
    private int port;

    @CodecField(value = "username", hasDefault = false)
    private String username;

    @CodecField(value = "database", hasDefault = false)
    private String database;

    @CodecField(value = "password", hasDefault = false)
    private String password;

}
