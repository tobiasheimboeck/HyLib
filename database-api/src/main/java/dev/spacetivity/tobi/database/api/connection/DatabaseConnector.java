package dev.spacetivity.tobi.database.api.connection;

import lombok.Getter;
import lombok.Setter;
import dev.spacetivity.tobi.database.api.connection.credentials.DatabaseCredentials;

@Getter
public abstract class DatabaseConnector<T, C extends DatabaseCredentials> {

    private final DatabaseType type;

    @Setter
    private T client;

    public DatabaseConnector(DatabaseType type) {
        this.type = type;
        this.client = null;
    }

    public abstract void establishConnection(C credentials);

    public T getSafeConnection() {
        if (this.client == null) throw new NullPointerException("Database connection is null!");
        return this.client;
    }

}
