package dev.spacetivity.tobi.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.database.api.connection.impl.sql.Table;

/**
 * Represents a JOIN clause in a SQL query.
 */
public record Join(JoinType type, Table table, Column leftColumn, Column rightColumn) {
    public Join {
        if (type == null) {
            throw new IllegalArgumentException("Join type cannot be null");
        }
        if (table == null) {
            throw new IllegalArgumentException("Join table cannot be null");
        }
        if (leftColumn == null) {
            throw new IllegalArgumentException("Left column cannot be null");
        }
        if (rightColumn == null) {
            throw new IllegalArgumentException("Right column cannot be null");
        }
    }
}
