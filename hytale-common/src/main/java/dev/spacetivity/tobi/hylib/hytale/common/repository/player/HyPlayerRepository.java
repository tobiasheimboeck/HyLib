package dev.spacetivity.tobi.hylib.hytale.common.repository.player;

import dev.spacetivity.tobi.hylib.database.api.connection.DatabaseConnectionHandler;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.*;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder.SqlBuilder;
import dev.spacetivity.tobi.hylib.database.api.repository.Repository;
import dev.spacetivity.tobi.hylib.database.api.repository.impl.AbstractMariaDbRepository;
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LocalizationService;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayer;
import dev.spacetivity.tobi.hylib.hytale.common.api.player.HyPlayerImpl;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class HyPlayerRepository extends AbstractMariaDbRepository<HyPlayer> implements Repository {

    public static final Table HY_PLAYERS_TABLE = Table.of("hy_players");

    public static final Column PLAYER_ID_COL = Column.of("player_id");
    public static final Column PLAYER_NAME_COL = Column.of("player_name");
    public static final Column LANGUAGE_COL = Column.of("language");

    public HyPlayerRepository(DatabaseConnectionHandler db, Connection connection) {
        super(db, TableDefinition.create(
                connection,
                HY_PLAYERS_TABLE,
                SQLColumn.fromPrimary(PLAYER_ID_COL, SQLDataType.UUID),
                SQLColumn.from(PLAYER_NAME_COL, SQLDataType.VARCHAR),
                SQLColumn.from(LANGUAGE_COL, SQLDataType.VARCHAR)
        ));
    }

    @Override
    public HyPlayer deserializeResultSet(ResultSet resultSet) {
        try {
            UUID uniqueId = UuidUtils.bytesToUuid(resultSet.getBytes(PLAYER_ID_COL.name()));
            String username = resultSet.getString(PLAYER_NAME_COL.name());
            String languageCode = resultSet.getString(LANGUAGE_COL.name());
            // Convert string from DB to Lang object
            Lang lang;
            if (languageCode != null) {
                lang = Lang.of(languageCode);
            } else {
                LocalizationService localizationService = HytaleProvider.getApi().getLocalizationService();
                lang = localizationService.getDefaultLanguage();
            }
            return new HyPlayerImpl(uniqueId, username, lang);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insert(HyPlayer hyPlayer) {
        byte[] uuidBytes = uuidToBytes(hyPlayer.getUniqueId());
        executeUpdate(SqlBuilder
                .insertInto(getTable())
                .value(PLAYER_ID_COL, uuidBytes)
                .value(PLAYER_NAME_COL, hyPlayer.getUsername())
                .value(LANGUAGE_COL, hyPlayer.getLanguage().getCode()) // Store as string in DB
                .build()
        );
    }

    public void changeUsername(UUID uniqueId, String newUsername) {
        byte[] uuidBytes = uuidToBytes(uniqueId);
        executeUpdate(SqlBuilder
                .update(getTable())
                .set(PLAYER_NAME_COL, newUsername)
                .where(PLAYER_ID_COL, uuidBytes)
                .build());
    }

    public void changeLanguage(UUID uniqueId, Lang lang) {
        byte[] uuidBytes = uuidToBytes(uniqueId);
        executeUpdate(SqlBuilder
                .update(getTable())
                .set(LANGUAGE_COL, lang.getCode()) // Store as string in DB
                .where(PLAYER_ID_COL, uuidBytes)
                .build());
    }

    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

}
