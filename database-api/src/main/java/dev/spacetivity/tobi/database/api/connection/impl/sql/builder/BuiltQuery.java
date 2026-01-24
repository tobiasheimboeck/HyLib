package dev.spacetivity.tobi.database.api.connection.impl.sql.builder;

import java.util.List;

/**
 * Represents a built SQL query with its parameters.
 * The SQL string and parameters are in the correct order for PreparedStatement execution.
 */
public record BuiltQuery(String sql, List<Object> params) {
    public BuiltQuery {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL query cannot be null or blank");
        }
        if (params == null) {
            throw new IllegalArgumentException("Parameters list cannot be null");
        }
    }
}
