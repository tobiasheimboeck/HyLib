package dev.spacetivity.tobi.database.api.connection.impl.sql;

/**
 * Represents a foreign key constraint referencing another table and column.
 */
public record ForeignKey(Table referencedTable, Column referencedColumn) {
    public ForeignKey {
        if (referencedTable == null) {
            throw new IllegalArgumentException("Referenced table cannot be null");
        }
        if (referencedColumn == null) {
            throw new IllegalArgumentException("Referenced column cannot be null");
        }
    }
}
