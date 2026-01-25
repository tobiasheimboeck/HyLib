package dev.spacetivity.tobi.database.hytale.config;

import dev.spacetivity.tobi.database.api.config.AutoCodec;
import dev.spacetivity.tobi.database.api.config.CodecField;
import lombok.Getter;
import lombok.Setter;

@AutoCodec
@Getter
@Setter
public class DbConfig {

    @CodecField(value = "Hostname", hasDefault = true, defaultValue = "localhost")
    private String hostname = "localhost";

    @CodecField(value = "Port", hasDefault = true, defaultValue = "5520")
    private Integer port = 5520;

    @CodecField(value = "Username", hasDefault = true, defaultValue = "root")
    private String username = "root";

    @CodecField(value = "Database", hasDefault = true, defaultValue = "game_db")
    private String database = "game_db";

    @CodecField(value = "Password", hasDefault = true, defaultValue = "password")
    private String password = "password";

}
