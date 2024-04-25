package net.neptunsworld.elytra.database.api.connection.impl.sql;

import com.zaxxer.hikari.HikariDataSource;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnector;
import net.neptunsworld.elytra.database.api.connection.DatabaseType;
import net.neptunsworld.elytra.database.api.connection.credentials.impl.MariaDbCredentials;

public class MariaDbConnector extends DatabaseConnector<HikariDataSource, MariaDbCredentials> {

    public MariaDbConnector() {
        super(DatabaseType.MARIADB);
    }

    @Override
    public void establishConnection(MariaDbCredentials credentials) {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setMaxLifetime(1800000);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(10000);
        dataSource.setIdleTimeout(600000);
        dataSource.addDataSourceProperty("cachePrepStmts", "true");
        dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource.setJdbcUrl("jdbc:mariadb://" + credentials.hostname() + ":" + credentials.port() + "/" + credentials.database());
        dataSource.setUsername(credentials.username());
        dataSource.setPassword(credentials.password());
        setClient(dataSource);
    }
}
