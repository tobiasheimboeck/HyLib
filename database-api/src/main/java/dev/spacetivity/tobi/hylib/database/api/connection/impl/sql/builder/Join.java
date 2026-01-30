package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Table;

/**
 * Represents a JOIN clause in a SQL query.
 * 
 * <p>This record encapsulates the information needed to construct a JOIN clause:
 * the join type (INNER, LEFT, RIGHT), the table to join, and the columns to join on.
 * 
 * <h3>Usage</h3>
 * 
 * <p>Join objects are created automatically by the builder methods in {@link SelectBuilder}:
 * 
 * <pre>{@code
 * .innerJoin(postsTable, Column.of("user_id"), Column.of("id"))
 * }</pre>
 * 
 * <p>This creates a JOIN that matches {@code posts.user_id = users.id}.
 * 
 * @param type        the type of join (INNER, LEFT, or RIGHT)
 * @param table       the table to join
 * @param leftColumn  the column from the left table (or previous join)
 * @param rightColumn the column from the right table (the joined table)
 * @see JoinType
 * @see SelectBuilder#innerJoin(Table, Column, Column)
 * @see SelectBuilder#leftJoin(Table, Column, Column)
 * @see SelectBuilder#rightJoin(Table, Column, Column)
 * @since 1.0
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
