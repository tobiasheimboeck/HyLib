package net.neptunsworld.elytra.database.api.connection.credentials.impl;

import net.neptunsworld.elytra.database.api.connection.credentials.DatabaseCredentials;

public record RedisCredentials(String hostname, int port, String password) implements DatabaseCredentials {

}
