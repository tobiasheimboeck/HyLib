package dev.spacetivity.tobi.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.database.api.connection.impl.sql.Table;

/**
 * Factory class for creating SQL query builders.
 * Provides a fluent API for building type-safe SQL queries.
 */
public final class SqlBuilder {

    private SqlBuilder() {
        // Utility class
    }

    /**
     * Creates a SELECT query builder.
     * @param columns the columns to select (must not be empty)
     * @return a SelectBuilder instance
     */
    public static SelectBuilder select(Column... columns) {
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("At least one column must be specified");
        }
        return new SelectBuilder(columns);
    }

    /**
     * Creates an INSERT query builder.
     * @param table the table to insert into
     * @return an InsertBuilder instance
     */
    public static InsertBuilder insertInto(Table table) {
        return new InsertBuilder(table);
    }

    /**
     * Creates an UPDATE query builder.
     * @param table the table to update
     * @return an UpdateBuilder instance
     */
    public static UpdateBuilder update(Table table) {
        return new UpdateBuilder(table);
    }

    /**
     * Creates a DELETE query builder.
     * @param table the table to delete from
     * @return a DeleteBuilder instance
     */
    public static DeleteBuilder deleteFrom(Table table) {
        return new DeleteBuilder(table);
    }
}
