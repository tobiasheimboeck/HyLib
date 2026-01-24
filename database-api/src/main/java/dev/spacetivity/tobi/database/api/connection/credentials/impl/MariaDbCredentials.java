package dev.spacetivity.tobi.database.api.connection.credentials.impl;

import dev.spacetivity.tobi.database.api.connection.credentials.DatabaseCredentials;

public record MariaDbCredentials(String hostname, int port, String username, String database, String password) implements DatabaseCredentials {

}
