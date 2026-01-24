package dev.spacetivity.tobi.database.api.connection.impl.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps a single row from a ResultSet to a domain object.
 * @param <T> the type of the domain object
 */
@FunctionalInterface
public interface RowMapper<T> {
    /**
     * Maps the current row of the ResultSet to a domain object.
     * The ResultSet cursor is positioned at the current row.
     * @param rs the ResultSet positioned at the current row
     * @return the mapped domain object
     * @throws SQLException if a database access error occurs
     */
    T map(ResultSet rs) throws SQLException;
}
